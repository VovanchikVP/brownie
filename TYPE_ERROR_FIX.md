# Исправление ошибки типов данных

## 🚨 Проблема

Ошибка компиляции:
```
The floating-point literal does not conform to the expected type Long
```

## 🔍 Причина

Ошибка возникала из-за неправильного использования позиционных параметров в конструкторах Room Entity классов. Kotlin компилятор не мог правильно определить типы параметров.

## ✅ Решение

### **Использование именованных параметров**

Заменили позиционные параметры на именованные для лучшей читаемости и избежания ошибок типов:

#### **Было:**
```kotlin
repository.insertReading(Reading(electricityMeterId, 1250.5, currentTime))
repository.insertTariff(Tariff(electricityMeterId, 4.5, tariffStartDate, null))
```

#### **Стало:**
```kotlin
repository.insertReading(Reading(meterId = electricityMeterId, value = 1250.5, date = currentTime))
repository.insertTariff(Tariff(meterId = electricityMeterId, rate = 4.5, startDate = tariffStartDate, endDate = null))
```

## 📋 Изменения в коде

### **MainActivity.kt**

1. **Создание показаний:**
   ```kotlin
   // Было
   repository.insertReading(Reading(electricityMeterId, 1250.5, currentTime))
   
   // Стало
   repository.insertReading(Reading(meterId = electricityMeterId, value = 1250.5, date = currentTime))
   ```

2. **Создание тарифов:**
   ```kotlin
   // Было
   repository.insertTariff(Tariff(electricityMeterId, 4.5, tariffStartDate, null))
   
   // Стало
   repository.insertTariff(Tariff(meterId = electricityMeterId, rate = 4.5, startDate = tariffStartDate, endDate = null))
   ```

## 💡 Преимущества именованных параметров

1. **Читаемость** - код становится более понятным
2. **Безопасность** - меньше ошибок с типами данных
3. **Поддержка** - легче изменять код в будущем
4. **Отладка** - проще найти ошибки

## 🔧 Типы данных в Entity классах

### **Reading**
```kotlin
data class Reading(
    val id: Long = 0,           // Автоинкремент
    val meterId: Long,          // Связь с прибором учета
    val value: Double,          // Значение показания
    val date: Long              // Дата (timestamp)
)
```

### **Tariff**
```kotlin
data class Tariff(
    val id: Long = 0,           // Автоинкремент
    val meterId: Long,          // Связь с прибором учета
    val rate: Double,           // Значение тарифа
    val startDate: Long,        // Дата начала действия
    val endDate: Long?          // Дата окончания (null = действует)
)
```

### **Meter**
```kotlin
data class Meter(
    val id: Long = 0,           // Автоинкремент
    val number: String,         // Номер прибора
    val address: String,        // Адрес установки
    val type: MeterType         // Тип прибора
)
```

## 🚀 Проверка исправления

После применения исправлений:

1. ✅ Проект компилируется без ошибок
2. ✅ Типы данных корректно определяются
3. ✅ Код становится более читаемым
4. ✅ Приложение работает корректно

## 📚 Рекомендации

### **Всегда используйте именованные параметры для:**
- Room Entity классов
- Сложных конструкторов
- Методов с множественными параметрами
- Когда типы параметров могут быть неочевидными

### **Пример хорошего стиля:**
```kotlin
val reading = Reading(
    meterId = meterId,
    value = 1250.5,
    date = System.currentTimeMillis()
)
```

### **Избегайте:**
```kotlin
val reading = Reading(meterId, 1250.5, System.currentTimeMillis())
```