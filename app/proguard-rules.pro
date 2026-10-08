# Proguard rules for TwinSpace

# Keep AndroidX & Compose annotations
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# Keep Device Admin Receiver & Manifest components
-keep public class com.twinspace.cloner.admin.TwinDeviceAdminReceiver { *; }
-keep public class com.twinspace.cloner.CloneInstallerActivity { *; }
-keep public class com.twinspace.cloner.MainActivity { *; }
-keep public class com.twinspace.cloner.TwinSpaceApp { *; }

# Keep data models used in serialization & state
-keep class com.twinspace.cloner.core.CloneRecord { *; }
-keep class com.twinspace.cloner.core.CloneSettings { *; }
-keep class com.twinspace.cloner.core.AppItem { *; }

# Jetpack Compose runtime rules
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-dontwarn androidx.compose.**
