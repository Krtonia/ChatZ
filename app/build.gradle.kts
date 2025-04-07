plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.firebase.crashlytics")
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.jam.chatz"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jam.chatz"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.1"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Splashscreen
    implementation(libs.androidx.core.splashscreen)

    //Coroutines
    implementation (libs.kotlinx.coroutines.android)
    implementation (libs.kotlinx.coroutines.play.services)

    //Dagger-Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)

    //FireBase
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.crashlytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage)

    //ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.glide.v4151)

    //Glide Image
    implementation(libs.glide)

    //Room Database
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)

    //Recycler
    implementation(libs.androidx.recyclerview)

    // CardView for item layout
    implementation(libs.androidx.cardview)

    //Material
    implementation(libs.google.material)
}