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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    val activity = context as? android.app.Activity
    val currentLang by viewModel.currentLanguage.collectAsState()
    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
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
            otpSent = otpSent,
            onLanguageChange = { viewModel.changeLanguage(it) },
            onLogout = { viewModel.handleLogout() },
            onSendOtp = { mobile -> if (activity != null) viewModel.sendOtp(mobile, activity) },
            onVerifyOtp = { otp -> viewModel.verifyOtp(otp) },
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
    otpSent: Boolean,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogout: () -> Unit,
    onSendOtp: (String) -> Unit,
    onVerifyOtp: (String) -> Unit,
    onCompleteOnboarding: (String, String, String, AccountType, String, String, Int, String) -> Unit,
    onToggleOnboarding: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var mobileInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+91") }
    var expanded by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (onboardingStep) {
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // App Brand Logo
                Box(
                    modifier = Modifier
                        .width(126.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kuku),
                        contentDescription = "Logo",
                        modifier = Modifier.size(width = 112.dp, height = 40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Welcome to jobaaya",
                                modifier = Modifier.padding(top = 5.dp, bottom = 20.dp),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 24.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            if (!otpSent) {
                                // MOBILE NUMBER INPUT
                                OutlinedTextField(
                                    value = mobileInput,
                                    onValueChange = { mobileInput = it },
                                    label = { Text(JobaayaLocalization.translate("mobile_number", currentLang).ifBlank { "Mobile Number" }, color = Color.White) },
                                    leadingIcon = {
                                        Box {
                                            Row(
                                                modifier = Modifier
                                                    .clickable { expanded = true }
                                                    .padding(start = 12.dp, end = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(selectedCountryCode, color = Color.White, fontWeight = FontWeight.Bold)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                                            }
                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(Color(0xFF1F2937))
                                            ) {
                                                val codes = listOf(
                                                    "+91", "+1", "+44", "+971", "+880", "+977", "+92", "+94", "+65", "+60", 
                                                    "+66", "+62", "+82", "+84", "+63", "+81", "+86", "+49", "+33", "+39", 
                                                    "+34", "+7", "+55", "+52", "+27", "+966", "+93", "+98", "+964", "+90", 
                                                    "+20", "+234", "+254", "+251", "+212", "+213", "+41", "+43", "+46", 
                                                    "+47", "+45", "+31", "+32", "+351", "+30", "+48", "+420", "+36", "+40", 
                                                    "+380", "+353", "+64", "+54", "+56", "+57", "+58", "+51", "+593"
                                                ).sorted()
                                                codes.forEach { code ->
                                                    DropdownMenuItem(
                                                        text = { Text(code, color = Color.White) },
                                                        onClick = {
                                                            selectedCountryCode = code
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    placeholder = { Text("XXXXXXXXXX", color = Color.White.copy(alpha = 0.6f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { onSendOtp(selectedCountryCode + mobileInput) },
                                    enabled = mobileInput.length >= 10,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(35.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF22C55E),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFFE51F3E),
                                        disabledContentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = JobaayaLocalization.translate("send_otp", currentLang).ifBlank { "Send OTP" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                // OTP INPUT
                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = { otpInput = it },
                                    label = { Text(JobaayaLocalization.translate("enter_otp", currentLang).ifBlank { "Enter 6-digit OTP" }, color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color.White,
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                                    )
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Button(
                                    onClick = { onVerifyOtp(otpInput) },
                                    enabled = otpInput.length == 6,
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(35.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF22C55E),
                                        contentColor = Color.White,
                                        disabledContainerColor = Color(0xFFE51F3E),
                                        disabledContentColor = Color.White
                                    )
                                ) {
                                    Text(
                                        text = JobaayaLocalization.translate("verify_otp", currentLang).ifBlank { "Verify & Login" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                TextButton(
                                    onClick = { onSendOtp(mobileInput) },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        text = JobaayaLocalization.translate("resend_otp", currentLang).ifBlank { "Resend OTP" },
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
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
    var otpSent by remember { mutableStateOf(false) }
    
    Box {
        AuthContent(
            currentLang = AppLanguage.ENGLISH,
            onboardingStep = isRegister,
            otpSent = otpSent,
            onLanguageChange = {},
            onLogout = {},
            onSendOtp = { otpSent = true },
            onVerifyOtp = {},
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
        otpSent = false,
        onLanguageChange = {},
        onLogout = {},
        onSendOtp = {},
        onVerifyOtp = {},
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
        otpSent = false,
        onLanguageChange = {},
        onLogout = {},
        onSendOtp = {},
        onVerifyOtp = {},
        onCompleteOnboarding = { _, _, _, _, _, _, _, _ -> },
        onToggleOnboarding = {}
    )
}
