package info.sorbon.hskvocabulary.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import info.sorbon.hskvocabulary.domain.model.Word
import info.sorbon.hskvocabulary.presentation.theme.Blue600
import info.sorbon.hskvocabulary.presentation.theme.Green600
import info.sorbon.hskvocabulary.presentation.theme.Red800

@Composable
fun WordListItem(
    word: Word,
    searchQuery: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hanzi + Pinyin column
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Pinyin with optional highlight
                HighlightedText(
                    text = word.pinyin,
                    highlight = searchQuery,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Green600,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Light
                    ),
                    highlightStyle = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Green600
                    )
                )
                // Hanzi with optional highlight
                HighlightedText(
                    text = word.hanzi,
                    highlight = searchQuery,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Blue600,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    highlightStyle = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = Blue600
                    )
                )
            }

            Spacer(Modifier.width(10.dp))

            // Definition
            HighlightedText(
                text = word.localizedDefinition,
                highlight = searchQuery,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                highlightStyle = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = Green600
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

            // Bookmark icon
            if (word.isBookmark) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Bookmarked",
                    tint = Red800,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    highlight: String,
    style: androidx.compose.ui.text.TextStyle,
    highlightStyle: SpanStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    if (highlight.isBlank()) {
        Text(
            text = text,
            style = style,
            modifier = modifier,
            maxLines = maxLines
        )
    } else {
        val annotatedString = buildAnnotatedString {
            var startIndex = 0
            val lowerText = text.lowercase()
            val lowerHighlight = highlight.lowercase()

            while (startIndex < text.length) {
                val foundIndex = lowerText.indexOf(lowerHighlight, startIndex)
                if (foundIndex < 0) {
                    withStyle(SpanStyle(color = style.color, fontWeight = style.fontWeight, fontSize = style.fontSize)) {
                        append(text.substring(startIndex))
                    }
                    break
                }
                // Text before match
                if (foundIndex > startIndex) {
                    withStyle(SpanStyle(color = style.color, fontWeight = style.fontWeight, fontSize = style.fontSize)) {
                        append(text.substring(startIndex, foundIndex))
                    }
                }
                // Highlighted match
                withStyle(highlightStyle.copy(fontSize = style.fontSize)) {
                    append(text.substring(foundIndex, foundIndex + highlight.length))
                }
                startIndex = foundIndex + highlight.length
            }
        }

        Text(
            text = annotatedString,
            modifier = modifier,
            maxLines = maxLines
        )
    }
}
