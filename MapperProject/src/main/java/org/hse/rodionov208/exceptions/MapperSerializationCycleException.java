package org.hse.rodionov208.exceptions;

/**
 * Ошибка при нахождении цикла в графе объектов при десериализации
 * @author Алексей Родионов
 */
public class MapperSerializationCycleException extends MapperSerializationException {
    public MapperSerializationCycleException(String mes) {
        super(mes);
    }
}
