import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  implementation(projects.app.shared)

  implementation(compose.desktop.currentOs)
  implementation(libs.kotlinx.coroutinesSwing)

  implementation(libs.compose.ui.toolingPreview)
}

compose.desktop {
  application {
    mainClass = "ch.florianfrauenfelder.mensazh.MainKt"

    nativeDistributions {
      targetFormats(
        TargetFormat.Rpm,
        TargetFormat.Deb,
        TargetFormat.AppImage,
        TargetFormat.Msi,
        TargetFormat.Dmg,
      )
      packageName = "MensaZH"
      packageVersion = "2.2.1"
      description = "Menus of the mensas at ETH and UZH in Zürich"
      copyright = "© 2026 Florian Frauenfelder."
      vendor = "Florian Frauenfelder"
      licenseFile.set(rootProject.file("LICENSE.md"))
      modules("jdk.unsupported", "jdk.unsupported.desktop") // Necessary for room in compiled app

      linux {
        iconFile.set(rootProject.file("icons/ic_launcher.png"))
        debMaintainer = "florian.l.frauenfelder@gmail.com"
        rpmLicenseType = "MIT"
      }
      windows {
        iconFile.set(rootProject.file("icons/ic_launcher.ico"))
        upgradeUuid = "B3BA5958-98E6-4356-9653-9C1239B62299"
      }
      macOS {
        iconFile.set(rootProject.file("icons/ic_launcher.icns"))
        // TODO: Signing and notarizing
      }
    }
  }
}
