package ch.florianfrauenfelder.mensazh.services

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AssetService(private val assetManager: AssetManager) {
  suspend fun readStringFile(fileName: String): String? = withContext(Dispatchers.IO) {
    try {
      assetManager.open(fileName).bufferedReader().use { it.readText() }
    } catch (ex: IOException) {
      ex.printStackTrace()
      null
    }
  }
}
