# DiscordIPC for Kotlin

Kotlin библиотека для работы с Discord по IPC.\
Библиотека протестирована на Linux, Windows

## Возможности
1. Создание активности
2. Получение id
3. Получение username
4. Получение display name
5. Получение premium type
6. Получение ссылки на аватар

## Требования
1. Discord
2. [Application Id](https://discord.com/developers/applications)

## Gradle (Kotlin SDL)
```markdown
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Arbuzik-New:Kotlin-Discord-Ipc:1.0.5")
}
```

## Gradle (Groovy)
```markdown
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Arbuzik-New:Kotlin-Discord-Ipc:1.0.5'
}
```

## Maven
```markdown
<repository>
	<id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.Arbuzik-New</groupId>
    <artifactId>Kotlin-Discord-Ipc</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Examples
### Подключение к IPC
```markdown
val ipc = DiscordIPC("application id")
```

### Отключение IPC
```markdown
ipc.stop()
```

### Получение данных пользователя
```markdown
User.id - id пользователя
User.username - username пользователя
User.globalName - отображаемое имя пользователя
User.avatar - название аватара пользователя
User.premiumType - premium type пользователя
User.avatarLink() - ссылка на аватар пользователя
User.bytes - массив байтов аватара пользователя
User.downloadAvatar() - скачать аватар пользователя
```

### Установка статуса
```markdown
ipc.setRPC(
    ActivityPayload(
        details = "Details",
        detailsUrl = "Details Url",
        state = "State",
        stateUrl = "State Url",
        timestamps = TimestampsPayload(System.currentTimeMillis()),
        assets = AssetsPayload(
            large_image = "url or id",
            large_text = "large image text",
            large_url = "large image url",
            small_image = "url or id",
            small_text = "small image text",
            small_url = "small image url"
        )
    )
)
```

### Отключение статуса
```markdown
ipc.setRPC(null)
```

## Forking
### Клонирование
```markdown
git clone https://github.com/Arbuzik-New/Kotlin-Discord-Ipc.git
```

### Сборка
```markdown
./gradlew build
```

## Получение Application Id
1. Перейдите в [Discord Developer Portal](https://discord.com/developers/applications)
2. Создайте новое приложение
3. Скопируйте Application Id из раздела General Information

## Проблемы
### Поля объекта User пустые
1. Убедитесь, что запустили DiscordIPC
2. Убедитесь, что подождали получение данных пользователя после запуска DiscordIPC `DiscordIPC.wait()`
3. Попробуйте ПОЛНОСТЬЮ перезапустить Discord

### User.bytes пустое
1. Убедитесь что вызвали User.downloadAvatar()

### RPC не работает
1. Убедитесь в правильности Application Id
2. Убедитесь в правильности указанных полей
3. Убедитесь, что подождали handshake после запуска DiscordIPC `DiscordIPC.wait()`
4. Попробуйте ПОЛНОСТЬЮ перезапустить Discord

### java.net.ConnectException: В соединении отказано
1. Убедитесь что дискорд запущен
2. Перезапустите дискорд

### java.io.IOException: Обрыв канала
1. Убедитесь в правильности Application Id
