# Kotlin UML Generator (KSP)

Проект демонстрирует использование **Kotlin Symbol Processing (KSP)** для автоматического анализа Kotlin-кода и генерации UML-описаний классов.

Во время компиляции аннотированные классы обрабатываются кастомным **KSP-процессором**, который генерирует API для получения UML-модели класса.

Полученная модель может быть преобразована в **PlantUML-диаграмму**.

---

# Текущий этап реализации

На текущем этапе проекта реализована базовая инфраструктура анализа кода.

Процесс включает:

1. Поиск классов, помеченных аннотацией `@UmlDiagram`
2. Анализ структуры класса во время компиляции
3. Генерацию Kotlin-API для получения UML-модели
4. Преобразование модели в PlantUML через DSL

В отличие от первой версии проекта, UML-файлы **не генерируются напрямую**.
Вместо этого создаётся API:

```
TestClass.uml()
```

которое возвращает структуру UML-диаграммы.

---

# Архитектура решения

Проект построен из нескольких уровней:

```
Kotlin Source Code
        │
        ▼
KSP Processor
        │
        ▼
Generated API
(TestClass.uml())
        │
        ▼
UmlClass (модель UML)
        │
        ▼
UmlDsl
        │
        ▼
PlantUML текст
```

Таким образом, анализ кода отделён от визуализации диаграммы.

---

# Используемые технологии

* **Kotlin**
* **Gradle**
* **KSP (Kotlin Symbol Processing)**
* **PlantUML**
* **ktlint** (проверка стиля кода)

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
    val name: String,
    val age: Int
) {
    fun method1() {}
    fun method2() {}
}
```

После компиляции появляется API:

```kotlin
val uml = TestClass("", 0).uml()
```

---

# Генерация PlantUML

UML-модель можно преобразовать в PlantUML:

```kotlin
val diagram = PlantUmlRenderer.render(uml)
println(diagram)
```

Результат:

```
@startuml
class TestClass {
  -name : String
  -age : Int

  +method1()
  +method2()
}
@enduml
```

---

# Запуск проекта

Собрать проект:

```
./gradlew build
```

Запустить пример:

```
./gradlew run
```

---

# Планируемое развитие проекта

Следующие этапы проекта:

### 1. Анализ зависимостей между классами

Определение:

* зависимостей между классами
* связей между объектами
* взаимодействия между компонентами

### 2. Генерация полной UML-диаграммы проекта

Автоматическое построение:

* class diagrams
* dependency graphs

### 3. Использование LLM для анализа архитектуры

На основе UML-модели планируется использовать LLM для:

* анализа архитектуры проекта
* выявления сильно связанных компонентов
* определения потоков данных между классами
* предложения улучшений архитектуры

---

# Автор

Анастасия Ципенюк
тг @Iydyshka_krovopivyshka