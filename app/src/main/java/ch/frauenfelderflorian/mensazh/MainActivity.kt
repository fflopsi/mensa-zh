package ch.frauenfelderflorian.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import ch.frauenfelderflorian.mensazh.ui.theme.MensaZHTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MensaZHTheme {
        Scaffold { innerPadding ->
          Text(text = "New Mensa app", modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}
