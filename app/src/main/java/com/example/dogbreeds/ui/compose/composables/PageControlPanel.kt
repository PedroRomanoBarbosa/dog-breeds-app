package com.example.dogbreeds.ui.compose.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dogbreeds.R

@Composable
fun PageControlPanel(
    currentIndex: Int,
    totalPages: Int?,
    previousEnabled: Boolean,
    nextEnabled: Boolean,
    modifier: Modifier = Modifier,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
) {
    Row(
        modifier
            .background(Color.LightGray, shape = RoundedCornerShape(26.dp))
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(enabled = previousEnabled, onClick = onPrevious) {
            Text(text = stringResource(id = R.string.prev))
        }
        Text(
            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
            color = Color.DarkGray,
            fontSize = 22.sp,
            text = "${currentIndex + 1}/${totalPages ?: "-"}",
        )
        Button(enabled = nextEnabled, onClick = onNext) {
            Text(text = stringResource(id = R.string.next))
        }
    }
}

@Preview
@Composable
fun PageControlPanelPreview() {
    PageControlPanel(
        currentIndex = 0,
        totalPages = 5,
        previousEnabled = true,
        nextEnabled = true,
    )
}