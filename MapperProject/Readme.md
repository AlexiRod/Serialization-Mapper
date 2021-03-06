### Описание проекта
Библиотека реализует поддержку операций сохранения и восстановления классов и рекордов,
помеченных аннотацией @Exported. Реализованы все поддерживаемые типы и методы, заявленные в тз.

### Формат сохранения
Перед каждым сохраняемым блоком через равно записывается количество символов, занимаемых этим блоком.
Пример: 17={блок_сохранения}

Поля записываются в формате число="название_поля":"значение_поля";
После каждого поля записывается точка с запятой
Пример: 17="name":"example";

Значения массивов и сетов записываются в формате число=[элемент1,элемент2...]
При этом каждый элемент записывается как отдельный блок сохранения, то есть для каждого элемента
вначале указано количество символов, занимаемых элементом. В конце ставится запятая
Пример: 24="arr":"[4="1",5="12",]";
Значения остальных типов данных записываются в стандартном для себя представлении
Пример: 11="age"="15";

Классы записываются в формате число={название_класса~поля_класса}
Пример: 55={org.hse.rodionov208.example.java~17="name":"example";}

### Сохранение
Сохранение объекта происходит в указанном формате. В случае некорректной работы маппера кидается
непроверяемое исключение MapperSerializationException или его дочерние представители.

### Восстановление
Восстановление объекта происходит из указанного формата. В случае некорректной работы маппера кидается
непроверяемое исключение MapperSerializationException или его дочерние представители.

### О классах
Маппер работает с классами или рекордами, помеченными аннотацией @Exported, имеющими публичный
конструктор без параметров, сохраняет их нестатические поля, которые не являются isSynthetic 
и не помечены аннотацией @Ignored. Для создания и работы с рекордами вызывается каноничный конструктор.

### retainIdentity
Поддержка равенства ссылок реализована и в случае, если до сериализации два поля указывали на один
объект, то после десериализации они также будут указывать на один объект.

### Циклы
В случае, если в графе объектов для сериализации был найден цикл, кидается дочернее непроверяемое
исключение MapperSerializationCyclesException.

### Исключения
MapperSerializationException и MapperDeserializationException - два основных непроверяемых исключения
для сохранения и восстановления объекта. Остальные являются их дочерними представителями. Некоторые
проверяемые исключения, которые кидаются при работе маппера, перехватываются и передаются как соответствующие
MapperSerializationException или MapperDeserializationException для того, чтобы не нарушать декларацию
исходного интерфейса.
MapperSerializationFieldException - исключение при некорректной работе с полем класса при сериализации
MapperSerializationCycleException - исключение при нахождении цикла в графе объектов
MapperDeserializationFieldException - исключение при обнаружении в строке для десериализации неизвестного
поля, не представленного в классе. Кидается, если у заданного поля unknownPropertyPolicy = FAIL

### Поддерживаемые аннотации
Реализованы все заявленные аннотации, их работа протестирована в юнит-тестах. При задании некорректных
значений у аннотаций (например, PropertyName или DateFormat) ответственность за эти действия возлагается
на пользователя.

### Тесты и структура проекта
Тесты покрывают 100% методов реализации маппера и тестируют разные возможные случаи для разных классов.
Интерфейс маппера и аннотации расположены в пакете ru.hse.homework4, реализация и другие пакеты расположены
в org.hse.rodionov208 В пакете classes представлены классы для тестирования маппера.
