import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

dependencies {
  implementation(projects.app.shared)

  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutinesSwing)

  implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
  application {
    mainClass = "ch.florianfrauenfelder.mensazh.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
      packageName = "MensaZH"
      packageVersion = "2.2.1"
      modules("jdk.unsupported", "jdk.unsupported.desktop") // Necessary for room in compiled app
    }
  }
}
