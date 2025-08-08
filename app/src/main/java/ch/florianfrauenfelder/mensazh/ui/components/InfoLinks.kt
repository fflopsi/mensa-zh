package ch.florianfrauenfelder.mensazh.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import ch.florianfrauenfelder.mensazh.R

@Composable
fun InfoLinks(modifier: Modifier = Modifier) {
  Row(
    horizontalArrangement = Arrangement.Center,
    modifier = modifier,
  ) {
    Text(
      text = buildAnnotatedString {
        withLink(
          LinkAnnotation.Url(
            stringResource(R.string.url_privacy_policy),
            TextLinkStyles(
              style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                textDecoration = TextDecoration.Underline,
              ),
            ),
          ),
        ) {
          append(stringResource(R.string.privacy_policy))
        }
        append(", ")
        withLink(
          LinkAnnotation.Url(
            stringResource(R.string.url_source_code),
            TextLinkStyles(
              style = SpanStyle(
                color = MaterialTheme.colorScheme.tertiary,
                textDecoration = TextDecoration.Underline,
              ),
            ),
          ),
        ) {
          append(stringResource(R.string.source_code))
        }
      },
    )
  }
}

@Preview
@Composable
private fun InfoLinksPreview() = InfoLinks()
