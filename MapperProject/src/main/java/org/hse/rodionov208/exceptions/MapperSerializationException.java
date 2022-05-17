package org.hse.rodionov208.exceptions;

/**
 * Ошибка при сериализации объекта
 * @author Алексей Родионов
 */
public class MapperSerializationException extends RuntimeException {
    public MapperSerializationException(String mes) {
        super(mes);
    }
}
