# Kotlin UML Generator (KSP)

Проект демонстрирует использование **Kotlin Symbol Processing (KSP)** для автоматического анализа Kotlin-кода и генерации UML-описаний классов.

Во время компиляции аннотированные классы обрабатываются кастомным **KSP-процессором**, который генерирует API для получения UML-модели класса.

Полученная модель может быть преобразована в **PlantUML-диаграмму**.

---

# Текущий этап реализации

На текущем этапе реализован не только анализ структуры классов, но и **анализ зависимостей между ними**, **генерация полной UML-диаграммы проекта**, а также **интеграция LLM для анализа архитектуры**.

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
7. Анализ архитектуры с использованием LLM (через локальную модель)

---

# Что теперь умеет проект

Проект выполняет **статический и AI-анализ архитектуры** и определяет:

* зависимости между классами
* связи между объектами
* взаимодействие между компонентами системы
* структуру всего проекта в виде единой UML-диаграммы
* разделение проекта на архитектурные слои (application, domain и др.)
* выявление проблем архитектуры (сильная связность, нарушения слоёв)
* генерацию рекомендаций по улучшению
* определение потоков данных между классами (data flow)

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

Дополнительно строится **data flow**:

```
TestClass -> Order -> User
```

и визуально отображается в диаграмме:
```
TestClass --> Order : flow
Order --> User : flow
```
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
        │
        ▼
LLM Analyzer (архитектурный анализ)
```

Анализ кода отделён от визуализации диаграммы, что позволяет расширять проект.

---

# Используемые технологии

* Kotlin
* Gradle
* KSP (Kotlin Symbol Processing)
* PlantUML
* OkHttp (HTTP клиент)
* Ollama (локальная LLM)
* ktlint (линтинг и стиль кода)

---

# Структура проекта

```
kotlin-uml-ksp
│
├── processor
│ ├── UmlDiagram.kt
│ ├── UmlProcessor.kt
│ └── UmlProcessorProvider.kt
│
├── src
│ └── main
│ └── kotlin
│ ├── analysis
│ │ ├── ArchitectureAnalyzer.kt
│ │ ├── ArchitectureReport.kt
│ │ └── LlmClient.kt
│ │
│ ├── uml
│ │ ├── UmlClass.kt
│ │ ├── UmlProject.kt
│ │ ├── UmlDsl.kt
│ │ └── PlantUmlRenderer.kt
│ │
│ ├── Main.kt
│ └── TestClass.kt
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

TestClass --> Order : flow
Order --> User : flow
@enduml

===== AI ANALYSIS =====

Summary: Static architecture analysis + AI insights

Problems:
❌ TestClass is highly coupled
❌ Tight coupling between domain objects

Suggestions:
💡 Reduce coupling using interfaces
💡 Introduce abstraction layer

Data Flow:
➡️ TestClass -> Order -> User
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

Проект может быть расширен в следующих направлениях:

### Визуализация архитектурных проблем

* подсветка проблемных классов прямо в UML-диаграмме
* добавление комментариев (`note`) в PlantUML
* визуальное выделение зон риска (high coupling, cycles и др.)

---

### Генерация архитектурных отчётов

* экспорт результатов анализа в:
   * JSON (для интеграций)
   * HTML (человекочитаемые отчёты)
* сохранение истории изменений архитектуры
* формирование отчётов для CI/CD

---

### Интеграция с IDE

* разработка плагина для:
   * Android Studio
   * IntelliJ IDEA
* просмотр UML-диаграммы прямо в IDE
* автоматическое обновление при сборке проекта

---

### Публикация библиотеки

* оформление проекта как переиспользуемой библиотеки
* публикация в Maven Central
* возможность подключения в любой Kotlin/Android проект

---

# Автор

Анастасия Ципенюк
tg: @Iydyshka_krovopivyshka
