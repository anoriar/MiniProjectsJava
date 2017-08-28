package com.javarush.task.task29.task2909.human;

import java.util.ArrayList;
import java.util.List;

public class University {

    private List<Student> students = new ArrayList<>();
    private String name;
    private int age;

    public University(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public List<Student> getStudents() {
        return students;
    }
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Student getStudentWithAverageGrade(double averageGrade) {
        for(Student student : students) {
            if(student.getAverageGrade() == averageGrade) {
                return student;
            }
        }
        return null;
    }

    public Student getStudentWithMaxAverageGrade() {
        if(students != null && !students.isEmpty()) {
            double maxAverageGrade = students.get(0).getAverageGrade();
            double averageGrade;
            for(Student student : students) {
                averageGrade = student.getAverageGrade();
                if(averageGrade > maxAverageGrade) {
                    maxAverageGrade = averageGrade;
                }
            }
            return getStudentWithAverageGrade(maxAverageGrade);
        }
        return null;
    }

    public Student getStudentWithMinAverageGrade() {
        if(students != null && !students.isEmpty()) {
            double minAverageGrade = students.get(0).getAverageGrade();
            double averageGrade;
            for(Student student : students) {
                averageGrade = student.getAverageGrade();
                if(averageGrade < minAverageGrade) {
                    minAverageGrade = averageGrade;
                }
            }
            return getStudentWithAverageGrade(minAverageGrade);
        }
        return null;
    }

    public void expel(Student student) {
        students.remove(student);
    }

}