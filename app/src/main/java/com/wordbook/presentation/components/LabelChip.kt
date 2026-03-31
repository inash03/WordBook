package com.wordbook.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wordbook.domain.model.Label

@Composable
fun LabelChip(
    label: Label,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onToggle: (() -> Unit)? = null
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(label.color)) }
        .getOrDefault(Color(0xFF6200EE))

    if (onToggle != null) {
        FilterChip(
            selected = selected,
            onClick = onToggle,
            label = { Text(label.name, fontSize = 12.sp) },
            modifier = modifier.padding(end = 4.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = color.copy(alpha = 0.2f),
                selectedLabelColor = color
            )
        )
    } else {
        AssistChip(
            onClick = {},
            label = { Text(label.name, fontSize = 12.sp) },
            modifier = modifier.padding(end = 4.dp),
            colors = AssistChipDefaults.assistChipColors(
                containerColor = color.copy(alpha = 0.15f),
                labelColor = color
            )
        )
    }
}
