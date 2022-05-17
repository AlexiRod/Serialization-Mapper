package org.hse.rodionov208.classes;

import ru.hse.homework4.*;
import java.util.*;
import java.util.stream.Collectors;

@Exported
public class Teacher {
    String name;
    List<Student> students;
    HashSet<Student> bests;

    public Teacher () {}

    public Teacher(String name, List<Student> students, HashSet<Student> bests) {
        this.name = name;
        this.students = students;
        this.bests = bests;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                ", students=" + students.stream().map(Student::toString).collect(Collectors.joining()) +
                ", besets=" + bests.size() +
                '}';
    }
}
