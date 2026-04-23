# Vespa ProGuard Rules
# Keep application classes

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Room entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class dev.vskelk.cdf.**$$serializer { *; }
-keepclassmembers class dev.vskelk.cdf.** {
    *** Companion;
}
-keepclasseswithmembers class dev.vskelk.cdf.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Protobuf
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
