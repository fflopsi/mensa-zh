import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
}

android {
  namespace = "ch.florianfrauenfelder.mensazh"
  compileSdk = 36

  defaultConfig {
    applicationId = "ch.florianfrauenfelder.mensazh"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = libs.versions.app.get()

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
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(17)
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
  implementation(libs.androidx.datastore)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
