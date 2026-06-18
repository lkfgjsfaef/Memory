# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keep class com.niit.memory.data.model.** { *; }
-keepclassmembers class com.niit.memory.data.model.** { *; }

# Coil
-dontwarn coil.**

# DataStore (RxJava3)
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# RxJava3
-dontwarn io.reactivex.rxjava3.**

# OSMDroid
-dontwarn org.osmdroid.**

# Qiniu
-keep class com.qiniu.** { *; }
-dontwarn com.qiniu.**

# ExoPlayer / Media3
-dontwarn androidx.media3.**

# ViewModel and LiveData
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
