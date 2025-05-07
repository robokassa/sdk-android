import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val prop = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}

android {

    namespace = "com.robokassa_sample"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.robokassa_sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 12
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MERCHANT", prop["ipol_merchant"] as String)
        buildConfigField("String", "PWD_1", prop["ipol_pwd_1"] as String)
        buildConfigField("String", "PWD_2", prop["ipol_pwd_2"] as String)
        buildConfigField("String", "PWD_TEST_1", prop["ipol_pwd_test_1"] as String)
        buildConfigField("String", "PWD_TEST_2", prop["ipol_pwd_test_2"] as String)
        buildConfigField("String", "REDIRECT_URL", prop["ipol_redirect_url"] as String)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    buildFeatures {
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(project(":Robokassa_Library"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}