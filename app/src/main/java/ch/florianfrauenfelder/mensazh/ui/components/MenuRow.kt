package ch.florianfrauenfelder.mensazh.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun MenuRow(
  menu: Menu,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier
      .clickable(onClick = onClick)
      .focusable()
      .fillMaxWidth(),
  ) {
    Text(
      text = menu.description,
      style = MaterialTheme.typography.bodyMedium,
      maxLines = 3,
      modifier = Modifier
        .padding(8.dp)
        .weight(1f),
    )
    Column(
      horizontalAlignment = Alignment.End,
      modifier = Modifier.padding(8.dp),
    ) {
      Text(
        text = menu.title,
        textAlign = TextAlign.End,
        fontWeight = FontWeight.Bold,
      )
      if (menu.price.isNotEmpty()) {
        Text(
          text = stringResource(R.string.price, menu.price.first()),
          textAlign = TextAlign.End,
        )
      }
    }
  }
}
