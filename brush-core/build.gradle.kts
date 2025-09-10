plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.plugin.serialization)
}

apply(from = "$rootDir/gradle/android.gradle")

android {
    namespace = "com.linecorp.brushkit.core"
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.timber)
    testImplementation(libs.junit)
}
