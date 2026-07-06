package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.screens.AuthScreenPreview
import com.example.ui.screens.HomeScreenPreview
import com.example.ui.screens.OnboardingPreview
import com.example.ui.screens.ProfileDetailPreview
import com.example.ui.theme.MyApplicationTheme

@Preview(showBackground = true, name = "1. Onboarding Screen")
@Composable
fun ViewOnboarding() {
    MyApplicationTheme {
        OnboardingPreview()
    }
}

@Preview(showBackground = true, name = "2. Auth Screen")
@Composable
fun ViewAuthScreen() {
    MyApplicationTheme {
        AuthScreenPreview()
    }
}

@Preview(showBackground = true, name = "3. Home Screen")
@Composable
fun ViewHomeScreen() {
    MyApplicationTheme {
        HomeScreenPreview()
    }
}

@Preview(showBackground = true, name = "4. Profile Screen")
@Composable
fun ViewProfileScreen() {
    MyApplicationTheme {
        ProfileDetailPreview()
    }
}
