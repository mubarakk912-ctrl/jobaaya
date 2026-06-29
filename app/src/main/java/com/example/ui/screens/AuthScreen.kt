package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AccountType
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.ui.components.ProfessionPicker
import com.example.viewmodel.JobaayaViewModel

@Composable
fun AuthScreen(
    viewModel: JobaayaViewModel,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.authError.collectLatest { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AuthContent(
            currentLang = currentLang,
            onboardingStep = onboardingStep,
            onLanguageChange = { viewModel.changeLanguage(it) },
            onLogout = { viewModel.handleLogout() },
            onEmailLogin = { email, password -> viewModel.loginWithEmail(email, password) },
            onGoogleSignIn = { viewModel.loginWithGoogle() },
            onCompleteOnboarding = { name, profession, skills, accountType, email, address, exp, languages ->
                viewModel.completeOnboardingRegistration(name, profession, skills, accountType, email, address, exp, languages)
            },
            onToggleOnboarding = { viewModel.startOnboarding() },
            modifier = modifier
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun AuthContent(
    currentLang: AppLanguage,
    onboardingStep: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogout: () -> Unit,
    onEmailLogin: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onCompleteOnboarding: (String, String, String, AccountType, String, String, Int, String) -> Unit,
    onToggleOnboarding: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var otpInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    // Onboarding fields
    var regName by remember { mutableStateOf("") }
    var regProfession by remember { mutableStateOf("") }
    var regSkills by remember { mutableStateOf("") }
    var regAccountType by remember { mutableStateOf(AccountType.PROFESSIONAL) }
    var regEmail by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regExperience by remember { mutableIntStateOf(3) }
    var regLanguages by remember { mutableStateOf("English, Hindi") }

    val scrollState = rememberScrollState()

    val primaryBgGradients = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.background
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, bottom = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                // App Brand Logo fixed at top
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kuku),
                        contentDescription = "Logo",
                        modifier = Modifier.size(width = 80.dp, height = 28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(primaryBgGradients)
                .padding(innerPadding)
        ) {
            // Language selection icon at top right
            var showLangSelector by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { showLangSelector = !showLangSelector },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Change Language",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                if (showLangSelector) {
                    Card(
                        modifier = Modifier
                            .width(200.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            AppLanguage.entries.take(4).forEach { lang ->
                                Text(
                                    text = lang.displayName,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onLanguageChange(lang)
                                            showLangSelector = false
                                        }
                                        .padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal,
                                    color = if (lang == currentLang) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Name and Tagline
                Text(
                    text = "jobaaya",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White, // Branding/Heading as white
                    letterSpacing = 1.sp
                )

                Text(
                    text = JobaayaLocalization.translate("app_tagline", currentLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface, // Normal as OffWhite
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (!onboardingStep) {
                    // LOGIN INTERFACES
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_card"),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = JobaayaLocalization.translate(key = "login_title", language = currentLang),
                                modifier = Modifier.padding(top = 5.dp),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 35.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = JobaayaLocalization.translate("login_subtitle", currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // EMAIL MODE ONLY
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text(JobaayaLocalization.translate("enter_email", currentLang)) },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text(JobaayaLocalization.translate("enter_password", currentLang)) },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { onEmailLogin(emailInput, passwordInput) },
                                enabled = emailInput.contains("@") && passwordInput.length >= 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(JobaayaLocalization.translate("login", currentLang), fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Google Sign In Button
                            OutlinedButton(
                                onClick = { onGoogleSignIn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = JobaayaLocalization.translate("google_signin", currentLang),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "New to jobaaya?",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                TextButton(onClick = { onToggleOnboarding(true) }) {
                                    Text(
                                        text = JobaayaLocalization.translate("register", currentLang),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // REGISTRATION ONBOARDING QUESTIONNAIRE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = JobaayaLocalization.translate("register", currentLang),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = { onToggleOnboarding(false) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Login")
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Role Picker (Three types)
                            Text(
                                text = JobaayaLocalization.translate("select_account_type", currentLang),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = regAccountType == AccountType.CUSTOMER,
                                        onClick = { regAccountType = AccountType.CUSTOMER }
                                    )
                                    Text(JobaayaLocalization.translate("customer", currentLang), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = regAccountType == AccountType.PROFESSIONAL,
                                        onClick = { regAccountType = AccountType.PROFESSIONAL }
                                    )
                                    Text(JobaayaLocalization.translate("professional", currentLang), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = regAccountType == AccountType.BUSINESS,
                                        onClick = { regAccountType = AccountType.BUSINESS }
                                    )
                                    Text(JobaayaLocalization.translate("business", currentLang), style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text(JobaayaLocalization.translate("full_name", currentLang)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ProfessionPicker(
                                currentProfession = regProfession,
                                onProfessionChange = { regProfession = it },
                                currentSkills = regSkills,
                                onSkillsChange = { regSkills = it },
                                label = JobaayaLocalization.translate("profession", currentLang)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regSkills,
                                onValueChange = { regSkills = it },
                                label = { Text(JobaayaLocalization.translate("skills", currentLang)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text(JobaayaLocalization.translate("email", currentLang)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regAddress,
                                onValueChange = { regAddress = it },
                                label = { Text(JobaayaLocalization.translate("address", currentLang)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${JobaayaLocalization.translate("experience", currentLang)}: ${regExperience}Y",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    IconButton(onClick = { if (regExperience > 0) regExperience-- }) {
                                        Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { regExperience++ }) {
                                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = regLanguages,
                                onValueChange = { regLanguages = it },
                                label = { Text(JobaayaLocalization.translate("languages", currentLang)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (regName.isNotBlank() && regProfession.isNotBlank()) {
                                        onCompleteOnboarding(
                                            regName,
                                            regProfession,
                                            regSkills,
                                            regAccountType,
                                            regEmail,
                                            regAddress,
                                            regExperience,
                                            regLanguages
                                        )
                                    }
                                },
                                enabled = regName.isNotBlank() && regProfession.isNotBlank(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(JobaayaLocalization.translate("save_profile", currentLang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Interactive Preview")
@Composable
fun AuthInteractivePreview() {
    var isRegister by remember { mutableStateOf(false) }
    
    Box {
        AuthContent(
            currentLang = AppLanguage.ENGLISH,
            onboardingStep = isRegister,
            onLanguageChange = {},
            onLogout = {},
            onEmailLogin = { _, _ -> },
            onGoogleSignIn = {},
            onCompleteOnboarding = { _, _, _, _, _, _, _, _ -> },
            onToggleOnboarding = { isRegister = it }
        )
        
        // Floating button only for preview toggle
        Button(
            onClick = { isRegister = !isRegister },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(if (isRegister) "Show Login" else "Show Registration")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthContent(
        currentLang = AppLanguage.ENGLISH,
        onboardingStep = false,
        onLanguageChange = {},
        onLogout = {},
        onEmailLogin = { _, _ -> },
        onGoogleSignIn = {},
        onCompleteOnboarding = { _, _, _, _, _, _, _, _ -> },
        onToggleOnboarding = {}
    )
}

@Preview(showBackground = true, name = "Onboarding")
@Composable
fun OnboardingPreview() {
    AuthContent(
        currentLang = AppLanguage.ENGLISH,
        onboardingStep = true,
        onLanguageChange = {},
        onLogout = {},
        onEmailLogin = { _, _ -> },
        onGoogleSignIn = {},
        onCompleteOnboarding = { _, _, _, _, _, _, _, _ -> },
        onToggleOnboarding = {}
    )
}
