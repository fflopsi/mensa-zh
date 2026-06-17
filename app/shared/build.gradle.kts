import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.buildkonfig)
  alias(libs.plugins.android.multiplatformLibrary)
}

kotlin {
  android {
    namespace = "ch.florianfrauenfelder.mensazh.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions.jvmTarget = JvmTarget.JVM_17
    androidResources.enable = true
    withHostTest {
      isIncludeAndroidResources = true
    }
  }

  jvm()

  listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "Shared"
      isStatic = true
    }
  }

//  js {
//    browser()
//  }

//  @OptIn(ExperimentalWasmDsl::class)
//  wasmJs {
//    browser()
//  }

  sourceSets {
    commonMain.dependencies {
      api(projects.core)
      implementation(libs.bundles.compose)
      implementation(libs.bundles.navigation3)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.bundles.androidx)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.kotlinx.datetime)
      implementation(libs.bundles.ktor.client)
      implementation(libs.coil.compose)
      implementation(libs.coil.network)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    androidMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    jvmMain.dependencies {
      implementation(libs.ktor.client.okhttp)
    }
    nativeMain.dependencies {
      implementation(libs.ktor.client.darwin)
    }
//    webMain.dependencies {
//      implementation(libs.ktor.client.js)
//    }
//    jsMain.dependencies {
//      implementation(libs.wrappers.browser)
//    }
  }

  compilerOptions {
    optIn.addAll(
      "kotlinx.coroutines.ExperimentalCoroutinesApi",
      "kotlin.concurrent.atomics.ExperimentalAtomicApi",
      "kotlinx.serialization.ExperimentalSerializationApi",
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi",
    )
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.ui.tooling)
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspJvm", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room.schemaDirectory("$projectDir/schemas")

val apiProperties = Properties()
val zfvApiKey: String = try {
  apiProperties.let {
    it.load(rootProject.file("api.properties").inputStream())
    it.getProperty("ZFV_API_KEY")
  }
} catch (_: Exception) {
  "\"\""
}

buildkonfig {
  packageName = "ch.florianfrauenfelder.mensazh"
  defaultConfigs {
    buildConfigField(FieldSpec.Type.STRING, "ZFV_API_KEY", zfvApiKey)
  }
}
