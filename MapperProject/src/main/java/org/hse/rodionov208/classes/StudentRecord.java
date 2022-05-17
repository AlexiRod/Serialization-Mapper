package org.hse.rodionov208.classes;

import ru.hse.homework4.Exported;
import ru.hse.homework4.NullHandling;
import ru.hse.homework4.PropertyName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Exported
public record StudentRecord(String name, int age, @PropertyName("Mass") Double weight, List<Integer> marks,
                    @PropertyName("Friends names") Set<String> names, LocalDateTime ldt, LocalTime lt, LocalDate ld, NullHandling nullHand) {
    @Override
    public String toString() {
        return "StudentRecord{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", weight=" + weight +
                ", marks=" + marks +
                ", names=" + names +
                ", ldt=" + ldt +
                ", lt=" + lt +
                ", ld=" + ld +
                ", nullHand=" + nullHand +
                '}';
    }
}
