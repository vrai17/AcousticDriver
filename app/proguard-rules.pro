# Proguard rules for Acoustic Driver
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
