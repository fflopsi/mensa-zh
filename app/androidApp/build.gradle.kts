plugins {
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.android.application)
}

dependencies {
  implementation(projects.app.shared)

  implementation(libs.androidx.activity.compose)

  implementation(libs.compose.ui.toolingPreview)
  debugImplementation(libs.compose.ui.tooling)
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

  packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

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

tasks.register<Copy>("copyApkToDist") {
  from(layout.buildDirectory.dir("outputs/apk"))
  into(rootProject.layout.projectDirectory.dir("dist/apk"))
  include("**/*.apk")
}

tasks.register<Copy>("copyAabToDist") {
  from(layout.buildDirectory.dir("outputs/bundle"))
  into(rootProject.layout.projectDirectory.dir("dist/aab"))
  include("**/*.aab")
}

tasks.matching { it.name.startsWith("assemble") }.configureEach { finalizedBy("copyApkToDist") }
tasks.matching { it.name.startsWith("bundle") }.configureEach { finalizedBy("copyAabToDist") }
