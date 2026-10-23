# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room generated implementations, entities and DAO annotations so the
# database keeps working after R8 shrinks and obfuscates the app.
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-keepattributes *Annotation*
-dontwarn androidx.room.paging.**
