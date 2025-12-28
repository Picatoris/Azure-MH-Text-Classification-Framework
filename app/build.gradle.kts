import org.gradle.kotlin.dsl.implementation

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sentimentanalysis"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.sentimentanalysis"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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

    packaging {
        resources {
            excludes += "/META-INF/{*}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.firebase:firebase-auth:24.0.1")
    implementation("com.google.firebase:firebase-database:22.0.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("org.apache.commons:commons-lang3:3.19.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    implementation("javax.xml.stream:stax-api:1.0-2")
    implementation("com.azure:azure-ai-textanalytics:5.5.11")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.squareup.okhttp3:okhttp:5.3.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation ("com.squareup.retrofit2:retrofit:3.0.0")
    implementation ("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("androidx.test:monitor:1.8.0")
    implementation("androidx.test.ext:junit:1.3.0")
    implementation("androidx.activity:activity:1.11.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    testImplementation("junit:junit:4.13.2")
    implementation("com.airbnb.android:lottie:6.7.1")
    implementation("com.firebaseui:firebase-ui-auth:9.1.1")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation ("com.github.bumptech.glide:glide:5.0.5")
    annotationProcessor ("com.github.bumptech.glide:compiler:5.0.5")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.github.lecho:hellocharts-library:1.5.8@aar")
    implementation("com.google.android.gms:play-services-fitness:21.3.0")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("nl.dionsegijn:konfetti:1.3.2")
    implementation("com.google.android.material:material:1.13.0")
}
