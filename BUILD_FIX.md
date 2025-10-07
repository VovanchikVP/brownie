# Решение проблемы сборки KAPT

## 🚨 Проблема

Ошибка `IllegalAccessError` возникает из-за несовместимости KAPT (Kotlin Annotation Processing Tool) с современными версиями Java. KAPT пытается получить доступ к внутренним классам Java компилятора, которые недоступны в новых версиях Java.

## ✅ Решение

### 1. **Замена KAPT на KSP**

KSP (Kotlin Symbol Processing) - это современная альтернатива KAPT, которая:
- Быстрее работает
- Лучше совместима с современными версиями Java
- Поддерживается Google и JetBrains

### 2. **Изменения в конфигурации**

#### **build.gradle (корневой)**
```gradle
plugins {
    id 'com.android.application' version '8.2.0' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.20' apply false
    id 'com.google.devtools.ksp' version '1.9.20-1.0.14' apply false
}
```

#### **app/build.gradle**
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.devtools.ksp'  // Заменили kotlin-kapt
}

dependencies {
    // Room database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    ksp 'androidx.room:room-compiler:2.6.1'  // Заменили kapt на ksp
}
```

### 3. **Что изменилось**

- ❌ `id 'kotlin-kapt'` → ✅ `id 'com.google.devtools.ksp'`
- ❌ `kapt 'androidx.room:room-compiler:2.6.1'` → ✅ `ksp 'androidx.room:room-compiler:2.6.1'`
- ❌ Настройки KAPT → ✅ Удалены (не нужны для KSP)

## 🔧 Альтернативные решения

### **Если KSP не работает:**

1. **Понижение версии Java**
   ```bash
   # Используйте Java 11 вместо Java 17+
   export JAVA_HOME=/path/to/java11
   ```

2. **Настройки JVM для KAPT**
   ```properties
   # В gradle.properties
   kapt.use.worker.api=false
   kapt.incremental.apt=false
   ```

3. **Обновление версий**
   ```gradle
   // Используйте более новые версии
   implementation 'androidx.room:room-runtime:2.6.1'
   kapt 'androidx.room:room-compiler:2.6.1'
   ```

## 🚀 Инструкция по применению

### **Шаг 1: Очистка проекта**
```bash
cd world_clock
./gradlew clean
```

### **Шаг 2: Синхронизация**
- Откройте Android Studio
- Нажмите "Sync Now" или "Sync Project with Gradle Files"

### **Шаг 3: Пересборка**
```bash
./gradlew build
```

### **Шаг 4: Запуск**
- Подключите устройство или запустите эмулятор
- Нажмите "Run" в Android Studio

## 📋 Проверка

После применения исправлений:

1. ✅ Проект синхронизируется без ошибок
2. ✅ Сборка проходит успешно
3. ✅ Приложение запускается
4. ✅ База данных Room работает корректно

## 💡 Преимущества KSP

- **Скорость**: В 2-3 раза быстрее KAPT
- **Совместимость**: Работает с современными версиями Java
- **Стабильность**: Меньше ошибок сборки
- **Поддержка**: Активно развивается Google и JetBrains

## 🔍 Диагностика

### **Если проблема остается:**

1. **Проверьте версию Java**
   ```bash
   java -version
   ```

2. **Проверьте версию Gradle**
   ```bash
   ./gradlew --version
   ```

3. **Очистите кэш**
   ```bash
   ./gradlew clean
   rm -rf .gradle
   ```

4. **Пересоздайте проект**
   - Удалите папку `.gradle`
   - Переимпортируйте проект в Android Studio

## 📚 Дополнительная информация

- [KSP Documentation](https://kotlinlang.org/docs/ksp-overview.html)
- [Room with KSP](https://developer.android.com/training/data-storage/room)
- [Migration from KAPT to KSP](https://kotlinlang.org/docs/ksp-migration.html)