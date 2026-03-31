-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.wordbook.data.** { *; }
-keep class com.wordbook.domain.model.** { *; }
