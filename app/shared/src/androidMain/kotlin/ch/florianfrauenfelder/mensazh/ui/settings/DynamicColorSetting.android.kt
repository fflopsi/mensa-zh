package ch.florianfrauenfelder.mensazh.ui.settings

import android.os.Build
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Switch
import androidx.compose.ui.Modifier
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.use_dynamic_colors
import org.jetbrains.compose.resources.stringResource

actual fun LazyListScope.dynamicColorSetting(theme: ThemeSettings, update: (Setting) -> Unit) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    item(key = 19) {
      SettingsRow(
        title = stringResource(Res.string.use_dynamic_colors),
        onClick = { update(Setting.SetUseDynamicColor(!theme.useDynamicColor)) },
        modifier = Modifier.animateItem(),
      ) {
        Switch(checked = theme.useDynamicColor, onCheckedChange = null)
      }
    }
  }
}
