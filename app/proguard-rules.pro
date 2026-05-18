# Add project specific ProGuard rules here.

# Garder les infos de stack utiles en prod
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# HERE Maps SDK (copié depuis heresdk AAR proguard.txt)
-keep public class com.here.NativeBase { *; }
-keep class com.here.NativeBase$* { *; }
-keep interface com.here.NativeBase$* { *; }
-keep class com.here.sdk.** { *; }
-keep class com.here.services.** { *; }
-keep class com.here.odnp.** { *; }
-keep class com.here.posclient.** { *; }
-keep class com.here.network.** { *; }
-keep class com.here.olp.** { *; }
-keep class com.here.annotations.** { *; }
-keep public class com.here.sdk.** { *; }
-keep public class com.here.services.** { *; }
-keep public class com.here.odnp.** { *; }
-keep public class com.here.posclient.** { *; }
-keep public class com.here.network.** { *; }
-keep public class com.here.olp.** { *; }
-keep public class com.here.annotations.** { *; }
-keep public enum com.here.sdk.** { *; }
-keep public enum com.here.services.** { *; }
-keep public enum com.here.odnp.** { *; }
-keep public enum com.here.posclient.** { *; }
-keep public enum com.here.network.** { *; }
-keep public enum com.here.olp.** { *; }
-keep public enum com.here.annotations.** { *; }
-keep public interface com.here.sdk.** { *; }
-keep public interface com.here.services.** { *; }
-keep public interface com.here.odnp.** { *; }
-keep public interface com.here.posclient.** { *; }
-keep public interface com.here.network.** { *; }
-keep public interface com.here.olp.** { *; }
-keep public interface com.here.annotations.** { *; }
-keepattributes Exceptions
-keep public class com.here.time.Duration { *; }

# Gson / modèles API
-keep class tn.turbodrive.data.network.model.** { *; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Rétrofit
-keep,allowobfuscation,allowshrinking interface tn.turbodrive.data.network.api.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclasseswithmembers,includedescriptorclasses class * {
    @retrofit2.http.* <methods>;
}

# Hilt / Dagger
-keep,allowobfuscation,allowshrinking @dagger.hilt.android.lifecycle.HiltViewModel class *
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Kotlinx Serialization (the project uses the kotlin.serialization plugin)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class tn.turbodrive.**$$serializer { *; }
-keepclassmembers class tn.turbodrive.** {
    *** Companion;
}

# Domain models + envelope (kept against reflection / serialization)
-keep class tn.turbodrive.domain.models.** { *; }
-keep class tn.turbodrive.data.network.envelope.** { *; }

# Firebase / Crashlytics
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
