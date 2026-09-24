plugins {
    id("com.android.application")
}

android {
    namespace = "tw.com.countdownboard"
    compileSdk = 35

    defaultConfig {
        applicationId = "tw.com.countdownboard"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.2"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
