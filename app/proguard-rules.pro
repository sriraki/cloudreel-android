# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Streamer settings are saved as JSON with Gson, so their field names must stay stable.
-keepclassmembers class com.cloudreel.mobile.ui.streamer.** { <fields>; }
