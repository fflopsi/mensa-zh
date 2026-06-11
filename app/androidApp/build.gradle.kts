plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

dependencies {
  implementation(projects.app.shared)

  implementation(libs.androidx.activity.compose)

  implementation(libs.compose.uiToolingPreview)
  debugImplementation(libs.compose.uiTooling)
}

android {
  namespace = "ch.florianfrauenfelder.mensazh"
  compileSdk {
    version = release(libs.versions.android.compileSdk.get().toInt())
  }

  defaultConfig {
    applicationId = "ch.famoser.mensa"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 71
    versionName = "2.2.1"
  }

  androidResources {
    generateLocaleConfig = true
    localeFilters += setOf("en", "de")
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }

  buildTypes {
    debug {
      isMinifyEnabled = false
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
}
