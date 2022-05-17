package org.hse.rodionov208.homework4;

import org.apache.commons.lang3.*;
import org.hse.rodionov208.exceptions.*;
import ru.hse.homework4.*;

import java.io.*;
import java.math.BigInteger;
import java.time.*;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

/**
 * Реализация интерфейса Mapper для сериализации/десериализации объекта
 * @author Алексей Родионов
 */
public class DefaultMapper implements Mapper {
    private final boolean retainIdentity;
    // Граф объектов для десериализации и нахождения циклов
    private final Map<Object, List<Object>> graph = new IdentityHashMap<>();
    // Словарь объектов для поддержания равенства ссылок при retainIdentity = true
    private final Map<String, Object> objects = new HashMap<>();

    public DefaultMapper(boolean retainIdentity) {
        this.retainIdentity = retainIdentity;
    }

    // Переопределенное чтение из InputStream - так как нужна вся строка, а не ее часть, сначала вызывается readFromString()
    @Override
    public <T> T read(Class<T> clazz, InputStream inputStream) throws IOException, MapperSerializationException {
        objects.clear();
        String str = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
        return readFromString(clazz, str);
    }

    // Переопределенное чтение из File - так как нужна вся строка, а не ее часть, сначала вызывается readFromString()
    @Override
    public <T> T read(Class<T> clazz, File file) throws IOException, MapperSerializationException {
        objects.clear();
        String str = new BufferedReader(new InputStreamReader(new FileInputStream(file)))
                .lines().collect(Collectors.joining("\n"));
        return readFromString(clazz, str);
    }

    // Переопределенное чтение объекта из строки. В случае ошибки работы кидается непроверяемое исключение или его наследники.
    @Override
    public <T> T readFromString(Class<T> clazz, String input) throws MapperDeserializationException {
        objects.clear();
        return deserialie(clazz, input);
    }

    // Переопределенная запись в OutputStream - сначала объект сериализуется в writeToString(), затем записывается.
    @Override
    public void write(Object object, OutputStream outputStream) throws IOException, MapperSerializationException {
        String serializedString = writeToString(object);
        outputStream.write(serializedString.getBytes());
    }

    // Переопределенная запись в File - сначала объект сериализуется в writeToString(), затем записывается.
    @Override
    public void write(Object object, File file) throws IOException, MapperSerializationException {
        String serializedString = writeToString(object);
        Files.writeString(file.toPath(), serializedString);
    }

    // Переопределенная запись объекта в строку. В случае ошибки работы кидается непроверяемое исключение или его наследники.
    @Override
    public String writeToString(Object object) throws MapperSerializationException {
        graph.clear();
        objects.clear();
        return serialize(object, null);
    }

    /***
     * Вспомогательный метод сериализации объекта для неоднократного вызова
     * @param clazz Тип объекта
     * @param input Строка для десериализации
     * @param <T> Обобщенный тип
     * @return Готовый Java-объект
     * @throws MapperDeserializationException Перехват исключений неправильной работы рефлексии или ошибка при десериализации
     */
    private <T> T deserialie(Class<T> clazz, String input) throws MapperDeserializationException {
        String objectName = "";
        try {
            if (!isExportedType(clazz)) {
                return getFieldValueFromString(input, null, clazz);
            }
            if (!clazz.isAnnotationPresent(Exported.class)) {
                throw new MapperDeserializationException("Объект не помечен аннотацией @Exported: " + clazz);
            }

            String[] parts = splitByChar("=", input);
            input = parts[1].substring(1, Integer.parseInt(parts[0]) - 1);
            var res = objects.get(input);
            if (retainIdentity && res != null) {
                return (T) res;
            }

            parts = splitByChar("~", input);
            objectName = parts[0];
            Class<?> objectClass = Class.forName(objectName);
            if (Record.class.isAssignableFrom(objectClass)) {
                Record record = (Record) createObject(objectClass);
                return fillRecord(record, parts[1], objectName);
            }
            Object object = objectClass.getConstructor().newInstance();
            res = fillClass(object, parts[1], objectName);
            objects.put(input, res);
            return (T) res;
        } catch (ClassNotFoundException ex) {
            throw new MapperDeserializationException("При десериализации обнаружен неизвестный класс: " + objectName);
        } catch (NoSuchMethodException ex) {
            throw new MapperDeserializationException("При десериализации обнаружен класс без публичного конструктора: " + objectName);
        } catch (InvocationTargetException ex) {
            throw new MapperDeserializationException("При десериализации не удалось создать экземпляр класса: " + objectName + "\n" + ex.getMessage());
        }catch (InstantiationException ex) {
            throw new MapperDeserializationException("При десериализации произошла ошибка инициализации: " + ex.getMessage());
        }catch (IllegalAccessException ex) {
            throw new MapperDeserializationException("При десериализации произошла ошибка доступа к полю/компоненту: " + ex.getMessage());
        }
    }

    /***
     * Вспомогательный метод заполнения объекта класса данными из строки.
     * @param object Объект для заполнения
     * @param input Строка с данными
     * @param objectName строковое название объекта
     * @param <T> Обобщенный тип
     * @return Заполненный данными объект типа класс
     * @throws MapperDeserializationException дочерние ошибки при десериализации
     */
    private <T> T fillClass(Object object, String input, String objectName) throws MapperDeserializationException, IllegalAccessException,
            ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        while (!input.isEmpty()) {
            String[] parts = getSymbols(input);
            String fieldString = parts[0];
            input = parts[1];

            parts = splitByChar(":", fieldString);
            String fieldKey = parts[0].substring(1, parts[0].length() - 1);
            String fieldValue = parts[1].substring(1, parts[1].length() - 2);

            Field field = (Field) getFieldByName(fieldKey, object, objectName);
            if (field == null) {
                continue;
            }
            field.trySetAccessible();
            var value = getFieldValueFromString(fieldValue, field, field.getType());
            field.set(object, value);
        }
        return (T) object;
    }

    /***
     * Вспомогательный метод заполнения объекта-рекорда данными из строки.
     * @param record Рекорд для заполнения
     * @param input Строка с данными
     * @param recordName строковое название рекорда
     * @param <T> Обобщенный тип
     * @return Заполненный данными объект типа рекорд
     * @throws MapperDeserializationException дочерние ошибки при десериализации
     */
    private <T> T fillRecord(Record record, String input, String recordName) throws MapperDeserializationException, IllegalAccessException,
            ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        List<Object> componentsValues = new ArrayList<>();
        while (!input.isEmpty()) {
            String[] parts = getSymbols(input);
            String fieldString = parts[0];
            input = parts[1];

            parts = splitByChar(":", fieldString);
            String fieldKey = parts[0].substring(1, parts[0].length() - 1);
            String fieldValue = parts[1].substring(1, parts[1].length() - 2);

            RecordComponent component = (RecordComponent) getFieldByName(fieldKey, record, recordName);
            if (component == null) {
                continue;
            }
            componentsValues.add(getFieldValueFromString(fieldValue, component, component.getType()));
        }
        return (T) createRecord(record, componentsValues);
    }

    /***
     * Вспомогательный метод создания рекорда по каноничному конструктору
     * @param record Рекорд для создания
     * @param componentsValues Список аргументов для канонического конструктора
     * @return Заполненный рекорд
     */
    private Record createRecord(Record record, List<Object> componentsValues) throws NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?>[] canonicalComponentTypes = Arrays.stream(record.getClass().getRecordComponents())
                .map(rc -> rc.getType())
                .toArray(Class<?>[]::new);
        var canonical = record.getClass().getDeclaredConstructor(canonicalComponentTypes);

        while (componentsValues.size() < canonicalComponentTypes.length) {
            var type = canonicalComponentTypes[componentsValues.size()];
            var a = createObject(type);
            componentsValues.add(a);
        }
        return canonical.newInstance(componentsValues.toArray());
    }

    /***
     * Получение поля класса по заданному названию и проверка его на соответствие
     * @param fieldKey Название поля для поиска
     * @param object Объект, по полям которого осуществляется поиск нужного
     * @param objectName Строковое название объекта
     * @return Найденное поле
     * @throws MapperDeserializationFieldException Непроверяемое исключение в случае ненахождения необходимого поля
     */
    private AnnotatedElement getFieldByName(String fieldKey, Object object, String objectName) throws MapperDeserializationFieldException {
        var collection = object instanceof Record ? object.getClass().getRecordComponents() : object.getClass().getDeclaredFields();
        var fieldsList = Arrays.stream(collection).filter(f ->
                f.isAnnotationPresent(PropertyName.class) && f.getAnnotation(PropertyName.class).value().equals(fieldKey) ||
                        !f.isAnnotationPresent(PropertyName.class) &&
                                ((object instanceof Record ? ((RecordComponent) f).getName().equals(fieldKey) :
                                        ((Field) f).getName().equals(fieldKey)))).collect(Collectors.toList());
        if (fieldsList.size() != 1) {
            if (object.getClass().isAnnotationPresent(Exported.class) &&
                    object.getClass().getAnnotation(Exported.class).unknownPropertiesPolicy() == UnknownPropertiesPolicy.FAIL) {
                throw new MapperDeserializationFieldException("Не удалось в классе " + objectName +
                        " для названия поля " + fieldKey + " однозначно найти подходящее поле.");
            } else {
                return null;
            }
        }
        return fieldsList.get(0);
    }

    /***
     * Получение данных для данного поля из строки
     * @param str Строка с данными
     * @param field Поле для заполнения
     * @param clazz Класс, которому принадлежит поле
     * @param <T> Обобщенный тип
     * @return Значение для поля
     * @throws MapperDeserializationException Дочерние ошибки при десериализации
     */
    private <T> T getFieldValueFromString(String str, AnnotatedElement field, Class<?> clazz) throws MapperDeserializationException,
            IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        if (str.startsWith("{") && str.endsWith("}") && !String.class.isAssignableFrom(clazz) || clazz.isAnnotationPresent(Exported.class)) {
            return (T) deserialie(clazz, str);
        }
        if (str.startsWith("[") && str.endsWith("]")) {
            if (List.class.isAssignableFrom(clazz)) {

                List<?> list = getGenericList(str.substring(1, str.length() - 1), clazz, field);
                return (T) list;
            }
            Set<?> set = getGenericSet(str.substring(1, str.length() - 1), clazz, field);
            return (T) set;
        }
        if (Enum.class.isAssignableFrom(clazz)) {
            return (T) Enum.valueOf((Class<Enum>) clazz, str);
        }
        if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return (T) (field != null && field.isAnnotationPresent(DateFormat.class) ?
                    LocalDateTime.parse(str, DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    LocalDateTime.parse(str));
        }
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return (T) (field != null && field.isAnnotationPresent(DateFormat.class) ?
                    LocalDate.parse(str, DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    LocalDate.parse(str));
        }
        if (LocalTime.class.isAssignableFrom(clazz)) {
            return (T) (field != null && field.isAnnotationPresent(DateFormat.class) ?
                    LocalTime.parse(str, DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    LocalTime.parse(str));
        }
        return getPrimitiveFromString(clazz, str);
    }

    /***
     * Вспомогательный метод создания типизированного динамического массива из строки
     * @param str Строка с данными
     * @param clazz Класс, представляющий тип массива
     * @param field Поле, которое должно быть заполнено данным массивом
     * @param <T> Обобщенный тип
     * @return Заполненный массив
     * @throws MapperDeserializationException Дочерние ошибки при десериализации
     */
    private <T> List<T> getGenericList(String str, Class<?> clazz, AnnotatedElement field) throws IllegalAccessException,
            ClassNotFoundException, InvocationTargetException, MapperDeserializationException, NoSuchMethodException, InstantiationException {
        ParameterizedType stringListType = field instanceof Field ? (ParameterizedType) ((Field) field).getGenericType() :
                (ParameterizedType) ((RecordComponent) field).getGenericType();
        Class<T> listClass = (Class<T>) stringListType.getActualTypeArguments()[0];
        List<T> list = new ArrayList<>();
        if (!clazz.isInterface()) {
            list = (List<T>) createObject(clazz);
        }

        while (!str.isEmpty()) {
            String[] parts = getSymbols(str);
            String val = parts[0].substring(1, parts[0].length() - 2);
            str = parts[1];
            if (listClass.isAnnotationPresent(Exported.class)) {
                list.add(deserialie(listClass, val));
            } else {
                list.add(getFieldValueFromString(val, field, listClass));
            }
        }
        return list;
    }

    /***
     * Вспомогательный метод создания типизированного сета массива из строки
     * @param str Строка с данными
     * @param clazz Класс, представляющий тип массива
     * @param field Поле, которое должно быть заполнено данным массивом
     * @param <T> Обобщенный тип
     * @return Заполненный сет
     * @throws MapperDeserializationException Дочерние ошибки при десериализации
     */
    private <T> Set<T> getGenericSet(String str, Class<T> clazz, AnnotatedElement field) throws IllegalAccessException, ClassNotFoundException,
            InvocationTargetException, MapperDeserializationException, NoSuchMethodException, InstantiationException {
        ParameterizedType stringListType = field instanceof Field ? (ParameterizedType) ((Field) field).getGenericType() :
                (ParameterizedType) ((RecordComponent) field).getGenericType();
        Class<T> setClass = (Class<T>) stringListType.getActualTypeArguments()[0];
        Set<T> set = new HashSet<>();
        if (!clazz.isInterface()) {
            set = (Set<T>) createObject(clazz);
        }

        while (!str.isEmpty()) {
            String[] parts = getSymbols(str);
            String val = parts[0].substring(1, parts[0].length() - 2);
            str = parts[1];
            if (setClass.isAnnotationPresent(Exported.class)) {
                set.add(deserialie(setClass, val));
            } else {
                set.add(getFieldValueFromString(val, field, setClass));
            }
        }
        return set;
    }


    /**
     * Получение примитива или Wrapper-a из строки
     * @param clazz Класс примитива или Wrapper-a
     * @param str Строка с данными
     * @param <T> Обобщенный тип
     * @return Готовый примитив или Wrapper-класс
     */
     private <T> T getPrimitiveFromString(Class<?> clazz, String str) {
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Long.parseLong(str));
        }
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Integer.parseInt(str));
        }
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Double.parseDouble(str));
        }
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Float.parseFloat(str));
        }
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Short.parseShort(str));
        }
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Byte.parseByte(str));
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            return (T) (Object) (Boolean.parseBoolean(str));
        }
        if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz)) {
            return (T) str;
        }
        return (T) str;
    }

    /**
     * Разделение строки на две подстроки по заданному символу (первое вхождение)
     * @param ch Символ для разделения
     * @param str Исходная строка
     * @return Массив с двумя частями строки после разделения
     */
    private String[] splitByChar(String ch, String str) {
        String[] splited = str.split(ch);
        return new String[]{splited[0], str.substring(splited[0].length() + 1)};
    }

    /**
     * Получение заданного количества символов из строки
     * @param str Исходная строка
     * @return Массив с извлеченным количеством символов и остатком строки
     */
    private String[] getSymbols(String str) {
        String[] parts = splitByChar("=", str);
        int count = Integer.parseInt(parts[0]);
        String symbs = parts[1].substring(0, count);
        String newStr = count <= parts[1].length() ? parts[1].substring(count) : "";
        return new String[]{symbs, newStr};
    }

    /**
     * Рекурсивный метод создания объекта заданного типа с дефолтными значениями
     * @param clazz Тип объекта
     * @param <T> Обобщенный тип
     * @return Объект с дефолтными значениями
     */
    private <T> T createObject(Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var ret = getDeclaredTypeDefaultValue(clazz);
        if (ret != null || Enum.class.isAssignableFrom(clazz)) {
            return (T) ret;
        }

        var conss = clazz.getDeclaredConstructors();
        var cons = Arrays.stream(clazz.getDeclaredConstructors()).filter(x -> x.getParameterTypes().length == 0).findFirst();
        if (cons.isPresent() || conss.length == 0) {
            return clazz.getConstructor().newInstance();
        }

        Class<?>[] a = clazz.getDeclaredConstructors()[0].getParameterTypes();
        Object[] c = new Object[a.length];
        for (int i = 0; i < a.length; ++i) {
            c[i] = createObject(a[i]);
        }
        return clazz.getDeclaredConstructor(a).newInstance(c);
    }

    /**
     * Получение дефолтного значения примитива или Wrapper-класса
     * @param clazz Заданный тип
     * @param <T> Обобщенный тип
     * @return Дефолтное значение примитива или Wrapper-класса
     */
    private <T> T getDeclaredTypeDefaultValue(Class<?> clazz) {
        if (Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)) {
            return (T) Long.valueOf(0);
        }
        if (Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)) {
            return (T) Integer.valueOf(0);
        }
        if (Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)) {
            return (T) Double.valueOf(0.0);
        }
        if (Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)) {
            return (T) Float.valueOf(0f);
        }
        if (Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)) {
            return (T) Short.valueOf((short) 0);
        }
        if (Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)) {
            return (T) Byte.valueOf((byte) 0);
        }
        if (Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)) {
            return (T) Boolean.valueOf(true);
        }
        if (Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz)) {
            return (T) Character.valueOf('\0');
        }
        if (String.class.isAssignableFrom(clazz)) {
            return (T) "";
        }
        if (List.class.isAssignableFrom(clazz) && clazz.isInterface()) {
            return (T) new ArrayList<T>();
        }
        if (Set.class.isAssignableFrom(clazz) && clazz.isInterface()) {
            return (T) new HashSet<T>();
        }
        if (LocalDateTime.class.isAssignableFrom(clazz)) {
            return (T) LocalDateTime.now();
        }
        if (LocalDate.class.isAssignableFrom(clazz)) {
            return (T) LocalDate.now();
        }
        if (LocalTime.class.isAssignableFrom(clazz)) {
            return (T) LocalTime.now();
        }
        return null;
    }

    /**
     * Вспомогательный метод для повторного использования сериализации объекта
     * @param object Сериализуемый объект
     * @param parent "Родитель" объекта - чьим полем он является, если является (для проверки циклов)
     * @return Сериализованный в строку объект
     * @throws MapperSerializationException Заявленные ошибки при сериализации и дочерние ошибки
     */
    private String serialize(Object object, Object parent) throws MapperSerializationException {
        List<String> strings;
        if (Objects.isNull(object)) {
            throw new MapperSerializationException("Нельзя сериализовать null-объект.");
        }
        if (Record.class.isAssignableFrom(object.getClass())) {
            strings = serializeRecordClass(object, parent);
        } else {
            if (!isExportedType(object.getClass())) {
                return (getStringFromFieldValue(null, object, null));
            }
            strings = serializeClass(object, parent);
        }
        String res = "{" + object.getClass().getName() + "~" + String.join("", strings) + "}";
        return res.length() + "=" + res;
    }

    /**
     * Вспомогательный метод сериализации объекта класса
     * @param object Объект для сериализации
     * @param parent "Родитель" объекта
     * @return Массив строк с сериализованными полями объекта
     * @throws MapperSerializationException Заявленные ошибки при сериализации и дочерние ошибки
     */
    private List<String> serializeClass(Object object, Object parent) throws MapperSerializationException {
        try {
            Class<?> clazz = checkObject(object, parent);
            List<String> strings = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || field.isAnnotationPresent(Ignored.class)) {
                    continue;
                }
                boolean access = field.canAccess(object);
                if (!field.trySetAccessible()) {
                    throw new MapperSerializationFieldException("Невозможно установить Accessible = true у поля " + field.getName());
                }

                String key = field.getName();
                if (field.getAnnotation(PropertyName.class) != null && !field.getAnnotation(PropertyName.class).value().isEmpty()) {
                    key = field.getAnnotation(PropertyName.class).value();
                }
                String val = null;
                if (Objects.isNull(field.get(object))) {
                    if(object.getClass().isAnnotationPresent(Exported.class) &&
                            object.getClass().getAnnotation(Exported.class).nullHandling() == NullHandling.INCLUDE) {
                        String res = "\"" + key + "\":\"null\";";
                        strings.add(res.length() + "=" + res);
                    }
                    continue;
                }
                val = getStringFromFieldValue(field, field.get(object), object);
                String res = "\"" + key + "\":\"" + val + "\";";
                strings.add(res.length() + "=" + res);
                field.setAccessible(access);
            }
            return strings;
        } catch (IllegalAccessException e) {
            throw new MapperSerializationException("Ошибка при обращении к полю класса (IllegalAccessException): " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод сериализации объекта-рекорда
     * @param object Объект-рекорд для сериализации
     * @param parent "Родитель" объекта
     * @return Массив строк с сериализованными компонентами рекорда
     * @throws MapperSerializationException Заявленные ошибки при сериализации и дочерние ошибки
     */
    private List<String> serializeRecordClass(Object object, Object parent) throws MapperSerializationException {
        try {
            List<String> strings = new ArrayList<>();
            Record record = (Record) object;
            Class<?> clazz = checkObject(object, parent);

            for (RecordComponent component : clazz.getRecordComponents()) {
                if (component.isAnnotationPresent(Ignored.class)) {
                    continue;
                }
                String key = component.getName();
                if (component.getAnnotation(PropertyName.class) != null &&
                        !component.getAnnotation(PropertyName.class).value().isEmpty()) {
                    key = component.getAnnotation(PropertyName.class).value();
                }
                String val;
                if (Objects.isNull(component.getAccessor().invoke(record)) && object.getClass().isAnnotationPresent(Exported.class)
                        && object.getClass().getAnnotation(Exported.class).nullHandling() == NullHandling.EXCLUDE) {
                    continue;
                }
                val = getStringFromFieldValue(component, component.getAccessor().invoke(record), record);
                String res = "\"" + key + "\":\"" + val + "\";";
                strings.add(res.length() + "=" + res);
            }
            return strings;
        } catch (InvocationTargetException e) {
            throw new MapperSerializationException("Ошибка при создании рекорда (InvocationTargetException): " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new MapperSerializationException("Ошибка при обращении к полю рекорда (IllegalAccessException): " + e.getMessage());
        }
    }

    /**
     * Получение строкового представления значения поля
     * @param field Поле для сериализации
     * @param object Объект, которому принадлежит поле
     * @param parent "Родитель" объекта
     * @return
     * @throws MapperSerializationException
     */
    private String getStringFromFieldValue(AnnotatedElement field, Object object, Object parent) throws MapperSerializationException {

        Class<?> type = object.getClass();
        if (ClassUtils.isPrimitiveOrWrapper(type) || type == String.class || Enum.class.isAssignableFrom(type)) {
            return object.toString();
        }
        if (type.isAnnotationPresent(Exported.class)) {
            return serialize(object, parent);
        }
        if (List.class.isAssignableFrom(type)) {
            if (((List<?>) object).isEmpty()) {
                return "[]";
            }
            StringBuilder elems = new StringBuilder("[");
            for (var elem : (List<?>) object) {
                String res = "'" + getStringFromFieldValue(field, elem, parent) + "',";
                elems.append(res.length()).append('=').append(res);
            }
            return elems.append("]").toString();
        }
        if (Set.class.isAssignableFrom(type)) {
            if (((Set<?>) object).isEmpty()) {
                return "[]";
            }
            StringBuilder elems = new StringBuilder("[");
            for (var elem : (Set<?>) object) {
                String res = "'" + getStringFromFieldValue(field, elem, parent) + "',";
                elems.append(res.length()).append('=').append(res);
            }
            return elems.append("]").toString();
        }
        if (LocalDateTime.class.isAssignableFrom(type)) {
            LocalDateTime ldt = (LocalDateTime) object;
            return field != null && field.isAnnotationPresent(DateFormat.class) ?
                    ldt.format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    ldt.toString();
        }
        if (LocalDate.class.isAssignableFrom(type)) {
            LocalDate ldt = (LocalDate) object;
            return field != null && field.isAnnotationPresent(DateFormat.class) ?
                    ldt.format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    ldt.toString();
        }
        if (LocalTime.class.isAssignableFrom(type)) {
            LocalTime ldt = (LocalTime) object;
            return field != null && field.isAnnotationPresent(DateFormat.class) ?
                    ldt.format(DateTimeFormatter.ofPattern(field.getAnnotation(DateFormat.class).value())) :
                    ldt.toString();
        }
        throw new MapperSerializationFieldException("Невозможно сериализовать поле данного типа: " + type +
                ". Проверьте, подходит есть ли оно в списке допустимых типов и помечено ли @Exported");
    }

    /**
     * Проверка объекта на соответствующие правила
     * @param object Объект для проверки
     * @param parent "Родитель объекта"
     * @return Класс объекта, если объект прошел проверку
     * @throws MapperSerializationException Несоответствие правилам или MapperSerializationCycleException при нахождении цикла
     */
    private Class<?> checkObject(Object object, Object parent) throws MapperSerializationException {
        Class<?> clazz = object.getClass();
        // Честно говоря, вообще не понимаю этого условия - все объекты наследуются от Object, а Рекорды отлавливаются раньше
        if (!(object instanceof Object) && !(object instanceof Record)) {
            throw new MapperSerializationException("Объект не наследуется от Object или от Record: " + clazz);
        }
        if (!isExportedType(clazz)) {
            return clazz;
        }
        if (!(object instanceof Record)) {
            if(Arrays.stream(clazz.getDeclaredConstructors()).filter(x -> x.getParameterCount() == 0).count() != 1) {
                throw new MapperSerializationException("Объект не имеет публичного конструктора без параметров: " + clazz);
            }
        }
        if (!clazz.isAnnotationPresent(Exported.class)) {
            throw new MapperSerializationException("Объект не помечен аннотацией @Exported: " + clazz);
        }

        var parents = graph.get(parent);
        if (parents != null && parents.contains(object)) {
            throw new MapperSerializationCycleException("Цикл найден между полями: " + object + " и " + parent);
        }
        List<Object> value = parents == null ? new ArrayList<>() : new ArrayList<>(parents);
        value.add(parent);
        graph.put(object, value);
        return clazz;
    }

    /**
     * Проверка типа на то, является ли он декларированным @Exported классом
     * @param type Тип для проверки
     * @return Результат проверки
     */
    private boolean isExportedType(Class<?> type) {
        return !(ClassUtils.isPrimitiveOrWrapper(type) || type == String.class || Enum.class.isAssignableFrom(type) ||
                List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type) || LocalDate.class.isAssignableFrom(type) ||
                LocalDateTime.class.isAssignableFrom(type) || LocalTime.class.isAssignableFrom(type));
    }
}