package ch.florianfrauenfelder.mensazh.ui.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun MenuList(
  menus: List<Menu>,
  selectedMenu: Menu,
  modifier: Modifier = Modifier,
  listState: LazyListState = rememberLazyListState(),
  listBottomPadding: Dp = 0.dp,
  bottomSpacer: Boolean = false,
) {
  LazyColumn(
    state = listState,
    contentPadding = PaddingValues(bottom = listBottomPadding),
    modifier = modifier.fillMaxWidth(),
  ) {
    items(
      items = menus,
      key = { it.title + it.description },
    ) {
      MenuRow(
        menu = it,
        selected = it == selectedMenu,
      )
    }
    if (bottomSpacer) {
      item(key = 0) {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }
}
