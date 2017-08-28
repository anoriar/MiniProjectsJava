package com.javarush.task.task35.task3513;


import java.util.*;

public class Model {

    private final static int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    private Stack<Tile[][]> previousStates = new Stack<>();
    private Stack<Integer> previousScores = new Stack<>();
    private boolean isSaveNeeded = true;
    int score = 0;
    int maxTile = 2;


    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    public boolean hasBoardChanged() {
        Tile[][] tile = previousStates.peek();
        for(int i = 0; i < tile.length; i++){
            for(int j = 0; j < tile[i].length; j++){
                if(tile[i][j].value != gameTiles[i][j].value)
                    return true;
            }
        }
        return false;
    }

    public void autoMove(){
        PriorityQueue queue = new PriorityQueue(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                left();
            }
        }));
        queue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                right();
            }
        }));
        queue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                up();
            }
        }));
        queue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                down();
            }
        }));

        MoveEfficiency autoMoveEfficiency = (MoveEfficiency) queue.peek();
        autoMoveEfficiency.getMove().move();
    }

    public MoveEfficiency getMoveEfficiency(Move move){
        MoveEfficiency moveEfficiency = null;
        move.move();
        if(hasBoardChanged()) {
            int countOfEmptyTiles = 0;
            for(int i = 0; i <  gameTiles.length; i++){
                for(int j = 0; j <  gameTiles[i].length; j++){
                   if(gameTiles[i][j].value == 0){
                        countOfEmptyTiles++;
                   }
                }
            }
            moveEfficiency = new MoveEfficiency(countOfEmptyTiles, score, move);
        } else {
            moveEfficiency = new MoveEfficiency(-1, 0, move);
        }
        rollback();
        return  moveEfficiency;
    }

    public void saveState(Tile[][] tile){
        Tile[][] newTile = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < tile.length; i++){
            for(int j = 0; j < tile[i].length; j++){
                newTile[i][j] = new Tile(tile[i][j].value);
            }
        }
        previousStates.push(newTile);
        previousScores.push(score);
        isSaveNeeded = false;
    }
    public void rollback(){
        if(!previousStates.isEmpty())
            gameTiles = (Tile[][]) previousStates.pop();
        if(!previousScores.isEmpty())
            score = (int) previousScores.pop();
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch(n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    public boolean canMove() {
        int length = gameTiles.length;
        int counter = 0;
        for(int i = 0; i < length; i++) {
            for(int j = 0; j < length; j++) {
                int tileValue = gameTiles[i][j].value;
                if(tileValue == 0)
                    return true;
                if(i + 1 != length && tileValue == gameTiles[i + 1][j].value)
                    counter++;
                if(j + 1 != length && tileValue == gameTiles[i][j + 1].value)
                    counter++;
                if(i - 1 >= 0 && tileValue == gameTiles[i - 1][j].value)
                    counter++;
                if(j - 1 >= 0 && tileValue == gameTiles[i][j - 1].value)
                    counter++;
            }
        }
        if(counter == 0)
            return false;
        return true;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if(emptyTiles == null || emptyTiles.isEmpty())
            return;
        int value = Math.random() < 0.9 ? 2 : 4;
        int randomPos = (int) (emptyTiles.size() * Math.random());
        Tile tile = emptyTiles.get(randomPos);
        tile.value = value;

    }

    private List<Tile> getEmptyTiles() {
        List<Tile> tiles = new ArrayList<>();
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles[0].length; j++) {
                if(gameTiles[i][j].isEmpty()) {
                    tiles.add(gameTiles[i][j]);
                }
            }
        }
        return tiles;
    }

    public void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles[0].length; j++) {
                gameTiles[i][j] = new Tile();
                //(int)(Math.random() * 2)
            }
        }

        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean change = false;
        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles.length; j++) {
                if (j + 1 != tiles.length && tiles[j].isEmpty() && !tiles[j + 1].isEmpty()) {
                    change = true;
                    tiles[j] = tiles[j + 1];
                    tiles[j + 1] = new Tile();
                }
            }
        }
        return change;
    }


    private boolean mergeTiles(Tile[] tiles) {
        boolean change = false;
        for (int i = 0; i < tiles.length; i++) {
            if (i + 1 != tiles.length && (tiles[i].value == tiles[i + 1].value) && !tiles[i].isEmpty() && !tiles[i + 1].isEmpty()) {
                change = true;
                tiles[i].value *= 2;
                score += tiles[i].value;
                maxTile = maxTile > tiles[i].value ? maxTile : tiles[i].value;
                tiles[i + 1] = new Tile();
                compressTiles(tiles);
            }
        }
        return change;
    }

    public void printMatrix(){
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles.length; j++) {
                System.out.print(gameTiles[i][j].value + " ");
            }
            System.out.println();
        }
    }


    public void left() {
        if(isSaveNeeded)
            saveState(gameTiles);
        boolean change = false;
        for(int i = 0; i < gameTiles.length; i++) {
            if(compressTiles(gameTiles[i]))
                change = true;
            if(mergeTiles(gameTiles[i]))
                change = true;
        }

        if(change) {
            addTile();
        }
        isSaveNeeded = true;
    }

    public void down() {
        saveState(gameTiles);
        rotate();
        left();
        rotate();
        rotate();
        rotate();
    }

    public void right() {
        saveState(gameTiles);
        rotate();
        rotate();
        left();
        rotate();
        rotate();
    }

    public void up() {
        saveState(gameTiles);
        rotate();
        rotate();
        rotate();
        left();
        rotate();
    }


    public void rotate() {
       Tile[][] result = new Tile[gameTiles.length][gameTiles.length];
       int size = gameTiles.length;
       for(int i = 0; i < size; i++) {
           for(int j = 0; j < size; j++) {
               result[j][size - 1 - i] = gameTiles[i][j];
           }
       }
        gameTiles = result;
    }

}
