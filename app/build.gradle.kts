plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.blendedxr.gls.agent"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.blendedxr.gls.agent"
        minSdk = 34
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.signalr)
    implementation(libs.kotlinx.coroutines.rx3)
}