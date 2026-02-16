import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.androidx.room)
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

  androidResources {
    generateLocaleConfig = true
    localeFilters += setOf("en", "de")
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
    optIn.addAll(
      "kotlin.uuid.ExperimentalUuidApi",
      "kotlinx.coroutines.ExperimentalCoroutinesApi",
      "kotlin.concurrent.atomics.ExperimentalAtomicApi",
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    )
  }
}

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.bundles.androidx.compose)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)
  implementation(libs.bundles.androidx.navigation3)
  implementation(libs.androidx.adaptive.navigation.suite)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(platform(libs.okhttp.bom))
  implementation(libs.okhttp)
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
