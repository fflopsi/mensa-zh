import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.google.ksp)
}

android {
  namespace = "ch.florianfrauenfelder.mensazh"
  compileSdk {
    version = release(36)
  }

  defaultConfig {
    applicationId = "ch.famoser.mensa"
    minSdk = 26
    targetSdk = 36
    versionCode = 54
    versionName = "2.0.4"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      isMinifyEnabled = false
      isShrinkResources = false
      isDebuggable = true
    }
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      isDebuggable = false
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
  }
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.adaptive)
  implementation(libs.androidx.adaptive.layout)
  implementation(libs.androidx.adaptive.navigation)
  implementation(libs.androidx.adaptive.navigation.suite)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(libs.coil.compose)
  implementation(libs.coil.network)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
