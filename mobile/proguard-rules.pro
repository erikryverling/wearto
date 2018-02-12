# Entities
-keep class se.yverling.wearto.app.entities.** { *; }
-keep class se.yverling.wearto.sync.dtos.** { *; }


# -- Third party dependencies

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkIO
-dontwarn okio.**

# Dagger
-dontwarn com.google.errorprone.annotations.*