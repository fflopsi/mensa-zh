package ch.florianfrauenfelder.mensazh.ui

enum class Platform(val isMobile: Boolean) {
  Android(true), Ios(true), Desktop(false)
}

expect val thisPlatform: Platform
