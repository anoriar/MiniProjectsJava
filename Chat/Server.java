package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс, реализующий сервер
 */
public class Server {

    /**
     * Все входящие соединения (ключ - имя пользователя, значение - соединение)
     */
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    /**
     * Запуск сервера. Пишем порт сервера. Затем сервер создает свой сокет и ждет входящего соединения (через serverSocket.accept)
     * Когда соединение принято, запускает поток Handler
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        int serverPort = ConsoleHelper.readInt();
        try (ServerSocket serverSocket  = new ServerSocket(serverPort)){
            ConsoleHelper.writeMessage("Сервер запущен");
            while(true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch(Exception e) {
            ConsoleHelper.writeMessage(e.getMessage());
        }
    }

    /**
     * Посылает сообщение всем участникам чата
     * @param message сообщение, которое надо переслать
     */
    public static void sendBroadcastMessage(Message message) {
        for(Map.Entry<String, Connection> elem : connectionMap.entrySet()) {
            try{
                elem.getValue().send(message);
            }catch(IOException e) {
                ConsoleHelper.writeMessage("Не смогли отправить сообщение пользователю " + elem.getKey());
            }
        }
    }

    /**
     * Класс, обрабатывающий работу сервера с соединением
     */
    private static class Handler extends Thread{

        /**
         * Сокет, с которым работает сервер
         */
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        /**
         * "Рукопожатие" сервера и клиента (основная задача: взять имя пользователя, который подсоединился к серверу)
         * Сервер посылает запрос на имя клиента (NAME_REQUEST)
         * Получает сообщение от клиента
         * Проверяет, правильный ли тип ответа ( он должен быть USER_NAME).
         * Если сообщение от клиента другого типа, то повторяет NAME_REQUEST
         * Если правильный, то записывает имя пользователя
         * Проверяет, содержится ли в соединениях такое имя и не пусто ли оно.
         * @param connection
         * @return имя пользователя
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            Message message = null;
            while(true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                message = connection.receive();
                if(!message.getType().equals(MessageType.USER_NAME))
                    continue;
                name = message.getData();
                if(!connectionMap.containsKey(name) && !name.equals(""))
                    break;
            }
            connectionMap.put(name, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            ConsoleHelper.writeMessage(name + " принято");
            return name;
        }

        /**
         * Посылает список пользователей чата, только что вошедшему пользователю
         * @param connection
         * @param userName
         * @throws IOException
         */
        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            for(Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                if(!entry.getKey().equals(userName)) {
                    Message message = new Message(MessageType.USER_ADDED, entry.getKey());
                    connection.send(message);
                }
            }
        }

        /**
         * Основной цикл сервера (основная задача: позволить обмениваться сообщениями клиенту и серверу)
         *  В бесконечном цикле получает сообщения от клиента.
         *  Если сообщение типа TEXT, отправляет его в чат в формате: [username]: [message]
         * @param connection
         * @param userName
         * @throws IOException
         * @throws ClassNotFoundException
         */
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT) {
                    String data = String.format("%s: %s", userName, message.getData());
                    sendBroadcastMessage(new Message(MessageType.TEXT, data));
                } else{
                    ConsoleHelper.writeMessage("Error!");
                }
            }
        }

        /**
         * Запуск потока обработчика соединения клиента с сервером
         * Оповещает, с каким адресом было установлено соединение.
         * Создает новое соединение.
         * Вызывает "рукопожатие" сервера и клиента (в  успешном случае берет имя пользователя)
         * Посылает сообщение участникам чата, что пользователь присоединился к чату
         * Посылает список пользователей чата новому пользователю
         * Запускает основной цикл сервера
         * В конце работы (если пользователь отсоединился или в результате Exception):
         * Закрывает соединение, оповещает других пользователей о том, что пользователь вышел из чата
         */
        public void run(){
            String clientName = null;
            ConsoleHelper.writeMessage("Connection with port " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket))
            {
                clientName = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, clientName));
                sendListOfUsers(connection, clientName);
                serverMainLoop(connection, clientName);
            }
            catch (IOException | ClassNotFoundException e)
            {
                ConsoleHelper.writeMessage("An error occurred while communicating with the remote address.");
            }
            finally
            {
                if(clientName != null)
                    connectionMap.remove(clientName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, clientName));
            }

            ConsoleHelper.writeMessage(String.format("Connection with remote address (%s) is closed.", socket.getRemoteSocketAddress()));

        }
    }
}
