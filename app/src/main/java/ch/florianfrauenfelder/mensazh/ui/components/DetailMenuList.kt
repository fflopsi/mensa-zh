package ch.florianfrauenfelder.mensazh.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun DetailMenuList(
  menus: List<Menu>,
  selectedMenu: Menu,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
  endSpacer: Boolean = false,
) {
  LazyColumn(
    state = listState,
    modifier = modifier.fillMaxWidth(),
  ) {
    items(menus) {
      DetailMenuRow(menu = it, selected = it == selectedMenu)
    }
    if (endSpacer) {
      item {
        Spacer(modifier = Modifier.height(76.dp))
      }
    }
  }
}
