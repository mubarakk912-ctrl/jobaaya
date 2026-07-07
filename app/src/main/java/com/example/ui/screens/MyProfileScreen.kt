package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.compose.ui.viewinterop.AndroidView
import de.hdodenhof.circleimageview.CircleImageView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.R
import com.example.ui.components.ProfessionPicker
import com.example.ui.components.PhotoFitDialog
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel

@Composable
fun MyProfileScreen(
    viewModel: JobaayaViewModel,
    onPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
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
        } else {
            val exception = result.error
            // Handle error if needed
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
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF0B3A51)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text(text = JobaayaLocalization.translate("my_profile_title", currentLang), style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            myProfile?.let { me ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        AndroidView(
                            factory = { ctx ->
                                val view = LayoutInflater.from(ctx).inflate(R.layout.activity_main, null)
                                view.findViewById<Button>(R.id.btn_select_image).setOnClickListener {
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
                                if (me.profilePhotoUrl.isNotBlank()) {
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
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = JobaayaLocalization.translate("prof_details", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text(JobaayaLocalization.translate("full_name", currentLang)) }, modifier = Modifier.fillMaxWidth())
                        ProfessionPicker(currentProfession = editProfession, onProfessionChange = { editProfession = it }, currentSkills = editSkills, onSkillsChange = { editSkills = it }, label = JobaayaLocalization.translate("profession", currentLang))
                        
                        AndroidView(
                            factory = { ctx ->
                                val view = LayoutInflater.from(ctx).inflate(R.layout.layout_contact_info, null)

                                val etAddress = view.findViewById<android.widget.EditText>(R.id.etAddressLine)
                                val etCity = view.findViewById<android.widget.EditText>(R.id.etCity)
                                val etPinCode = view.findViewById<android.widget.EditText>(R.id.etPinCode)
                                val etMobile = view.findViewById<android.widget.EditText>(R.id.etMobileNumber)
                                val spinnerCountry = view.findViewById<Spinner>(R.id.spinnerCountry)
                                val tvCountryCodeDisplay = view.findViewById<android.widget.TextView>(R.id.tvCountryCodeDisplay)

                                etAddress.setText(editAddress)
                                etCity.setText(editCity)
                                etPinCode.setText(editPinCode)
                                etMobile.setText(editMobile)

                                val textWatcher = object : android.text.TextWatcher {
                                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                                    override fun afterTextChanged(s: android.text.Editable?) {
                                        editAddress = etAddress.text.toString()
                                        editCity = etCity.text.toString()
                                        editPinCode = etPinCode.text.toString()
                                        editMobile = etMobile.text.toString()
                                    }
                                }

                                etAddress.addTextChangedListener(textWatcher)
                                etCity.addTextChangedListener(textWatcher)
                                etPinCode.addTextChangedListener(textWatcher)
                                etMobile.addTextChangedListener(textWatcher)

                                // Map of Country Names and their respective Dialing Codes
                                val countryData = linkedMapOf(
                                    "India" to "+91", "United States" to "+1", "United Kingdom" to "+44",
                                    "UAE" to "+971", "Australia" to "+61", "Canada" to "+1",
                                    "Germany" to "+49", "France" to "+33", "Japan" to "+81"
                                )
                                val countryNames = countryData.keys.toTypedArray()

                                val countryAdapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item, countryNames)
                                spinnerCountry.adapter = countryAdapter

                                // Dynamic Sync: Country select karne par code apne aap badlega
                                spinnerCountry.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                                        val selectedCountry = countryNames[position]
                                        val matchingCode = countryData[selectedCountry] ?: "+91"
                                        tvCountryCodeDisplay.text = matchingCode
                                        editCountryCode = matchingCode

                                        // Force white text visibility in dropdown selection
                                        (view as? android.widget.TextView)?.setTextColor(android.graphics.Color.WHITE)
                                    }
                                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                                }

                                view
                            },
                            update = { view ->
                                val etAddress = view.findViewById<android.widget.EditText>(R.id.etAddressLine)
                                val etCity = view.findViewById<android.widget.EditText>(R.id.etCity)
                                val etPinCode = view.findViewById<android.widget.EditText>(R.id.etPinCode)
                                val etMobile = view.findViewById<android.widget.EditText>(R.id.etMobileNumber)

                                if (etAddress.text.toString() != editAddress) etAddress.setText(editAddress)
                                if (etCity.text.toString() != editCity) etCity.setText(editCity)
                                if (etPinCode.text.toString() != editPinCode) etPinCode.setText(editPinCode)
                                if (etMobile.text.toString() != editMobile) etMobile.setText(editMobile)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editExperience, 
                            onValueChange = { editExperience = it }, 
                            label = { Text(JobaayaLocalization.translate("experience", currentLang)) }, 
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                        OutlinedTextField(value = editEmail, onValueChange = { editEmail = it }, label = { Text(JobaayaLocalization.translate("email", currentLang)) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
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
                        Button(onClick = {
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
                                    fullAddress = combinedAddress,
                                    mobileNumber = combinedMobile,
                                    emailAddress = editEmail,
                                    languagesRaw = editLanguages,
                                    workingHours = editWorkingHours
                                )
                            )
                            Toast.makeText(context, JobaayaLocalization.translate("saved_toast", currentLang), Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text(JobaayaLocalization.translate("save_all", currentLang))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

