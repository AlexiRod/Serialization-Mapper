package org.hse.rodionov208.classes;

import ru.hse.homework4.*;

import java.time.LocalDateTime;
import java.util.*;

@Exported
public class User {
    @PropertyName("Name")
    String name;
    @Ignored
    int age;
    @DateFormat("yyyy-MM-dd hh:mm:ss")
    LocalDateTime ldt;

    User bestFriend;
    List<User> friends;

    public User() {}

    public User(String name) {
        this.name = name;
        this.friends = new ArrayList<>();
    }

    public User(String name, int age, LocalDateTime ldt) {
        this.name = name;
        this.age = age;
        this.ldt = ldt;
    }

    public void setBestFriend(User user) {
        bestFriend = user;
    }


    public void addFriend(User user) {
        friends.add(user);
    }

    public User getFriend(int ind) {
        return friends.get(ind);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", ldt=" + ldt +
                ", friends=" + friends +
                '}';
    }
}
