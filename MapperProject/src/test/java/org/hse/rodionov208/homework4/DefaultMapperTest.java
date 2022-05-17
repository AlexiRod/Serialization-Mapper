package org.hse.rodionov208.homework4;

import org.hse.rodionov208.classes.*;
import org.hse.rodionov208.exceptions.*;
import org.junit.jupiter.api.*;
import ru.hse.homework4.*;

import java.io.*;
import java.time.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMapperTest {
    Mapper mapper = new DefaultMapper(true);

    /**
     * Вспомогательный метод для проверки сериализации/десериализации заданного объекта.
     * Проверяется равенство строковых представлений объекта до и после сериализации/десериализации
     * @param object Объект для тестирования
     */
    private void testSerializeAndDeserializeObject(Object object) {
        try {
            String ser = mapper.writeToString(object);
            assertEquals(object.toString(), (mapper.readFromString(object.getClass(), ser)).toString());
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()));
        }
    }


    /**
     * Тестирование всех простых декларированных типов кроме @Exported объектов
     * (как выяснилось, это даже сверх ТЗ и сериализовывать/десериализовывать нужно только @Exported классы)
     */
    @Test
    void testNotExportedTypes() {
        int intP = Integer.MAX_VALUE / 2;
        long longP = Long.MAX_VALUE / 2;
        short shortP = Short.MAX_VALUE / 2;
        byte byteP = Byte.MAX_VALUE / 2;
        double doubleP = 3.1;
        float floatP = 5.4f;
        boolean boolP = true;
        char charP = 'a';
        String stringSimple = "simple string";
        String stringCrack = "{className~\"field\":\"vale\";}";
        NullHandling enumP = NullHandling.EXCLUDE;
        LocalDateTime ldt = LocalDateTime.now();
        LocalDate ld = LocalDate.now();
        LocalTime lt = LocalTime.now();

        testSerializeAndDeserializeObject(Integer.valueOf(intP));
        testSerializeAndDeserializeObject(intP);
        testSerializeAndDeserializeObject(Long.valueOf(longP));
        testSerializeAndDeserializeObject(longP);
        testSerializeAndDeserializeObject(Short.valueOf(shortP));
        testSerializeAndDeserializeObject(shortP);
        testSerializeAndDeserializeObject(Byte.valueOf(byteP));
        testSerializeAndDeserializeObject(byteP);
        testSerializeAndDeserializeObject(Double.valueOf(doubleP));
        testSerializeAndDeserializeObject(doubleP);
        testSerializeAndDeserializeObject(Float.valueOf(floatP));
        testSerializeAndDeserializeObject(floatP);
        testSerializeAndDeserializeObject(Boolean.valueOf(boolP));
        testSerializeAndDeserializeObject(boolP);
        testSerializeAndDeserializeObject(Character.valueOf(charP));
        testSerializeAndDeserializeObject(charP);
        testSerializeAndDeserializeObject(stringSimple);
        testSerializeAndDeserializeObject(stringCrack);
        testSerializeAndDeserializeObject(enumP);
        testSerializeAndDeserializeObject(ldt);
        testSerializeAndDeserializeObject(ld);
        testSerializeAndDeserializeObject(lt);
    }

    /**
     * Тестирование класса с простыми полями (без вложенности объектов и коллекций)
     * В классе представлены поля с примитивными типами данных, Wrapper-классами, листами, сетами, перечислениями и дата-типами
     */
    @Test
    void testClassWithSimpleFieldsAndCollections() {
        Student student = new Student("Simple", 19, 57.6, Arrays.asList(10, 8, 9, 8, 10),
                new HashSet<>(Set.of("simple", "s1mple", "easy")), LocalDateTime.now(), LocalTime.now(), LocalDate.now(), NullHandling.INCLUDE);
        testSerializeAndDeserializeObject(student);
    }

    /**
     * Тестирование рекорда с простыми полями (без вложенности объектов и коллекций)
     * В рекорде представлены поля с примитивными типами данных, Wrapper-классами, листами, сетами, перечислениями и дата-типами
     */
    @Test
    void testRecordWithSimpleFieldsAndCollections() {
        StudentRecord student = new StudentRecord("Simple", 19, 57.6, Arrays.asList(10, 8, 9, 8, 10),
                new HashSet<>(Set.of("simple", "s1mple", "easy")), LocalDateTime.now(), LocalTime.now(), LocalDate.now(), NullHandling.INCLUDE);
        testSerializeAndDeserializeObject(student);
    }

    /**
     * Тестирование класса с коллекциями вложенных объектов
     * Представлена классовая иерархия со вложенностями одних классов в другие
     */
    @Test
    void testClassWithCollectionsOfClasses() {
        Student s1 = new Student("Alice");
        Student s2 = new Student("Bob");
        Student s3 = new Student("Claire");
        Student s4 = new Student("Denis");
        Teacher t1 = new Teacher("Teacher1", new ArrayList<>(), new HashSet<>());
        Teacher t2 = new Teacher("Teacher2", Arrays.asList(s1, s2, s3), new HashSet<>());
        Teacher t3 = new Teacher("Teacher3", Arrays.asList(s2, s3, s4), new HashSet<>(Set.of(s1, s2, s4)));
        testSerializeAndDeserializeObject(t1);
        testSerializeAndDeserializeObject(t2);
        testSerializeAndDeserializeObject(t3);
    }

    /**
     * Тестирование сериализации/десериализации пустого объекта или null-объекта
     * Объект с незаполненными полями сериализуется и десериализуется как пустой, null-объект выдает исключение
     */
    @Test
    void testSerializeNullOrEmptyObject() {
        Student empty = new Student();
        testSerializeAndDeserializeObject(empty);
        assertThrows(MapperSerializationException.class, () -> mapper.writeToString(null),
                "Нельзя сериализовать null-объект.");
    }

    /**
     * Тестирование сериализации класса без публичного конструктора или не помеченного аннотацией @Exported
     * Класс без явно объявленного публичного конструктора без параметров или непомеченный @Exported кидает исключение
     */
    @Test
    void testSerializeClassWithoutAnnotationOrPublicConstructor() {
        DumbClass1 dumb1 = new DumbClass1();
        DumbClass2 dumb2 = new DumbClass2(1);

        assertThrows(MapperSerializationException.class, () -> mapper.writeToString(dumb1),
               "Объект не помечен аннотацией @Exported: " + dumb1);
        assertThrows(MapperSerializationException.class, () -> mapper.writeToString(dumb2),
                "Объект не имеет публичного конструктора без параметров: " + dumb2);
    }

    /**
     * Тестирование сериализации класса с циклов внутри полей
     * Классовая иерархия выстроена таким образом, чтобы внутри графа образовался граф, который обработается исключением
     */
    @Test
    void testSerializeClassesWithCycles() {
        User u1 = new User("A");
        User u2 = new User("B");
        User u3 = new User("C");
        User u4 = new User("D");
        User u5 = new User("E");
        u1.setBestFriend(u2); //    1
        u2.setBestFriend(u3); //    |
        u2.setBestFriend(u4); //    2 <--
        u4.setBestFriend(u5); //   / \    \
        u5.setBestFriend(u2); //  3   4 -- 5

        assertThrows(MapperSerializationCycleException.class, () -> mapper.writeToString(u1),
                "Цикл найден между полями: " + u2 + " и " + u5);
    }

    /**
     * Тестирование всех декларированных аннотаций (@PropertyName, @Ignored, @DateFormat)
     */
    @Test
    void testAllDeclaredAnnotations() {
        LocalDateTime ldt = LocalDateTime.now();
        User user = new User("John", 54, ldt);
        assertEquals("83={org.hse.rodionov208.classes.User~14=\"Name\":\"John\";28=\"ldt\":\""+
                ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")) +"\";}", mapper.writeToString(user));
    }

    /**
     * Тестирование настройки маппера NullHandling в двух вариантах
     * Пустые поля класса либо игнорируются, либо записываются как null
     */
    @Test
    void testNullHandlingSetting() {
        mapper = new DefaultMapper(true);

        UserExcludeNull user1 = new UserExcludeNull();
        assertEquals("46={org.hse.rodionov208.classes.UserExcludeNull~}", mapper.writeToString(user1));

        UserIncludeNull user2 = new UserIncludeNull();
        assertEquals("82={org.hse.rodionov208.classes.UserIncludeNull~14=\"name\":\"null\";16=\"friend\":\"null\";}", mapper.writeToString(user2));
    }

    /**
     * Тестирование настройки маппера UnknownPropertyPolicy в двух вариантах
     * Неизвестно при десериализации поле либо игнорируется, либо выбрасывает исключение
     */
    @Test
    void testUnknownPropertySetting() {

        mapper = new DefaultMapper(true);
        UserExcludeNull user = new UserExcludeNull("John");
        String str1 = "73={org.hse.rodionov208.classes.UserExcludeNull~14=\"name\":\"John\";8=\"a\":\"b\";}";
        UserExcludeNull deser = mapper.readFromString(UserExcludeNull.class, str1);
        assertEquals(user.toString(), deser.toString());

        String str2 = "73={org.hse.rodionov208.classes.UserIncludeNull~14=\"name\":\"John\";8=\"a\":\"b\";}";
        assertThrows(MapperDeserializationFieldException.class, () -> mapper.readFromString(UserIncludeNull.class, str2),
                "Не удалось в классе " + UserIncludeNull.class + " для названия поля a однозначно найти подходящее поле.");
    }

    /**
     * Тестирование настройки маппера retainIdentity в двух вариантах у полей разных классов
     * Классовая иерархия выстроена таким образом, что два объекта ссылаются на один и тот же. При восстановлении объекта
     * равенство ссылок у полей двух этих классов сохраняется.
     */
    @Test
    void testRetainIdentitySetting() {
        mapper = new DefaultMapper(true);
        User u1 = new User("A");
        User u2 = new User("B");
        User u3 = new User("C");
        User u4 = new User("D");
        u1.addFriend(u2); //    1
        u1.addFriend(u3); //   / \
        u2.addFriend(u4); //  2   3
        u3.addFriend(u4); //   \ /
                          //    4
        User deser1 = mapper.readFromString(User.class, mapper.writeToString(u1));
        assertSame(deser1.getFriend(0).getFriend(0), deser1.getFriend(1).getFriend(0));

        mapper = new DefaultMapper( false);
        User deser2 = mapper.readFromString(User.class, mapper.writeToString(u1));
        assertNotSame(deser2.getFriend(0).getFriend(0), deser2.getFriend(1).getFriend(0));
    }

    /**
     * Тестирование настройки маппера retainIdentity в двух вариантах у полей одного класса
     * Иерархия полей устроена таким образом, что у двух полей одного класса одинаковые ссылки.
     * После восстановления равенство ссылок этих полей сохраняется
     */
    @Test
    void testRetainIdentitySettingFromOneClass() {
        mapper = new DefaultMapper(true);
        TreeNode child = new TreeNode();
        TreeNode treeNode = new TreeNode(child, child);

        TreeNode deser1 = mapper.readFromString(TreeNode.class, mapper.writeToString(treeNode));
        assertSame(deser1.getLeft(), deser1.getRight());

        mapper = new DefaultMapper(false);
        TreeNode deser2 = mapper.readFromString(TreeNode.class, mapper.writeToString(treeNode));
        assertNotSame(deser2.getLeft(), deser2.getRight());
    }

    /**
     * Тестирование записи/чтения объекта в/из OutputStream/InputStream
     * @throws IOException Ошибка ввода-вывода
     */
    @Test
    void testReadWriteToStream() throws IOException {
        Student student = new Student("Simple", 19, 57.6, Arrays.asList(10, 8, 9, 8, 10),
                new HashSet<>(Set.of("simple", "s1mple", "easy")), LocalDateTime.now(), LocalTime.now(), LocalDate.now(), NullHandling.INCLUDE);

        File etalon = new File("src/test/resources/etalon.txt");
        File file = new File("src/test/resources/file1.txt");
        mapper.write(student, new PrintStream(file));
        assertEquals(etalon.getFreeSpace(), file.getFreeSpace());
        assertEquals(student.toString(), mapper.read(Student.class, new FileInputStream(file)).toString());
    }

    /**
     * Тестирование записи/чтения объекта в/из файла
     * @throws IOException Ошибка ввода-вывода
     */
    @Test
    void testReadWriteToFile() throws IOException {
        Student student = new Student("Simple", 19, 57.6, Arrays.asList(10, 8, 9, 8, 10),
                new HashSet<>(Set.of("simple", "s1mple", "easy")), LocalDateTime.now(), LocalTime.now(), LocalDate.now(), NullHandling.INCLUDE);

        File etalon = new File("src/test/resources/etalon.txt");
        File file = new File("src/test/resources/file2.txt");
        mapper.write(student, file);
        assertEquals(etalon.getFreeSpace(), file.getFreeSpace());
        assertEquals(student.toString(), mapper.read(Student.class, file).toString());
    }
}