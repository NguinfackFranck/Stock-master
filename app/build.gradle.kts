plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.androidx.room)
}

android {
    namespace = "com.example.stockmanagement"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.stockmanagement"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        // NOTE: If Room 2.6+ throws a JDK compatibility error,
        // change both of these from VERSION_11 to VERSION_17
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

// 1. Tell Room where to drop schemas
room {
    schemaDirectory("$projectDir/schemas")
}

// 2. FORCE KSP TO GENERATE JAVA SOURCE CODE INSTEAD OF KOTLIN
ksp {
    arg("room.generateKotlin", "false")
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.androidx.databinding.adapters)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.cardview)
    implementation(libs.material.v190)
    implementation("com.github.f4b6a3:uuid-creator:6.1.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core:1.12.0")
    implementation("androidx.work:work-runtime:2.9.0")

        compileOnly (libs.projectlombok.lombok)
        annotationProcessor (libs.projectlombok.lombok)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler) // KSP safely processes Java files!

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}