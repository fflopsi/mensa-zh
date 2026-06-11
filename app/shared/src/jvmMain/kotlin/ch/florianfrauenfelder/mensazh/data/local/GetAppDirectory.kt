package ch.florianfrauenfelder.mensazh.data.local

import java.io.File

private const val appName = "mensazh"

fun getAppDirectory(): File {
  val os = System.getProperty("os.name")?.lowercase() ?: ""
  val dir = when {
    os.contains("win") -> {
      // Windows: C:\Users\<User>\AppData\Roaming\<AppName>
      val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
      File(appData, appName)
    }
    os.contains("mac") -> {
      // macOS: ~/Library/Application Support/<AppName>
      val home = System.getProperty("user.home")
      File(home, "Library/Application Support/$appName")
    }
    else -> {
      // Linux/other: ~/.config/<AppName>  (respects XDG_CONFIG_HOME if set)
      val xdgConfig = System.getenv("XDG_CONFIG_HOME")
      if (!xdgConfig.isNullOrBlank()) {
        File(xdgConfig, appName)
      } else {
        File(System.getProperty("user.home"), ".config/$appName")
      }
    }
  }
  return dir.also { it.mkdirs() }
}
