# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep data classes
-keep class com.worldclock.app.data.** { *; }

# Keep UI classes
-keep class com.worldclock.app.ui.** { *; }

# Keep activities
-keep class com.worldclock.app.*Activity { *; }

# Keep adapters
-keep class * extends androidx.recyclerview.widget.RecyclerView$Adapter

# Keep ViewBinding
-keep class * extends androidx.viewbinding.ViewBinding

# Keep Parcelable classes
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep R class
-keep class com.worldclock.app.R$* {
    *;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Room
-keep class androidx.room.** { *; }

# Keep Material Design components
-keep class com.google.android.material.** { *; }

# Keep AndroidX components
-keep class androidx.** { *; }

# Security rules - prevent data leakage
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Удаляем все отладочные символы и информацию
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable

# Удаляем информацию о внутренних классах
-keepattributes !InnerClasses,!EnclosingMethod

# Удаляем информацию о синтетических методах
-keepattributes !Synthetic

# Удаляем информацию о подписях
-keepattributes !Signature

# Удаляем информацию о аннотациях (кроме необходимых)
-keepattributes !RuntimeVisibleAnnotations,!RuntimeVisibleParameterAnnotations

# Удаляем информацию о bridge методах
-keepattributes !Bridge

# Удаляем информацию о varargs
-keepattributes !Varargs

# Удаляем информацию о deprecated
-keepattributes !Deprecated

# Remove debug information in release builds
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Remove System.out.println in release builds
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}

# Obfuscate class names for better security
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic

# Keep only necessary classes for Room database
-keep class com.worldclock.app.data.AppDatabase
-keep class com.worldclock.app.data.Meter
-keep class com.worldclock.app.data.Reading  
-keep class com.worldclock.app.data.Tariff
-keep class com.worldclock.app.data.MeterType

# Security: Remove all debug information
-keepattributes !SourceFile,!LineNumberTable,!LocalVariableTable,!LocalVariableTypeTable,!InnerClasses,!EnclosingMethod,!Synthetic,!Signature,!RuntimeVisibleAnnotations,!RuntimeVisibleParameterAnnotations,!Bridge,!Varargs,!Deprecated

# Security: Obfuscate all class names
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic

# Security: Remove all logging
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Security: Remove System.out.println
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}