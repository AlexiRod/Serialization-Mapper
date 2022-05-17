package ru.hse.homework4;

import org.hse.rodionov208.exceptions.*;

import java.io.*;

public interface Mapper {
    /**
     * Читает сохранённый экземпляр класса {@code clazz} из строки {@code input}
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     * String input = """
     * {"comment":"Хорошая работа","resolved":false}""";
     * ReviewComment reviewComment =
     mapper.readFromString(ReviewComment.class, input);
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz класс, сохранённый экземпляр которого находится в {@code input}
     * @param input строковое представление сохранённого экземпляра класса {@code
    clazz}
     * @param <T> возвращаемый тип метода
     * @return восстановленный экземпляр {@code clazz}
     * @throws MapperSerializationException непроверяемое исключение в случае несоответствии строки заявленным требованиям
     */
    <T> T readFromString(Class<T> clazz, String input) throws MapperDeserializationException;

    /**
     * Читает объект класса {@code clazz} из {@code InputStream}'а
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Данный метод закрывает {@code inputStream}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     * String input = """
     * {"comment":"Хорошая работа","resolved":false}""";
     * ReviewComment reviewComment = mapper.read(ReviewComment.class,
     * new
     ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz класс, сохранённый экземпляр которого находится в {@code
    inputStream}
     * @param inputStream поток ввода, содержащий строку в {@link
     * java.nio.charset.StandardCharsets#UTF_8} кодировке
     * @param <T> возвращаемый тип метода
     * @return восстановленный экземпляр класса {@code clazz}
     * @throws IOException в случае ошибки ввода-вывода
     * @throws MapperSerializationException непроверяемое исключение в случае ошибки десериализации строки
     */
    <T> T read(Class<T> clazz, InputStream inputStream) throws IOException, MapperSerializationException;

    /**
     * Читает сохранённое представление экземпляра класса {@code clazz} из {@code
    File}'а
     * и возвращает восстановленный экземпляр класса {@code clazz}.
     * <p>
     * Пример вызова:
     *
     * <pre>
     * ReviewComment reviewComment = mapper.read(ReviewComment.class, new
     File("/tmp/review"));
     * System.out.println(reviewComment);
     * </pre>
     *
     * @param clazz класс, сохранённый экземпляр которого находится в файле
     * @param file файл, содержимое которого - строковое представление экземпляра
    {@code clazz}
     * в {@link java.nio.charset.StandardCharsets#UTF_8} кодировке
     * @param <T> возвращаемый тип метода
     * @return восстановленный экземпляр {@code clazz}
     * @throws IOException в случае ошибки ввода-вывода
     * @throws MapperSerializationException непроверяемое исключение в случае ошибки десериализации строки
     */
    <T> T read(Class<T> clazz, File file) throws IOException, MapperSerializationException;

    /**
     * Сохраняет {@code object} в строку
     * <p>
     * Пример вызова:
     *
     * <pre>
     * ReviewComment reviewComment = new ReviewComment();
     * reviewComment.setComment("Хорошая работа");
     * reviewComment.setResolved(false);
     *
     * String string = mapper.writeToString(reviewComment);
     * System.out.println(string);
     * </pre>
     *
     * @param object объект для сохранения
     * @throws MapperSerializationException в случае сериализации объекта:
     * без нужной аннотации @Exported, не наследующегося от Object/Record, null-Object
     * @throws MapperSerializationException непроверяемое исключение в случае несоответствии объекта заявленным требованиям
     * @return строковое представление объекта в выбранном формате
     */

    String writeToString(Object object) throws MapperSerializationException;
    /**
     * Сохраняет {@code object} в {@link OutputStream}.
     * <p>
     * То есть после вызова этого метода в {@link OutputStream} должны оказаться
     байты, соответствующие строковому
     * представлению {@code object}'а в кодировке {@link
    java.nio.charset.StandardCharsets#UTF_8}
     * <p>
     * Данный метод закрывает {@code outputStream}
     * <p>
     * Пример вызова:
     *
     * <pre>
     * ReviewComment reviewComment = new ReviewComment();
     * reviewComment.setComment("Хорошая работа");
     * reviewComment.setResolved(false);
     *
     * mapper.write(reviewComment, new FileOutputStream("/tmp/review"));
     * </pre>
     *
     * @param object объект для сохранения
     * @throws IOException в случае ошибки ввода-вывода
     * @throws MapperSerializationException непроверяемое исключение при сериализации объекта в строку
     */
    void write(Object object, OutputStream outputStream) throws IOException, MapperSerializationException;

    /**
     * Сохраняет {@code object} в {@link File}.
     * <p>
     * То есть после вызова этого метода в {@link File} должны оказаться байты,
     соответствующие строковому
     * представлению {@code object}'а в кодировке {@link
    java.nio.charset.StandardCharsets#UTF_8}
     * <p>
     * Данный метод закрывает {@code outputStream}
     * <p>
     * Пример вызова:
     *
     * <pre>
     * ReviewComment reviewComment = new ReviewComment();
     * reviewComment.setComment("Хорошая работа");
     * reviewComment.setResolved(false);
     *
     * mapper.write(reviewComment, new File("/tmp/review"));
     * </pre>
     *
     * @param object объект для сохранения
     * @throws IOException в случае ошибки ввода-вывода
     * @throws MapperSerializationException непроверяемое исключение при сериализации объекта в строку
     */
    void write(Object object, File file) throws IOException, MapperSerializationException;
}

