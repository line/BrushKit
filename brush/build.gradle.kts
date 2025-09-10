plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply(from = "$rootDir/gradle/android.gradle")
apply(from = "$rootDir/gradle/publish.gradle")

android {
    namespace = "com.linecorp.brushkit"
}

dependencies {
    api(project(":brush-core"))

    implementation(libs.kotlin.collection.immutable)
    implementation(libs.androidx.appcompat)
    implementation(libs.timber)
}
