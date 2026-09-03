// Top-level build file — configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    // Registering this here (apply false) is safe with no google-services.json —
    // it only becomes active, and only then requires the file, where it's
    // applied in app/build.gradle.kts. See the comment there.
    alias(libs.plugins.google.services) apply false
}
