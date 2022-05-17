package org.hse.rodionov208.classes;

import ru.hse.homework4.*;
import java.time.*;
import java.util.*;

@Exported
public class Student {
    String name;
    int age;
    Double weight;
    List<Integer> marks;
    Set<String> names;
    LocalDateTime ldt = LocalDateTime.now();
    LocalTime lt = LocalTime.now();
    LocalDate ld = LocalDate.now();
    NullHandling nullHand = NullHandling.EXCLUDE;

    public Student () { }

    public Student(String name, int age, Double weight, List<Integer> marks,
                   Set<String> names, LocalDateTime ldt, LocalTime lt, LocalDate ld, NullHandling nullHand) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.marks = marks;
        this.names = names;
        this.ldt = ldt;
        this.lt = lt;
        this.ld = ld;
        this.nullHand = nullHand;
    }

    public Student (String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", weight=" + weight +
                ", marks=" + marks +
                ", names=" + names +
                ", lt=" + lt +
                ", ld=" + ld +
                ", ldt=" + ldt +
                ", nullHand=" + nullHand +
                '}';
    }
}
