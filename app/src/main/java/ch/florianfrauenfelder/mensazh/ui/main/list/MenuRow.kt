package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun MenuRow(
  menu: Menu,
  listUseShortDescription: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clickable(onClick = onClick)
      .focusable(),
  ) {
    Text(
      text = menu.description,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 3,
    Column(
      modifier = Modifier
        .padding(8.dp)
        .weight(1f),
    )
    ) {
      Text(
        text = menu.description,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = if (listUseShortDescription) 2 else 3,
      )
    }
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.padding(8.dp),
    ) {
      Row {
        Text(
          text = menu.title,
          fontWeight = FontWeight.Bold,
        )
        if (menu.isVegan || menu.isVegetarian) {
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(if (menu.isVegan) R.string.vegan_short else R.string.vegetarian_short),
            color = Color(0xFF22AA22),
            fontWeight = FontWeight.Bold,
          )
        }
      }
      if (menu.price.isNotEmpty()) {
        Text(
          text = stringResource(R.string.price, menu.price.first()),
          textAlign = TextAlign.End,
        )
      }
    }
  }
}
