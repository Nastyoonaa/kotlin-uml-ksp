# Kotlin UML Generator (KSP)

Проект демонстрирует использование **Kotlin Symbol Processing (KSP)** для автоматического анализа Kotlin-кода и генерации UML-описаний классов.

Во время компиляции аннотированные классы обрабатываются кастомным **KSP-процессором**, который генерирует API для получения UML-модели класса.

Полученная модель может быть преобразована в **PlantUML-диаграмму**.

---

# Текущий этап реализации

На текущем этапе реализован не только анализ структуры классов, но и **анализ зависимостей между ними**.

Процесс включает:

1. Поиск классов, помеченных аннотацией `@UmlDiagram`
2. Анализ структуры классов:
    * свойства (поля)
    * методы
    * параметры методов
    * возвращаемые типы
3. Анализ зависимостей между классами на основе:
    * параметров конструктора
    * параметров методов
    * возвращаемых типов
4. Генерацию Kotlin-API:

   ```
   TestClass.uml()
   ```
5. Преобразование модели в PlantUML через DSL

---

# Что теперь умеет проект

Проект выполняет **статический анализ архитектуры** и определяет:

* зависимости между классами
* связи между объектами
* взаимодействие между компонентами системы

В UML-диаграмме автоматически появляются связи:

```
TestClass --> Order
TestClass *-- Repository
```

где:

* `-->` — зависимость (использование)
* `*--` — композиция (через конструктор)

---

# Архитектура решения

Проект построен по многоуровневой архитектуре:

```
Kotlin Source Code
        │
        ▼
KSP Processor
        │
        ▼
Code Analysis (structure + dependencies)
        │
        ▼
Generated API
(TestClass.uml())
        │
        ▼
UmlClass (модель)
        │
        ▼
UmlDsl
        │
        ▼
PlantUML текст
```

Анализ кода отделён от визуализации диаграммы, что позволяет расширять проект.

---

# Используемые технологии

* Kotlin
* Gradle
* KSP (Kotlin Symbol Processing)
* PlantUML
* ktlint (линтинг и стиль кода)

---

# Структура проекта

```
kotlin-uml-ksp
│
├── processor
│   ├── UmlDiagram.kt
│   ├── UmlProcessor.kt
│   └── UmlProcessorProvider.kt
│
├── src
│   └── main
│       └── kotlin
│           ├── Main.kt
│           └── TestClass.kt
│
├── uml
│   ├── UmlClass.kt
│   ├── UmlDsl.kt
│   └── PlantUmlRenderer.kt
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

# Аннотация

Для анализа классов используется аннотация:

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class UmlDiagram
```

---

# Пример использования

```kotlin
@UmlDiagram
class TestClass(
    val order: Order
) {
    fun process(user: User): Result {}
}
```

---

# Генерация PlantUML
UML-модель можно преобразовать в PlantUML:

```kotlin
val diagram = PlantUmlRenderer.render(uml)
println(diagram)
```

---

# Пример результата

```
@startuml
class TestClass {
  -order : Order

  +process(user: User): Result
}

TestClass *-- Order
TestClass --> User
@enduml
```

---

# Запуск проекта

Сборка:

```
./gradlew build
```

Запуск:

```
./gradlew run
```

---
# Планируемое развитие проекта

Следующие этапы проекта:

### 1. Генерация полной UML-диаграммы проекта

Автоматическое построение:

* class diagrams
* dependency graphs

### 2. Использование LLM для анализа архитектуры

На основе UML-модели планируется использовать LLM для:

* анализа архитектуры проекта
* выявления сильно связанных компонентов
* определения потоков данных между классами
* предложения улучшений архитектуры

---

# Автор

Анастасия Ципенюк
tg: @Iydyshka_krovopivyshka