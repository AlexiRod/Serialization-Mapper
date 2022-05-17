package org.hse.rodionov208.exceptions;

/**
 * Ошибка при сериализации поля объекта для сериализации
 * @author Алексей Родионов
 */
public class MapperSerializationFieldException extends MapperSerializationException {
    public MapperSerializationFieldException(String mes) {
        super(mes);
    }
}
