package org.hse.rodionov208.classes;

import ru.hse.homework4.*;

@Exported(nullHandling = NullHandling.EXCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.IGNORE)
public class UserExcludeNull {
    String name = null;
    User friend = null;

    public UserExcludeNull() {}

    public UserExcludeNull(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserExcludeNull{" +
                "name='" + name + '\'' +
                '}';
    }
}
