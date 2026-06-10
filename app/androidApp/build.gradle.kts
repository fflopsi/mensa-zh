import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.androidx.room)
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
    optIn.addAll(
      "kotlinx.coroutines.ExperimentalCoroutinesApi",
      "kotlin.concurrent.atomics.ExperimentalAtomicApi",
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    )
  }
}

dependencies {
  implementation(projects.app.shared)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.room.runtime)
  ksp(libs.androidx.room.compiler)
  implementation(libs.bundles.navigation3)
  implementation(libs.jetbrains.material3.adaptiveNavigationSuite)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.kotlinx.datetime)
  implementation(platform(libs.ktor.bom))
  implementation(libs.bundles.ktor.client)
  implementation(libs.ktor.client.okhttp)
  implementation(libs.coil.compose)
  implementation(libs.coil.network)

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

    val apiProperties = Properties()
    val zfvApiKey = try {
      apiProperties.let {
        it.load(rootProject.file("api.properties").inputStream())
        it.getProperty("ZFV_API_KEY")
      }
    } catch (_: Exception) {
      "\"\""
    }
    buildConfigField(
      type = "String",
      name = "ZFV_API_KEY",
      value = zfvApiKey,
    )
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
//    getByName("release") {
//      isMinifyEnabled = false
//    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures.buildConfig = true
}

room {
  schemaDirectory("$projectDir/schemas")
}
