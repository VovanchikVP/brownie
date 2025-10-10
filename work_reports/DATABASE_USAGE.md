# Использование базы данных SQLite в World Clock

## 📊 Структура базы данных

Приложение использует Room Database с тремя основными сущностями:

### 1. **Прибор учета (Meter)**
```kotlin
@Entity(tableName = "meters")
data class Meter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val number: String,           // Номер прибора учета
    val address: String,          // Адрес установки
    val type: MeterType          // Тип прибора учета
)
```

**Типы приборов:**
- `ELECTRICITY` - Электричество
- `GAS` - Газ
- `HOT_WATER` - Горячая вода
- `COLD_WATER` - Холодная вода

### 2. **Показания (Reading)**
```kotlin
@Entity(tableName = "readings")
data class Reading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,           // Связь с прибором учета
    val value: Double,           // Значение показания
    val date: Long              // Дата показания (timestamp)
)
```

### 3. **Тариф (Tariff)**
```kotlin
@Entity(tableName = "tariffs")
data class Tariff(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val meterId: Long,           // Связь с прибором учета
    val rate: Double,            // Значение тарифа (руб/ед.изм.)
    val startDate: Long,         // Дата начала действия
    val endDate: Long?           // Дата окончания (null если действует)
)
```

## 🔧 Основные операции

### Добавление прибора учета
```kotlin
val meterId = repository.insertMeter(
    Meter(
        number = "EL-001-2024",
        address = "ул. Ленина, д. 10, кв. 5",
        type = MeterType.ELECTRICITY
    )
)
```

### Добавление показаний
```kotlin
repository.insertReading(
    Reading(
        meterId = meterId,
        value = 1250.5,
        date = System.currentTimeMillis()
    )
)
```

### Добавление тарифа
```kotlin
repository.insertTariff(
    Tariff(
        meterId = meterId,
        rate = 4.5,
        startDate = System.currentTimeMillis(),
        endDate = null  // Действует бессрочно
    )
)
```

### Получение данных

#### Все приборы учета
```kotlin
repository.getAllMeters().collect { meters ->
    meters.forEach { meter ->
        println("${meter.number} - ${meter.type.displayName}")
    }
}
```

#### Показания по прибору
```kotlin
repository.getReadingsByMeterId(meterId).collect { readings ->
    readings.forEach { reading ->
        println("${reading.value} - ${Date(reading.date)}")
    }
}
```

#### Текущий тариф
```kotlin
val currentTariff = repository.getCurrentTariffByMeterId(meterId)
if (currentTariff != null) {
    println("Текущий тариф: ${currentTariff.rate} руб/ед.изм.")
}
```

## 📱 Примеры использования в приложении

### Инициализация базы данных
```kotlin
private fun initializeDatabase() {
    database = AppDatabase.getDatabase(this)
    repository = MeterRepository(
        database.meterDao(),
        database.readingDao(),
        database.tariffDao()
    )
}
```

### Добавление тестовых данных
```kotlin
private fun addSampleData() {
    lifecycleScope.launch {
        // Добавляем приборы учета
        val electricityMeterId = repository.insertMeter(
            Meter(
                number = "EL-001-2024",
                address = "ул. Ленина, д. 10, кв. 5",
                type = MeterType.ELECTRICITY
            )
        )
        
        // Добавляем показания
        repository.insertReading(
            Reading(electricityMeterId, 1250.5, System.currentTimeMillis())
        )
        
        // Добавляем тариф
        repository.insertTariff(
            Tariff(electricityMeterId, 4.5, System.currentTimeMillis(), null)
        )
    }
}
```

## 🔍 Полезные запросы

### Получение последних показаний
```kotlin
val latestReading = repository.getLatestReadingByMeterId(meterId)
```

### Получение активного тарифа
```kotlin
val activeTariff = repository.getActiveTariffByMeterId(meterId)
```

### Получение приборов по типу
```kotlin
repository.getMetersByType(MeterType.ELECTRICITY).collect { meters ->
    // Все электрические счетчики
}
```

## 📋 Тестовые данные

При запуске приложения автоматически добавляются:

1. **4 прибора учета:**
   - Электричество: EL-001-2024
   - Газ: GAS-002-2024
   - Горячая вода: HW-003-2024
   - Холодная вода: CW-004-2024

2. **Показания для каждого прибора**

3. **Тарифы для каждого прибора**

## 🚀 Запуск и тестирование

1. Запустите приложение
2. Откройте Logcat в Android Studio
3. Найдите сообщения с префиксом "=== ПРИБОРЫ УЧЕТА ==="
4. Увидите информацию о всех добавленных данных

## 💡 Возможности расширения

- Добавление UI для управления приборами
- Экспорт данных в CSV/Excel
- Расчет потребления между показаниями
- Уведомления о необходимости передачи показаний
- Графики потребления