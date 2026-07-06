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
import com.example.viewmodel.JobaayaViewModel

@Composable
fun MyProfileScreen(
    viewModel: JobaayaViewModel,
    onPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
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

    LaunchedEffect(myProfile) {
        myProfile?.let {
            if (editName.isEmpty()) editName = it.name
            if (editProfession.isEmpty()) editProfession = it.profession
            if (editSkills.isEmpty()) editSkills = it.skillsRaw
            if (editAbout.isEmpty()) editAbout = it.aboutSection
            if (editExperience == "0") editExperience = it.yearsOfExperience.toString()
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color(0xFF0B3A51)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                Text(text = "MY PROFESSIONAL PROFILE", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
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
                            Text(text = me.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = me.profession, color = Color(0xFF00A38E), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = "Professional Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                        ProfessionPicker(currentProfession = editProfession, onProfessionChange = { editProfession = it }, currentSkills = editSkills, onSkillsChange = { editSkills = it }, label = "Profession")
                        
                        AndroidView(
                            factory = { ctx ->
                                val view = LayoutInflater.from(ctx).inflate(R.layout.layout_contact_info, null)

                                val spinnerCountry = view.findViewById<Spinner>(R.id.spinnerCountry)
                                val tvCountryCodeDisplay = view.findViewById<android.widget.TextView>(R.id.tvCountryCodeDisplay)

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
                                        val matchingCode = countryData[selectedCountry]
                                        tvCountryCodeDisplay.text = matchingCode

                                        // Force white text visibility in dropdown selection
                                        (view as? android.widget.TextView)?.setTextColor(android.graphics.Color.WHITE)
                                    }
                                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                                }

                                view
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(value = editExperience, onValueChange = { editExperience = it }, label = { Text("Years of Experience") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editAbout, onValueChange = { editAbout = it }, label = { Text("About Me") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        
                        Button(
                            onClick = { onPreviewClick(me.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Preview")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(text = "Service Area: ${serviceRadius.toInt()} km", fontWeight = FontWeight.Medium)
                        Slider(value = serviceRadius, onValueChange = { viewModel.setServiceRadius(it) }, valueRange = 1f..100f)
                        Button(onClick = {
                            viewModel.updateMyProfessionalProfile(me.copy(name = editName, profession = editProfession, skillsRaw = editSkills, aboutSection = editAbout, yearsOfExperience = editExperience.toIntOrNull() ?: 0))
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Save, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Save All Details")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

