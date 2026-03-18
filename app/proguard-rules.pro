# Add project specific ProGuard rules here.

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Credential Manager (Google Sign-In)
# ============================================
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}

# ============================================
# Kotlin Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep @Serializable classes and their serializers
-keep,includedescriptorclasses class co.alcheclub.ai.trading.assistant.**$$serializer { *; }
-keepclassmembers class co.alcheclub.ai.trading.assistant.** {
    *** Companion;
}
-keepclasseswithmembers class co.alcheclub.ai.trading.assistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Supabase
# ============================================
-keep class io.github.jan.supabase.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.auth.** { *; }

# ============================================
# Ktor
# ============================================
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ============================================
# Google Identity (GoogleIdTokenCredential)
# ============================================
-keep class com.google.android.libraries.identity.googleid.** { *; }
