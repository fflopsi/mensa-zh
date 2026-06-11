package ch.florianfrauenfelder.mensazh.ui.settings

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.ic_arrow_next_24
import mensazh.app.shared.generated.resources.more_settings
import mensazh.app.shared.generated.resources.more_settings_desc
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

actual fun LazyListScope.openSystemSettingsSetting() {
  item(key = 22) {
    val context = LocalContext.current

    SettingsRow(
      title = stringResource(Res.string.more_settings),
      subtitle = stringResource(Res.string.more_settings_desc),
      onClick = {
        Intent(
          Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
          Uri.fromParts("package", context.packageName, null),
        ).apply { context.startActivity(this) }

      },
      modifier = Modifier.animateItem(),
    ) {
      Icon(painterResource(Res.drawable.ic_arrow_next_24), null)
    }
  }
}
