plugins {
  // this is necessary to avoid the plugins to be loaded multiple times
  // in each subproject's classloader
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.multiplatformLibrary) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.google.ksp) apply false
  alias(libs.plugins.androidx.room) apply false
  alias(libs.plugins.buildkonfig) apply false
  alias(libs.plugins.ktor) apply false
}
