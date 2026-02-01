package ch.florianfrauenfelder.mensazh.ui.main.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.domain.model.Menu

@Composable
fun MenuList(
  menus: List<Menu>,
  selectedMenu: Menu?,
  selectMenu: (Menu) -> Unit,
  autoShowImage: Boolean,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  LazyColumn(
    contentPadding = contentPadding,
    modifier = modifier,
  ) {
    items(
      items = menus,
      key = { it.title + it.description },
    ) {
      MenuRow(
        menu = it,
        selected = it.title == selectedMenu?.title,
        select = selectMenu,
        autoShowImage = autoShowImage,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
