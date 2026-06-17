import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.multiplatformLibrary)
}

kotlin {
  iosArm64()
  iosSimulatorArm64()

  jvm()

//  js {
//    browser()
//  }

//  @OptIn(ExperimentalWasmDsl::class)
//  wasmJs {
//    browser()
//  }

  android {
    namespace = "ch.florianfrauenfelder.mensazh.core"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions.jvmTarget = JvmTarget.JVM_11
    androidResources.enable = true
    withHostTest {
      isIncludeAndroidResources = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      // put your Multiplatform dependencies here
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
