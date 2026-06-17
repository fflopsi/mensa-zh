plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ktor)
}

group = "ch.florianfrauenfelder.mensazh"
version = "1.0.0"
application {
  mainClass = "ch.florianfrauenfelder.mensazh.ApplicationKt"
}

dependencies {
  api(projects.core)
  implementation(libs.logback)
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  testImplementation(libs.ktor.server.testHost)
  testImplementation(libs.kotlin.testJunit)
}
