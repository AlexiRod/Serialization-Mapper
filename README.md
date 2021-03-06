# Serialization Mapper
## Mapper для собственной сериализации данных

### Даты разработки: 02.2022-03.2022

### Ключевые технологии: Java, JUnit Test

### Описание

Реализация собственного маппера для сериализации данных в формат, схожий с Json.
Сериализовать классы или рекорды можно в строку, в файл или в поток, десериализовать соответственно из строки, файла или потока.

Аннотации для работы маппера:
1) @Exported - Методы Mapper кидают исключение при попытке сохранить или восстановить экземпляр класса без аннотации @Exported.
* Настройка NullHandling - настраивает обработку значений null для reference типов
* Настройка UnknownPropertiesPolicy - определяет, что делает Mapper, если при восстановлении он встречает
неизвестное поле
2) @PropertyName - меняет ключ, по которому Mapper восстанавливает или сохраняет значение поля.
3) @Ignored - помеченное поле не попадает в строку сериализации
4) @DateFormat - устанавливает, по какому шаблону Mapper сохраняет и восстанавливает поля типов LocalDate, LocalTime и LocalDateTime.

Поддерживаемые типы данных для сохранения и восстановления экземпляра класса с полями типа T, где T — одно из:
1. Примитив или соответствующий wrapper-класс
2. String
3. @Exported класс — класс, сохраняемый и восстанавливаемый Mapper'ом
4. List<T>
5. Set<T>
6. enum
7. LocalDate
8. LocalTime
9. LocalDateTime

Подбробнее про реализацию программы можно прочитать в файле Readme.md в основной папке проекта.