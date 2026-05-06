import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id ("kotlin-kapt")
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    flavorDimensions += "env"

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    productFlavors {
        create("dev") {
            dimension = "env"
            buildConfigField("String", "API_BASE_URL", "\"http://192.168.1.23:8000/\"")
            manifestPlaceholders["deepLinkHost"] = "192.168.1.8"
        }
        create("prod") {
            dimension = "env"
            buildConfigField("String", "API_BASE_URL", "\"https://yourdomain.com/\"")
            manifestPlaceholders["deepLinkHost"] = "yourdomain.com"
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.hilt.android) // Use the alias from the catalog
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7") // Or the latest version
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0") // Hoặc phiên bản mới hơn
    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-messaging:25.0.1")

    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    // Async Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Paging 3 (crucial for your paginated API)
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")
    implementation("androidx.paging:paging-compose:3.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // data storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.compose.material:material-icons-extended:1.6.8")
}