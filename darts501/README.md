# Darts 501 — Android (Kotlin + Jetpack Compose)

Игра 501 на двух человек с голосовым вводом.

## Сборка

1. Android Studio → **Open** → выбрать папку `Darts501`.
2. Если нет папки `gradle/wrapper/gradle-wrapper.jar` — Android Studio сама предложит
   скачать Gradle 8.9 (или выполнить `gradle wrapper` один раз).
3. Run на устройстве (не эмуляторе — нужен реальный микрофон и Google-распознавание).

SDK: compileSdk 35, minSdk 24, Kotlin 2.0.21, AGP 8.7.2.

## Файлы

```
settings.gradle.kts
build.gradle.kts
gradle.properties
gradle/wrapper/gradle-wrapper.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/java/com/example/darts501/MainActivity.kt
app/src/main/java/com/example/darts501/DartsScreen.kt     — весь UI
app/src/main/java/com/example/darts501/GameViewModel.kt   — правила 501
app/src/main/java/com/example/darts501/VoiceController.kt — непрерывный микрофон
app/src/main/java/com/example/darts501/VoiceParser.kt     — разбор речи
```

## Голосовой ввод

- Кнопка микрофона внизу включает непрерывное прослушивание.
- Команда: **«Счёт сто сорок»**, **«Счёт 60»**, **«Счёт двадцать шесть»**.
- Число появляется на весь экран → скажите **«Да»** (засчитать) или **«Нет»** (отменить),
  либо нажмите кнопки ДА / НЕТ.
- Синонимы «да»: ага, верно, точно, ок. Синонимы «нет»: не, отмена, неверно.
- Распознавание — ru-RU, требует Google-приложение распознавания речи на устройстве.

## Правила, которые реализованы

- Старт 501, ввод суммы трёх дротиков (0–180).
- Перебор (остаток < 0 или = 1) — очки не меняются, ход переходит.
- Выход только точно в 0.
- Невозможные суммы (179, 178, 176, 175, 173, 172, 171, 169) отклоняются.
- Леги, смена начинающего каждый лег, матч до N легов (настройки).
- ma — средний за матч (на 3 дротика), la — средний за текущий лег.
- Undo — отмена последнего броска (стек на 100 шагов).

## Известные ограничения

- При сворачивании приложения микрофон выключается — включить заново кнопкой.
- Двойной выход (double out) не проверяется: вводится сумма, а не отдельные дротики.
- Иконка приложения — системная по умолчанию (mipmap не добавлял).
"# Darts501" 
