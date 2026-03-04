# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class info.sorbon.hskvocabulary.**$$serializer { *; }
-keepclassmembers class info.sorbon.hskvocabulary.** {
    *** Companion;
}
-keepclasseswithmembers class info.sorbon.hskvocabulary.** {
    kotlinx.serialization.KSerializer serializer(...);
}
