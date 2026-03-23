# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep SSH-related classes
-keep class org.apache.sshd.** { *; }
-keep class com.sshpad.app.ssh.** { *; }

# Keep data models
-keep class com.sshpad.app.data.model.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Koin
-keep class org.koin.** { *; }

# DataStore
-keep class androidx.datastore.** { *; }
