# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.yalantis.ucrop**

# uCrop 2.2.11 calls Activity.enableEdgeToEdge(), whose AndroidX implementation still contains
# Android 15-deprecated system bar APIs. The crop screen already handles its own inset padding, so
# release builds can drop this helper call and avoid Play Console edge-to-edge warnings.
-assumenosideeffects class androidx.activity.EdgeToEdge {
    public static void enable(androidx.activity.ComponentActivity, androidx.activity.SystemBarStyle, androidx.activity.SystemBarStyle);
}

# Material 1.14 guards these helpers on API 35+, but stripping the color writes entirely keeps the
# release artifact free of deprecated system-bar color calls.
-assumenosideeffects class com.google.android.material.internal.EdgeToEdgeUtils {
    public static void setStatusBarColor(android.view.Window, int);
    public static void setNavigationBarColor(android.view.Window, int);
}
