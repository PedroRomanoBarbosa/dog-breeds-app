package com.example.dogbreeds.ui.compose.composables

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dogbreeds.R

@Composable
fun InfoSection(
    @DrawableRes iconRes: Int,
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    @StringRes contentDescriptionRes: Int? = null,
) {
    Box(modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                painter = painterResource(iconRes),
                contentDescription = contentDescriptionRes?.let {
                    stringResource(contentDescriptionRes)
                },
            )
            Text(textAlign = TextAlign.Center, text = stringResource(messageRes))
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFF,
)
@Composable
fun InfoSectionPreview() {
    InfoSection(
        R.drawable.baseline_error_24,
        R.string.search_empty,
    )
}