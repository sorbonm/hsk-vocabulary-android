# HSK Vocabulary

Android-приложение для изучения китайской лексики по системе HSK (汉语水平考试 — Экзамен по китайскому языку).

---

## Содержание

- [О проекте](#о-проекте)
- [Функциональность](#функциональность)
- [Архитектура](#архитектура)
- [Структура проекта](#структура-проекта)
- [Модели данных](#модели-данных)
- [Экраны](#экраны)
- [Менеджеры и сервисы](#менеджеры-и-сервисы)
- [Поддерживаемые языки](#поддерживаемые-языки)
- [Зависимости](#зависимости)
- [Монетизация](#монетизация)
- [Firebase & Backend](#firebase--backend)
- [Настройка проекта](#настройка-проекта)

---

## О проекте

**HSK Vocabulary** помогает пользователям готовиться к HSK-экзамену с помощью карточек-флеш, тестов и просмотра словарей по уровням. Приложение поддерживает 6 уровней HSK и охватывает более 5000 слов.

**Ключевые особенности:**
- Полная поддержка 6 уровней HSK (150 → 5000+ слов)
- 4 режима квиза с разными типами вопросов
- Интерактивные флеш-карточки с анимацией
- 14 языков для определений
- Рейтинг (Leaderboard) — соревнование с другими игроками по уровням HSK
- Профиль игрока — никнейм, страна, прогресс по уровням, настройки видимости
- Push-уведомления с deep link навигацией
- Офлайн-режим после первой загрузки данных
- Тёмная тема
- Закладки для избранных слов

---

## Функциональность

### Уровни HSK

| Уровень | Слов  | Описание                    |
|---------|-------|-----------------------------|
| HSK 1   | 150   | Базовый (начинающий)        |
| HSK 2   | 150   | Элементарный                |
| HSK 3   | 300   | Средне-начальный            |
| HSK 4   | 600   | Средний                     |
| HSK 5   | 1300  | Продвинутый                 |
| HSK 6   | 2500  | Мастерский                  |

### Режимы квиза

| Тип              | Описание                                     |
|------------------|----------------------------------------------|
| Микс всех        | Случайные вопросы всех типов                 |
| Слушать и угадать| Угадать иероглиф по произношению (TTS)       |
| Язык → Китайский | Перевод с выбранного языка на китайский      |
| Китайский → Язык | Перевод с китайского на выбранный язык       |

- 10 вопросов за раунд
- Рейтинг звёздами (0–3)
- История всех квизов
- Сохранение результатов локально + Firestore (quizLevelAggregates)

### Выбор уровня (Practice Level)

Полноэкранный экран выбора уровня для Quiz / Flashcard / Online Test:
- 6 карточек HSK 1-6 с градиентным фоном по цвету уровня
- Для Quiz: прогресс слов + звёзд на каждой карточке
- Для Flashcard: прогресс слов
- Для Online Test: общее количество слов
- Quiz: иконки Leaderboard и History в top bar
- Quiz: QuizTypeSheet (bottom sheet) после выбора уровня
- Flashcard: FlashcardModeSheet (bottom sheet) после выбора уровня

### Флеш-карточки

- Смахивание карточек
- Режим таймера с настраиваемым интервалом
- Показ/скрытие пиньиня
- Отображение упрощённых и традиционных иероглифов
- Произношение через TTS

### Словари

- Поиск по иероглифу, пиньиню или переводу
- Алфавитные секции
- Закладки
- Детальная информация по каждому слову

### Профиль

- Прогресс по каждому уровню HSK (из локальной БД): X/Y words + progress bar
- MY RANKING — лучший результат (уровень, score, время)
- SETTINGS: Show in leaderboard (toggle), Target HSK level (picker)
- ACCOUNT: Link Google Account (coming soon)

---

## Архитектура

Проект построен на **Clean Architecture** + **MVVM** с Jetpack Compose:

```
Presentation (Compose UI + ViewModel)
    ↕
Domain (UseCases + Repository interfaces + Models)
    ↕
Data (Repository implementations + Room + Firebase)
    ↕
Core (DI, DataStore, Ads, Billing, Utils)
```

**Ключевые решения:**
- **Jetpack Compose** — декларативный UI с Material 3
- **Room** — локальное хранилище (SQLite) всей лексики и результатов
- **DataStore Preferences** — настройки пользователя
- **Hilt** — Dependency Injection
- **Navigation Compose** — навигация между экранами
- **Firebase** (Auth, Firestore, RTDB, Messaging, Crashlytics, Remote Config)
- **Coroutines + Flow** — асинхронность и реактивные потоки данных

---

## Структура проекта

```
android/app/src/main/java/info/sorbon/hskvocabulary/
├── HskVocabularyApp.kt                    # Application class (Hilt)
├── MainActivity.kt                        # Single Activity
│
├── core/
│   ├── ads/
│   │   ├── ConsentManager.kt
│   │   ├── InterstitialAdManager.kt
│   │   └── RewardedAdManager.kt
│   ├── billing/
│   │   └── BillingManager.kt
│   ├── common/
│   │   ├── AdmobAds.kt
│   │   ├── Constants.kt
│   │   └── Result.kt
│   ├── datastore/
│   │   ├── PreferencesKeys.kt
│   │   └── UserPreferences.kt
│   ├── di/
│   │   ├── AppModule.kt
│   │   └── RepositoryModule.kt
│   └── util/
│       ├── AppUpdateManager.kt
│       ├── HskFirebaseMessagingService.kt
│       ├── NetworkMonitor.kt
│       ├── SoundPlayer.kt
│       └── TtsManager.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── HskDatabase.kt
│   │   │   ├── QuizResultDao.kt
│   │   │   └── WordDao.kt
│   │   ├── entity/
│   │   │   ├── QuizResultEntity.kt
│   │   │   └── WordEntity.kt
│   │   └── json/
│   │       └── BundledWordParser.kt
│   ├── remote/firebase/
│   │   ├── AuthDataSource.kt
│   │   ├── FirestoreDataSource.kt
│   │   ├── RealtimeDatabaseSource.kt
│   │   └── RemoteConfigDataSource.kt
│   └── repository/
│       ├── LanguagePackRepositoryImpl.kt
│       ├── QuizResultRepositoryImpl.kt
│       └── WordRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── HskLevel.kt
│   │   ├── Language.kt
│   │   ├── LeaderboardEntry.kt
│   │   ├── PracticeType.kt
│   │   ├── QuizQuestion.kt
│   │   ├── QuizResult.kt
│   │   ├── QuizType.kt
│   │   ├── Rating.kt
│   │   └── Word.kt
│   ├── repository/
│   │   ├── QuizResultRepository.kt
│   │   └── WordRepository.kt
│   └── usecase/
│       ├── GenerateQuizUseCase.kt
│       └── SaveQuizResultUseCase.kt
│
└── presentation/
    ├── navigation/
    │   ├── Screen.kt                      # Все routes
    │   └── HskNavHost.kt                  # Nav graph
    ├── home/
    │   ├── HomeScreen.kt
    │   └── HomeViewModel.kt
    ├── practicelevel/
    │   ├── PracticeLevelScreen.kt         # Выбор уровня (Quiz/Flashcard/OnlineTest)
    │   └── PracticeLevelViewModel.kt
    ├── levelwords/
    │   ├── LevelWordsScreen.kt
    │   ├── LevelWordsViewModel.kt
    │   └── WordDetailSheet.kt
    ├── quiz/
    │   ├── quiz/
    │   │   ├── QuizScreen.kt
    │   │   └── QuizViewModel.kt
    │   ├── quizlevelpart/
    │   │   ├── QuizLevelPartScreen.kt
    │   │   └── QuizLevelPartViewModel.kt
    │   ├── quiztype/
    │   │   └── QuizTypeSheet.kt
    │   ├── result/
    │   │   ├── QuizResultScreen.kt
    │   │   └── QuizResultViewModel.kt
    │   └── history/
    │       ├── QuizHistoryScreen.kt
    │       └── QuizHistoryViewModel.kt
    ├── flashcard/
    │   ├── FlashcardModeScreen.kt
    │   ├── FlashcardModeSheet.kt
    │   ├── FlashcardScreen.kt
    │   └── FlashcardViewModel.kt
    ├── leaderboard/
    │   ├── LeaderboardScreen.kt
    │   ├── LeaderboardViewModel.kt
    │   └── NicknameSetupSheet.kt
    ├── profile/
    │   ├── ProfileScreen.kt
    │   └── ProfileViewModel.kt
    ├── bookmarks/
    │   ├── BookmarksScreen.kt
    │   └── BookmarksViewModel.kt
    ├── language/
    │   ├── LanguageSelectionScreen.kt
    │   └── LanguageSelectionViewModel.kt
    ├── preload/
    │   ├── PreloadScreen.kt
    │   └── PreloadViewModel.kt
    ├── contacts/
    │   └── ContactsScreen.kt
    ├── components/
    │   ├── BannerAdView.kt
    │   ├── EmptyStateView.kt
    │   ├── HskLevelCard.kt
    │   ├── PracticeCard.kt
    │   ├── QuizOptionCard.kt
    │   ├── SectionHeader.kt
    │   ├── StarRatingBar.kt
    │   └── WordListItem.kt
    └── theme/
        ├── Color.kt
        ├── Shape.kt
        ├── Theme.kt
        └── Type.kt
```

---

## Модели данных

### WordEntity (Room)

Хранит каждое слово словаря HSK.

| Поле           | Тип    | Описание                         |
|----------------|--------|----------------------------------|
| `id`           | Int    | Уникальный идентификатор         |
| `level`        | Int    | Уровень HSK (1–6)                |
| `hanzi`        | String | Упрощённый иероглиф              |
| `tradHanzi`    | String | Традиционный иероглиф            |
| `pinyin`       | String | Пиньинь (фонетическая запись)    |
| `cl`           | String | Классификатор (счётное слово)    |
| `definition`   | String | Перевод на английский (базовый)  |
| `definition_ru`| String | Перевод на русский               |
| `definition_*` | String | Переводы на другие языки         |
| `sectionTitle` | String | Заголовок алфавитной секции      |
| `isBookmark`   | Bool   | Флаг закладки пользователя       |

### QuizResultEntity (Room)

Хранит историю прохождения квизов.

| Поле            | Тип    | Описание                      |
|-----------------|--------|-------------------------------|
| `id`            | Long   | Уникальный идентификатор      |
| `quizType`      | String | Тип квиза                     |
| `level`         | Int    | Уровень HSK                   |
| `wordPart`      | Int    | Раздел уровня                 |
| `rating`        | Double | Рейтинг (0.0–3.0)             |
| `duration`      | Int    | Длительность в секундах       |
| `correctAnswer` | Int    | Количество правильных ответов |
| `createDate`    | Long   | Дата прохождения (timestamp)  |

---

## Экраны

### Home
Главный экран с тремя секциями:
- **LEVEL** — выбор уровня HSK (1–6) с отображением рейтинга (сетка 3x2)
- **PRACTICE** — горизонтальная прокрутка: Quiz, Flashcard, Online Test → переход на PracticeLevel
- **OTHER** — Закладки, Рейтинг, Оценить приложение (In-App Review), Контакты

### PracticeLevel (экран выбора уровня)
Полноэкранный список HSK 1-6 для Quiz / Flashcard / Online Test:
- TopAppBar: назад + title + (Quiz: Leaderboard + History)
- 6 карточек с градиентным фоном (`level.backgroundColor`)
- Прогресс: Quiz — слова + звёзды, Flashcard — слова, Online Test — кол-во слов
- Quiz → QuizTypeSheet → QuizLevelPart
- Flashcard → FlashcardModeSheet → Flashcard
- Online Test → Открывает приложение если установлено, иначе Play Store

### LevelWords
Полный список слов выбранного уровня HSK:
- Поиск по иероглифу, пиньиню или переводу
- Алфавитные секции
- Детальная карточка слова (WordDetailSheet)
- Произношение через TTS

### QuizLevelPart
Выбор раздела для квиза:
- Статистика: learned words, got stars, total time (сумма всех попыток)
- Сетка частей с замками (открыты последовательно)
- Звёздный рейтинг для пройденных частей
- Заголовок: "HSK X - Quiz Type (Language)"

### Quiz
Интерактивный квиз с 4 вариантами ответа:
- Прогресс-бар
- Мгновенная обратная связь
- Звуковое произношение (режим "Слушать")
- 10 вопросов за раунд

### QuizResult
Результаты квиза:
- Звёздный рейтинг
- Repeat / Complete / Next

### Flashcard
Карточки с анимацией:
- Режим таймера с настраиваемым интервалом
- Показ/скрытие пиньиня
- Произношение через TTS

### Leaderboard
Рейтинг игроков по уровням HSK:
- Фильтр по уровням (HSK 1-6)
- Топ игроков с никнеймом, страной, очками и временем
- Выделение текущего пользователя
- Кнопка Share — поделиться своим рейтингом в соц. сетях (ссылки на Android и iOS)
- Настройка никнейма (NicknameSetupSheet)

### Profile
Профиль игрока:
- Прогресс по каждому уровню HSK (X/Y words + progress bar) — из локальной БД
- MY RANKING — лучший результат (HSK level, Score, время)
- SETTINGS: Show in leaderboard (toggle), Target HSK level (picker)
- ACCOUNT: Link Google Account (coming soon)

### Language Selection
Управление языковыми пакетами:
- Список 14 языков со статусом загрузки
- Загрузка пакета по требованию
- Индикатор прогресса

### Bookmarks
Избранные слова с возможностью поиска и удаления.

### Contacts
Контактная информация разработчика:
- Текст-призыв к обратной связи (баги, идеи, рекомендации)
- Email, Facebook, Instagram, Telegram, WeChat
- Версия приложения

### Preload
Начальная загрузка данных при первом запуске.

---

## Менеджеры и сервисы

| Компонент                 | Назначение                                      |
|---------------------------|-------------------------------------------------|
| Room (HskDatabase)        | Локальная БД: WordDao, QuizResultDao            |
| UserPreferences           | DataStore — настройки пользователя              |
| AuthDataSource            | Firebase Auth (анонимная авторизация)            |
| FirestoreDataSource       | Firestore: профили, рейтинг, агрегаты           |
| RealtimeDatabaseSource    | Firebase RTDB: загрузка слов                    |
| RemoteConfigDataSource    | Remote Config: лимиты, версии, max duration     |
| ConsentManager            | UMP GDPR consent для AdMob                      |
| AppUpdateManager          | Force update (In-App Update) + soft update dialog|
| GenerateQuizUseCase       | Генерация вопросов для квиза                    |
| SaveQuizResultUseCase     | Сохранение результата + синхронизация           |
| TtsManager                | TTS — произношение                              |
| SoundPlayer               | Звуковые эффекты                                |
| NetworkMonitor            | ConnectivityManager — отслеживание сети         |
| RewardedAdManager         | Rewarded ads для повторного прохождения         |
| InterstitialAdManager     | Межстраничная реклама                           |
| BillingManager            | Google Play Billing — покупки                   |
| HskFirebaseMessagingService | FCM — push-уведомления (topic: news)          |

---

## Поддерживаемые языки

| Язык         | Код | Флаг |
|--------------|-----|------|
| Английский   | en  | 🇬🇧   |
| Русский      | ru  | 🇷🇺   |
| Таджикский   | tj  | 🇹🇯   |
| Немецкий     | de  | 🇩🇪   |
| Французский  | fr  | 🇫🇷   |
| Японский     | jp  | 🇯🇵   |
| Арабский     | ar  | 🇸🇦   |
| Испанский    | es  | 🇪🇸   |
| Итальянский  | it  | 🇮🇹   |
| Кхмерский    | km  | 🇰🇭   |
| Корейский    | ko  | 🇰🇷   |
| Португальский| pt  | 🇧🇷   |
| Тайский      | th  | 🇹🇭   |
| Вьетнамский  | vi  | 🇻🇳   |

---

## Зависимости

| Библиотека                | Назначение                              |
|---------------------------|-----------------------------------------|
| Jetpack Compose + Material 3 | Декларативный UI                    |
| Navigation Compose        | Навигация между экранами                |
| Hilt                      | Dependency Injection                    |
| Room                      | Локальная SQLite база данных            |
| DataStore Preferences     | Настройки пользователя                  |
| Firebase Database         | Realtime Database — источник данных     |
| Firebase Firestore        | Профили, рейтинг, агрегаты              |
| Firebase Auth             | Анонимная аутентификация                |
| Firebase Messaging        | Push-уведомления (FCM)                  |
| Firebase Analytics        | Аналитика и события                     |
| Firebase Crashlytics      | Отслеживание сбоев                      |
| Firebase AppCheck         | Play Integrity — защита API             |
| Firebase Remote Config    | Удалённая конфигурация                  |
| Google Mobile Ads (AdMob) | Баннерная и rewarded реклама            |
| Play Billing              | Встроенные покупки                      |
| Play Review               | In-App Review                           |
| Play Update               | In-App Update                           |
| Kotlin Coroutines + Flow  | Асинхронность и реактивные потоки       |
| Kotlin Serialization      | JSON сериализация                       |

---

## Монетизация

### Реклама
- **UMP GDPR Consent** — запрос согласия у пользователей из EEA перед показом рекламы
- **Баннер** — отображается на главном экране
- **Rewarded** — показывается для повторного прохождения квиза
- **Межстраничная** — показывается между сессиями

### Встроенные покупки
- **Удаление рекламы** — одноразовая покупка отключает все объявления
- Google Play Billing
- Поддержка восстановления покупок

---

## Firebase & Backend

### Realtime Database
- Хранит весь словарный запас по уровням и языкам
- Версионирование данных для детектирования обновлений
- Анонимная аутентификация пользователей

### Remote Config

| Ключ                          | Тип    | Дефолт | Описание                                      |
|-------------------------------|--------|--------|-----------------------------------------------|
| `leaderboard_limit`           | Long   | 50     | Лимит записей в рейтинге                      |
| `quiz_max_duration_seconds`   | Long   | 600    | Максимальное время квиза (10 мин)             |
| `android_latest_version`      | String | 1.0.0  | Последняя версия для soft update диалога      |
| `android_min_version`         | String | 1.0.0  | Минимальная версия для force update            |
| `latest_version`              | String | —      | (iOS) Последняя версия                         |
| `min_version`                 | String | —      | (iOS) Минимальная версия                       |

### Firestore

| Коллекция                    | Описание                                           |
|------------------------------|-----------------------------------------------------|
| `users/{uid}`                | Профиль: nickname, country, platform, isPublic, fcmToken |
| `quizLevelAggregates/{uid_level}` | Агрегированные результаты для рейтинга         |

### Структура данных в Realtime Database

```
root/
├── version/           # Версия базы данных
├── hsk1/              # Слова уровня 1
│   ├── en/            # Английские определения
│   ├── ru/            # Русские определения
│   └── ...
├── hsk2/ ... hsk6/    # Аналогично для уровней 2-6
```

---

## Настройка проекта

**Требования:** Android Studio Hedgehog+, Min SDK 26, Target SDK 35, JDK 17

```bash
git clone <repository-url>
cd HSK-Vocabulary/android
```

1. Добавить `google-services.json` из Firebase Console в `app/`
2. Открыть в Android Studio
3. Sync Gradle и запустить

### Ключи DataStore

| Ключ                    | Тип     | Описание                          |
|-------------------------|---------|-----------------------------------|
| `current_language`      | String  | Текущий язык интерфейса           |
| `hsk_word_preloaded`    | Boolean | Флаг первичной загрузки данных    |
| `removed_ads`           | Boolean | Флаг покупки удаления рекламы     |
| `downloaded_languages`  | Set     | Список загруженных языков         |
| `target_hsk_level`      | Int     | Целевой уровень HSK               |
| `user_is_public`        | Boolean | Видимость в рейтинге              |
| `last_quiz_type`        | String  | Последний тип квиза               |
| `data_version`          | Int     | Версия локальных данных           |

---

## Навигация

| Route                                    | Экран              | Аргументы                      |
|------------------------------------------|--------------------|--------------------------------|
| `home`                                   | HomeScreen         | —                              |
| `practice_level/{type}`                  | PracticeLevelScreen| type: quiz/flashcard/online_test|
| `level_words/{level}`                    | LevelWordsScreen   | level: Int                     |
| `quiz_level_part/{level}/{quizType}`     | QuizLevelPartScreen| level: Int, quizType: String   |
| `quiz/{level}/{part}/{startWordId}/{quizType}` | QuizScreen  | level, part, startWordId, quizType |
| `quiz_result/...`                        | QuizResultScreen   | level, part, quizType, correct, total, duration |
| `quiz_history`                           | QuizHistoryScreen  | level?, quizType? (optional)   |
| `flashcard_mode/{level}`                 | FlashcardModeScreen| level: Int                     |
| `flashcard/{level}/{mode}`               | FlashcardScreen    | level: Int, mode: String       |
| `leaderboard`                            | LeaderboardScreen  | —                              |
| `profile`                                | ProfileScreen      | —                              |
| `language_selection`                     | LanguageSelectionScreen | —                          |
| `bookmarks`                              | BookmarksScreen    | —                              |
| `contacts`                               | ContactsScreen     | —                              |

---

## App Update

Два механизма обновления приложения:

| Механизм       | Условие                                | Поведение                                              |
|----------------|----------------------------------------|--------------------------------------------------------|
| Force Update   | `android_min_version` > текущая версия | Google Play IMMEDIATE update — блокирует приложение   |
| Soft Update    | `android_latest_version` > текущая     | AlertDialog с кнопками "Update" / "Later"              |

Версии управляются через Firebase Remote Config (отдельно от iOS).

---

## Deep Link

| Scheme              | Описание                                           |
|---------------------|----------------------------------------------------|
| `hskvocabulary://`  | Custom URL scheme — открывает приложение           |

### Online Test — открытие внешних приложений

При нажатии на Online Test уровня:
1. Если приложение установлено (`info.sorbon.hsk_X_online_test`) → открывает его
2. Если не установлено → открывает Play Store

Для Android 11+ в манифесте объявлены `<queries>` для видимости пакетов.

---

## Push Notifications

FCM topic: `news` (подписка при запуске, общий для iOS и Android).

## Deep Link / Push Notifications

| `screen`       | Доп. параметры                 | Действие                            |
|----------------|--------------------------------|-------------------------------------|
| `quiz`         | `level` (опц.)                | Открыть выбор уровня квиза          |
| `flashcard`    | `level` (опц.)                | Открыть выбор уровня флеш-карточек  |
| `words`        | `level` (обяз.)               | Открыть список слов уровня          |
| `leaderboard`  | `level` (опц.)                | Открыть рейтинг                     |
| `profile`      | —                              | Открыть профиль                     |
| `bookmarks`    | —                              | Открыть закладки                    |
| `history`      | —                              | Открыть историю квизов              |
| `update`       | `alert_title`, `alert_message`| Алерт с кнопкой Update → Play Store |
| `news`         | `alert_title`, `alert_message`, `action` | Информационный алерт      |
