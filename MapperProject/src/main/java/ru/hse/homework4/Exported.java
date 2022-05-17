package ru.hse.homework4;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Exported {
    NullHandling nullHandling() default NullHandling.EXCLUDE;

    UnknownPropertiesPolicy unknownPropertiesPolicy()
            default UnknownPropertiesPolicy.FAIL;
}
