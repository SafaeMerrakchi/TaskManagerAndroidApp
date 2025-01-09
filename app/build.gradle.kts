plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.taskmanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskmanager"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"


        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}



dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.material:material:1.9.0")  // pour TabLayout
    implementation("androidx.viewpager2:viewpager2:1.0.0")     // pour ViewPager2

    implementation("androidx.activity:activity:1.9.3") // Dernière version à ce jour
    implementation("androidx.fragment:fragment:1.6.1") // Pour une compatibilité complète

    implementation("com.google.android.material:material:<latest_version>")

    implementation("com.androidplot:androidplot-core:1.5.11")

    implementation("com.airbnb.android:lottie:6.0.0")

    implementation("com.google.code.gson:gson:2.8.9")










}