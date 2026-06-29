package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MyApplicationTheme

@Preview(showBackground = true, name = "Simple Test")
@Composable
fun SimpleTestPreview() {
    MyApplicationTheme {
        Text(text = "Preview is Working!", modifier = Modifier.padding(20.dp))
    }
}
