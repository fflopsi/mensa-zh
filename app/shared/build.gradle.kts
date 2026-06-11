import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.ksp)
  alias(libs.plugins.androidx.room)
  alias(libs.plugins.buildkonfig)
}

kotlin {
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

  android {
    namespace = "ch.florianfrauenfelder.mensazh.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()

    compilerOptions {
      jvmTarget = JvmTarget.JVM_17
    }
    androidResources {
      enable = true
    }
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
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)

      implementation(libs.androidx.datastore)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.bundles.navigation3)
      implementation(libs.jetbrains.material3.adaptiveNavigationSuite)
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
      implementation(libs.compose.uiToolingPreview)
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
}

dependencies {
  androidRuntimeClasspath(libs.compose.uiTooling)
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspJvm", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

room {
  schemaDirectory("$projectDir/schemas")
}

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
