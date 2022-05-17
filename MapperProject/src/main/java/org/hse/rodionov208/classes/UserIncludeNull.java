package org.hse.rodionov208.classes;

import ru.hse.homework4.*;

@Exported(nullHandling = NullHandling.INCLUDE, unknownPropertiesPolicy = UnknownPropertiesPolicy.FAIL)
public class UserIncludeNull {
    String name = null;
    User friend = null;

    public UserIncludeNull() {}
}
