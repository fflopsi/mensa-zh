package ch.florianfrauenfelder.mensazh.services

import android.content.res.AssetManager
import java.io.IOException

class AssetService(private val assetManager: AssetManager) {
  fun readStringFile(fileName: String): String? = try {
    assetManager.open(fileName).bufferedReader().use { it.readText() }
  } catch (ex: IOException) {
    ex.printStackTrace()
    null
  }
}
