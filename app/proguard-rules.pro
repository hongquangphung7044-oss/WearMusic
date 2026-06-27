# ProGuard rules for WearMusic

# Keep song models (used for serialization)
-keep class com.example.wearmusic.data.model.** { *; }
-keep class com.example.wearmusic.data.remote.** { *; }

# Media3
-keep class androidx.media3.** { *; }

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# Hilt
-keep class * extends dagger.hilt.android.HiltActivity { *; }
