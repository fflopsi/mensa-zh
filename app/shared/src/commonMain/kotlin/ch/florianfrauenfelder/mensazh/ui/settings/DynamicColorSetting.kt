package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.compose.foundation.lazy.LazyListScope
import ch.florianfrauenfelder.mensazh.domain.preferences.Setting
import ch.florianfrauenfelder.mensazh.domain.preferences.ThemeSettings

expect fun LazyListScope.dynamicColorSetting(theme: ThemeSettings, update: (Setting) -> Unit)
