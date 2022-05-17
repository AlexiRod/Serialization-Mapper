package ru.hse.homework4;

import java.lang.annotation.*;

@Target({
        ElementType.RECORD_COMPONENT,
        ElementType.FIELD,
})
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignored {
}

