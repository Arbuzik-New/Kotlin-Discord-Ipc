# DiscordIPC for Kotlin

Kotlin библиотека для работы с Discord по IPC.\
Библиотека протестирована на Linux (Windows Coming Soon)

## Возможности
1. Создание активности
2. Получение id
3. Получение username
4. Получение display name
5. Получение premium type
6. Получение ссылки на аватар

## Требования
1. Kotlin 1.8+
2. Discord
3. [Application Id](https://discord.com/developers/applications)

## Gradle (Kotlin SDL)
```markdown
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.Arbuzik-New:Kotlin-Discord-Ipc:1.0.0")
}
```

## Gradle (Groovy)
```markdown
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Arbuzik-New:Kotlin-Discord-Ipc:1.0.0'
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
    <version>1.0.0</version>
</dependency>
```

## Examples
### Подключение к IPC
```markdown
DiscordIPC.start("application id")
```

### Отключение IPC
```markdown
DiscordIPC.stop()
```

### Получение данных пользователя
```markdown
User.id - id пользователя
User.username - username пользователя
User.globalName - отображаемое имя пользователя
User.avatar - название аватара пользователя
User.premiumType - premium type пользователя
User.avatarLink() - ссылка на аватар пользователя
```

### Установка статуса
```markdown
DiscordIPC.setRPC(
    ActivityPayload(
        state = "State",
        stateUrl = "State Url",
        details = "Details",
        detailsUrl = "Details Url",
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
DiscordIPC.setRPC(null)
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
2. Убедитесь, что подождали получение данных пользователя после запуска DiscordIPC `User.wait()`

### RPC не работает
1. Убедитесь в правильности Application Id
2. Убедитесь в правильности указанных полей

### java.net.ConnectException: В соединении отказано
1. Убедитесь что дискорд запущен
2. Перезапустите дискорд

### java.io.IOException: Обрыв канала
1. Убедитесь в правильности Application Id
