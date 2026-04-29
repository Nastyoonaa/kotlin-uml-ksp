# Kotlin UML Generator (KSP)

Проект демонстрирует использование **Kotlin Symbol Processing (KSP)** для автоматического анализа Kotlin-кода и генерации UML-описаний классов.

Во время компиляции аннотированные классы обрабатываются кастомным **KSP-процессором**, который генерирует API для получения UML-модели класса.

Полученная модель может быть преобразована в **PlantUML-диаграмму**.

---

# Текущий этап реализации

На текущем этапе реализован не только анализ структуры классов, но и **анализ зависимостей между ними**, а также **генерация полной UML-диаграммы проекта**.

Процесс включает:

1. Поиск классов, помеченных аннотацией `@UmlDiagram`
2. Анализ структуры классов:

   * свойства (поля)
   * методы
   * параметры методов
3. Анализ зависимостей между классами на основе:

   * параметров конструктора
   * параметров методов
4. Генерацию Kotlin-API:

   ```
   TestClass.uml()
   projectUml()
   ```
5. Агрегацию всех классов проекта и построение единой UML-диаграммы
6. Преобразование модели в PlantUML через DSL
7. Преобразование модели в PlantUML через DSL

---

# Что теперь умеет проект

Проект выполняет **статический анализ архитектуры** и определяет:

* зависимости между классами
* связи между объектами
* взаимодействие между компонентами системы
* структуру всего проекта в виде единой UML-диаграммы
* разделение проекта на архитектурные слои (application, domain и др.)

В UML-диаграмме автоматически появляются связи:

```
TestClass *-- Order
TestClass --> User
TestClass --|> BaseService
TestClass ..|> Repository
Order *-- User
```

где:

* `-->` — зависимость (использование)
* `*--` — композиция (через конструктор)
* `--|>`— наследование
* `..|>`— реализация интерфейса

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
(TestClass.uml(), projectUml())
        │
        ▼
UmlClass / UmlProject (модель)
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
│   ├── UmlProject.kt
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
    fun process(user: User): Order
}
```

---

# Генерация PlantUML

UML-модель всего проекта можно преобразовать в PlantUML:

```kotlin
val project = projectUml()
val diagram = PlantUmlRenderer.renderProject(project)
println(diagram)
```

---

# Пример результата

```
@startuml
package "application" {
class TestClass {
  -order : Order
  +process(user: User): Order
}

}
package "domain" {
class Order {
  -user : User
}

class User {
  -name : String
}

}
TestClass *-- Order
TestClass --> User
TestClass --|> BaseService
TestClass ..|> Repository
Order *-- User
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

### Использование LLM для анализа архитектуры

На основе UML-модели планируется использовать LLM для:

* анализа архитектуры проекта
* выявления сильно связанных компонентов
* определения потоков данных между классами
* предложения улучшений архитектуры

---

# Автор

Анастасия Ципенюк
tg: @Iydyshka_krovopivyshka
