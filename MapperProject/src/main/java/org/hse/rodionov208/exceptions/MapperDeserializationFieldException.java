package org.hse.rodionov208.exceptions;

/**
 * Ошибка при ненахождении поля с заданным названием (Настройка UnknownPropertyPolicy = FAIL)
 * @author Алексей Родионов
 */
public class MapperDeserializationFieldException extends MapperDeserializationException {
    public MapperDeserializationFieldException(String mes) {
        super(mes);
    }
}