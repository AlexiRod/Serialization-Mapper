package org.hse.rodionov208.exceptions;

/**
 * Ошибка при десериализации объекта
 * @author Алексей Родионов
 */
public class MapperDeserializationException extends RuntimeException {
    public MapperDeserializationException(String mes) {
        super(mes);
    }
}
