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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation (libs.mappls.android.sdk)
    implementation (libs.retrofit)
    implementation(libs.converter.gson)

}