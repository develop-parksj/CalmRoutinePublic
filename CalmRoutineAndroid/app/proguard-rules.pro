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

############################
# 1. 必須アノテーションの維持
############################
-keep @androidx.annotation.Keep class *

# Kotlin reflection
-keep class kotlin.Metadata { *; }

############################
# 2. Retrofit, Gson, kotlinx.serialization
############################
# Gson SerializedName 使用時
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# (kotlinx.serialization 使用時に必要な場合がある)
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

############################
# 3. 内部データモデル (実際に使用するパッケージ名のみ)
############################
-keepclassmembers class com.gyoheul.calm_routine.model.** {
    public <init>(...);
}
-keep class com.gyoheul.calm_routine.model.** { *; }
-keepclassmembers class com.gyoheul.calm_routine.data.** {
    public <init>(...);
}
-keep class com.gyoheul.calm_routine.data.** { *; }

############################
# 4. Retrofit インターフェース
############################
-keep interface retrofit2.** { *; }
-keepattributes Signature

############################
# 5. Crashlytics
############################
-keepattributes SourceFile,LineNumberTable

############################
# 6. 必要に応じて追加 (エラー発生時のみ)
############################
# -keep class com.google.firebase.** { *; }
# -keep class com.amplifyframework.** { *; }
# -keep class coil.** { *; }
# -keep class com.airbnb.lottie.** { *; }
# -keep class com.nimbusds.** { *; }
# -keep class com.google.android.gms.tasks.** { *; }

# (これらは一旦すべて除外し、
# ビルド時や実行時に ClassNotFound, NoSuchMethod などのエラーが出た際に追加)

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn aws.sdk.kotlin.runtime.http.interceptors.AwsBusinessMetric
-dontwarn aws.sdk.kotlin.runtime.http.interceptors.BusinessMetricsInterceptor