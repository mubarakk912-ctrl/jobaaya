package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button as AndroidButton
import android.widget.EditText as AndroidEditText
import android.widget.Spinner
import android.widget.TextView as AndroidTextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.R
import com.example.ui.components.ProfessionPicker
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel
import de.hdodenhof.circleimageview.CircleImageView

@Composable
fun SettingsScreen(
    viewModel: JobaayaViewModel,
    modifier: Modifier = Modifier,
    onPreviewClick: (String) -> Unit = {} // MainActivity का एरर हटाने के लिए डिफ़ॉल्ट हैंडलर
) {
    val context = LocalContext.current

    // --- PROFILE SCREEN INITIALIZATION ---
    val sharedPreferences = remember { context.getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE) }

    // --- SETTINGS STATES ---
    val currentLang by viewModel.currentLanguage.collectAsState()
    val blockedUsers by viewModel.blockedProfiles.collectAsState()
    val isMobilePublic by viewModel.isMobilePublic.collectAsState()
    val isAccountPrivate by viewModel.isAccountPrivate.collectAsState()

    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showPrivacyItems by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // --- NEW DROPDOWN STATE FOR PROFILE ---
    var showProfileItems by remember { mutableStateOf(false) }

    // --- PROFILE STATES ---
    val myProfile by viewModel.myProfile.collectAsState()
    val serviceRadius by viewModel.serviceRadius.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = CropImageContract()
    ) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                viewModel.uploadProfilePhoto(uri)
            }
        }
    }

    var editName by remember { mutableStateOf("") }
    var editProfession by remember { mutableStateOf("") }
    var editSkills by remember { mutableStateOf("") }
    var editAbout by remember { mutableStateOf("") }
    var editExperience by remember { mutableStateOf("0") }
    var editAddress by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editPinCode by remember { mutableStateOf("") }
    var editMobile by remember { mutableStateOf("") }
    var editCountryCode by remember { mutableStateOf("+91") }
    var editEmail by remember { mutableStateOf("") }
    var editLanguages by remember { mutableStateOf("") }
    var editWorkingHours by remember { mutableStateOf("09:00 - 17:00") }

    LaunchedEffect(myProfile) {
        myProfile?.let {
            if (editName.isEmpty()) editName = it.name
            if (editProfession.isEmpty()) editProfession = it.profession
            if (editSkills.isEmpty()) editSkills = it.skillsRaw
            if (editAbout.isEmpty()) editAbout = it.aboutSection
            if (editExperience == "0" && it.yearsOfExperience > 0) editExperience = it.yearsOfExperience.toString()
            if (editEmail.isEmpty()) editEmail = it.emailAddress
            if (editLanguages.isEmpty()) editLanguages = it.languagesRaw
            if (editWorkingHours == "09:00 - 17:00") editWorkingHours = it.workingHours

            val addressParts = it.fullAddress.split(",").map { it.trim() }
            if (editAddress.isEmpty()) editAddress = addressParts.getOrNull(0) ?: ""
            if (editCity.isEmpty()) editCity = addressParts.getOrNull(1) ?: ""
            if (editPinCode.isEmpty()) editPinCode = addressParts.getOrNull(2) ?: ""

            if (editMobile.isEmpty()) {
                val mobileParts = it.mobileNumber.split(" ")
                if (mobileParts.size >= 2) {
                    editCountryCode = mobileParts[0]
                    editMobile = mobileParts.drop(1).joinToString(" ")
                } else {
                    editMobile = it.mobileNumber
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App settings Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B3A51))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = JobaayaLocalization.translate("settings", currentLang).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ==========================================
            // 1. PROFILE EXPANDABLE DROPDOWN CARD (जगह बचाने के लिए बॉक्स में पैक)
            // ==========================================
            myProfile?.let { me ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column {
                        // Dropdown Header (हमेशा दिखाई देगा)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showProfileItems = !showProfileItems }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = JobaayaLocalization.translate("prof_details", currentLang),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (showProfileItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        }

                        // Dropdown Content (सिर्फ एरो क्लिक करने पर खुलेगा)
                        AnimatedVisibility(visible = showProfileItems) {
                            Column(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 4.dp))

                                // इमेज और नाम वाला हेडर
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    AndroidView(
                                        factory = { ctx ->
                                            val view = LayoutInflater.from(ctx).inflate(R.layout.activity_main, null)
                                            view.findViewById<AndroidButton>(R.id.btn_select_image)?.setOnClickListener {
                                                cropImageLauncher.launch(
                                                    CropImageContractOptions(
                                                        uri = null,
                                                        cropImageOptions = CropImageOptions().apply {
                                                            guidelines = CropImageView.Guidelines.ON
                                                            cropShape = CropImageView.CropShape.OVAL
                                                            fixAspectRatio = true
                                                        }
                                                    )
                                                )
                                            }
                                            view
                                        },
                                        update = { view ->
                                            val imageView = view.findViewById<CircleImageView>(R.id.profile_image)
                                            if (imageView != null && me.profilePhotoUrl.isNotBlank()) {
                                                imageView.setImageURI(Uri.fromFile(java.io.File(me.profilePhotoUrl)))
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(20.dp))
                                    Column {
                                        Text(text = editName.ifBlank { me.name }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                        Text(text = editProfession.ifBlank { me.profession }, color = Color(0xFF00A38E), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text(JobaayaLocalization.translate("full_name", currentLang)) }, modifier = Modifier.fillMaxWidth())
                                ProfessionPicker(currentProfession = editProfession, onProfessionChange = { editProfession = it }, currentSkills = editSkills, onSkillsChange = { editSkills = it }, label = JobaayaLocalization.translate("profession", currentLang))

                                AndroidView(
                                    factory = { ctx ->
                                        val view = LayoutInflater.from(ctx).inflate(R.layout.layout_contact_info, null)

                                        val etAddress = view.findViewById<AndroidEditText>(R.id.etAddressLine)
                                        val etCity = view.findViewById<AndroidEditText>(R.id.etCity)
                                        val etPinCode = view.findViewById<AndroidEditText>(R.id.etPinCode)
                                        val etMobile = view.findViewById<AndroidEditText>(R.id.etMobileNumber)
                                        val spinnerCountry = view.findViewById<Spinner>(R.id.spinnerCountry)
                                        val tvCountryCodeDisplay = view.findViewById<AndroidTextView>(R.id.tvCountryCodeDisplay)

                                        etAddress?.setText(editAddress)
                                        etCity?.setText(editCity)
                                        etPinCode?.setText(editPinCode)
                                        etMobile?.setText(editMobile)

                                        etAddress?.setText(sharedPreferences.getString("address", ""))
                                        etCity?.setText(sharedPreferences.getString("city", ""))
                                        etPinCode?.setText(sharedPreferences.getString("pincode", ""))
                                        etMobile?.setText(sharedPreferences.getString("mobilenumber", ""))

                                        val savedCountryPos = sharedPreferences.getInt("country_position", 0)
                                        spinnerCountry?.setSelection(savedCountryPos)

                                        val textWatcher = object : android.text.TextWatcher {
                                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                            override fun afterTextChanged(s: android.text.Editable?) {
                                                editAddress = etAddress?.text.toString()
                                                editCity = etCity?.text.toString()
                                                editPinCode = etPinCode?.text.toString()
                                                editMobile = etMobile?.text.toString()
                                            }
                                        }

                                        etAddress?.addTextChangedListener(textWatcher)
                                        etCity?.addTextChangedListener(textWatcher)
                                        etPinCode?.addTextChangedListener(textWatcher)
                                        etMobile?.addTextChangedListener(textWatcher)

                                        val countryData = linkedMapOf(
                                            "India" to "+91", "United States" to "+1", "United Kingdom" to "+44",
                                            "UAE" to "+971", "Australia" to "+61", "Canada" to "+1",
                                            "Germany" to "+49", "France" to "+33", "Japan" to "+81"
                                        )
                                        val countryNames = countryData.keys.toTypedArray()

                                        val countryAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, countryNames)
                                        spinnerCountry?.adapter = countryAdapter

                                        spinnerCountry?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                                                val selectedCountry = countryNames[position]
                                                val matchingCode = countryData[selectedCountry] ?: "+91"
                                                tvCountryCodeDisplay?.text = matchingCode
                                                editCountryCode = matchingCode
                                                (view as? AndroidTextView)?.setTextColor(android.graphics.Color.WHITE)
                                            }
                                            override fun onNothingSelected(parent: AdapterView<*>?) {}
                                        }

                                        view
                                    },
                                    update = { view ->
                                        val etAddress = view.findViewById<AndroidEditText>(R.id.etAddressLine)
                                        val etCity = view.findViewById<AndroidEditText>(R.id.etCity)
                                        val etPinCode = view.findViewById<AndroidEditText>(R.id.etPinCode)
                                        val etMobile = view.findViewById<AndroidEditText>(R.id.etMobileNumber)

                                        if (etAddress?.text.toString() != editAddress) etAddress?.setText(editAddress)
                                        if (etCity?.text.toString() != editCity) etCity?.setText(editCity)
                                        if (etPinCode?.text.toString() != editPinCode) etPinCode?.setText(editPinCode)
                                        if (etMobile?.text.toString() != editMobile) etMobile?.setText(editMobile)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = editExperience,
                                    onValueChange = { editExperience = it },
                                    label = { Text(JobaayaLocalization.translate("experience", currentLang)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text(JobaayaLocalization.translate("email", currentLang)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                                OutlinedTextField(value = editLanguages, onValueChange = { editLanguages = it }, label = { Text(JobaayaLocalization.translate("lang_sep", currentLang)) }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editWorkingHours, onValueChange = { editWorkingHours = it }, label = { Text(JobaayaLocalization.translate("working_hours", currentLang)) }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editAbout, onValueChange = { editAbout = it }, label = { Text(JobaayaLocalization.translate("about", currentLang)) }, modifier = Modifier.fillMaxWidth(), minLines = 3)

                                Button(
                                    onClick = { onPreviewClick(me.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text(JobaayaLocalization.translate("preview", currentLang))
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                Text(text = "${JobaayaLocalization.translate("service_area", currentLang)}: ${serviceRadius.toInt()} km", fontWeight = FontWeight.Medium)
                                Slider(value = serviceRadius, onValueChange = { viewModel.setServiceRadius(it) }, valueRange = 1f..100f)

                                Button(
                                    onClick = {
                                        val editor = sharedPreferences.edit()
                                        editor.putString("address", editAddress)
                                        editor.putString("city", editCity)
                                        editor.putString("pincode", editPinCode)
                                        editor.putString("mobilenumber", editMobile)
                                        editor.apply()

                                        val combinedAddress = if (editAddress.isNotBlank() || editCity.isNotBlank() || editPinCode.isNotBlank()) {
                                            "$editAddress, $editCity, $editPinCode"
                                        } else {
                                            me.fullAddress
                                        }
                                        val combinedMobile = if (editMobile.isNotBlank()) {
                                            "$editCountryCode $editMobile"
                                        } else {
                                            me.mobileNumber
                                        }
                                        viewModel.updateMyProfessionalProfile(
                                            me.copy(
                                                name = editName,
                                                profession = editProfession,
                                                skillsRaw = editSkills,
                                                aboutSection = editAbout,
                                                yearsOfExperience = editExperience.toIntOrNull() ?: 0,
                                                emailAddress = editEmail,
                                                languagesRaw = editLanguages,
                                                workingHours = editWorkingHours,
                                                fullAddress = combinedAddress,
                                                mobileNumber = combinedMobile
                                            )
                                        )
                                        Toast.makeText(context, "Profile Saved Successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Save, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(JobaayaLocalization.translate("save_all_details", currentLang) ?: "Save All Details")
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 2. SETTINGS SCREEN CODE (मूल कोडिंग बिना किसी बदलाव के)
            // ==========================================
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Expandable Privacy Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyItems = !showPrivacyItems }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(text = JobaayaLocalization.translate("privacy_settings", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(
                            imageVector = if (showPrivacyItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showPrivacyItems) {
                        Column(modifier = Modifier.padding(start = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(JobaayaLocalization.translate("public_mobile", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(JobaayaLocalization.translate("public_mobile_desc", currentLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(checked = isMobilePublic, onCheckedChange = { viewModel.setMobilePublic(it) })
                            }

                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(JobaayaLocalization.translate("private_account", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(JobaayaLocalization.translate("private_account_desc", currentLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(checked = isAccountPrivate, onCheckedChange = { viewModel.setAccountPrivate(it) })
                            }

                            Row(Modifier.fillMaxWidth().clickable { showBlockedDialog = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(12.dp))
                                Text(JobaayaLocalization.translate("blocked_list", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Icon(Icons.Default.Delete, null)
                                Spacer(Modifier.width(8.dp))
                                Text(JobaayaLocalization.translate("delete_account", currentLang))
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Text(text = JobaayaLocalization.translate("app_data", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(Modifier.fillMaxWidth().clickable { showLanguagesDialog = true }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text("${JobaayaLocalization.translate("languages", currentLang)}: ${currentLang.displayName}", color = MaterialTheme.colorScheme.onSurface)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Text(text = JobaayaLocalization.translate("support_social", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(Modifier.fillMaxWidth().clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jobaaya.com/support"))
                        context.startActivity(intent)
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("help_center", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable {
                        Toast.makeText(context, JobaayaLocalization.translate("report_bug", currentLang), Toast.LENGTH_SHORT).show()
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("report_bug", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Download jobaaya app to find local services!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("share_app", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.example.jobaaya"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("rate_us", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Button(
                onClick = { viewModel.handleLogout() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text(JobaayaLocalization.translate("logout", currentLang), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Languages Dialog
    if (showLanguagesDialog) {
        Dialog(onDismissRequest = { showLanguagesDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = JobaayaLocalization.translate("select_language", currentLang),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Box(modifier = Modifier.height(400.dp)) {
                        LazyColumn {
                            items(AppLanguage.entries) { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.changeLanguage(lang)
                                            showLanguagesDialog = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        color = if (lang == currentLang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 16.sp
                                    )
                                    if (lang == currentLang) {
                                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = { showLanguagesDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(JobaayaLocalization.translate("cancel", currentLang).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Blocked Users Dialog
    if (showBlockedDialog) {
        Dialog(onDismissRequest = { showBlockedDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Blocked Users", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))

                    if (blockedUsers.isEmpty()) {
                        Text("No users blocked yet.", color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(blockedUsers) { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = user.toString(), color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}