import com.android.build.api.dsl.Lint
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.safety"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.safety"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties().apply {
            load(project.rootProject.file("local.properties").reader())
        }
        buildConfigField("String","REST_MAP_SDK_KEY","\"${properties.getProperty("REST_MAP_SDK_KEY")}\"")
        buildConfigField("String","CLIENT_ID_KEY","\"${properties.getProperty("CLIENT_ID_KEY")}\"")
        buildConfigField("String","CLIENT_SECRET_KEY","\"${properties.getProperty("CLIENT_SECRET_KEY")}\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isCrunchPngs = true
            isShrinkResources  =true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    lint {
        disable.addAll(
            listOf(
                "TimberArgCount",
                "TimberArgTypes",
                "TimberTagLength",
                "BinaryOperationInTimber",
                "LogNotTimber",
                "StringFormatInTimber",
                "ThrowableNotAtBeginning"
            )
        )
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.location)
    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)

    // JUnit
    testImplementation("junit:junit:4.13.2")

    // AndroidX Test - Core, Runner, and Rules
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")

    // Espresso for UI testing
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    testImplementation("androidx.test.espresso:espresso-intents:3.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    // Mockito for mocking API calls
    testImplementation("org.mockito:mockito-core:4.5.1")
    androidTestImplementation("org.mockito:mockito-android:4.5.1")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
    testImplementation("org.mockito:mockito-inline:3.12.4")

// Add PowerMock (for mocking static methods)
    testImplementation ("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation ("org.powermock:powermock-api-mockito2:2.0.9")
    // Coroutine Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
    androidTestImplementation("org.robolectric:robolectric:4.14")
    testImplementation("androidx.arch.core:core-testing:2.1.0")

    implementation (libs.mappls.android.sdk)
    implementation (libs.retrofit)
    implementation(libs.converter.gson)

}