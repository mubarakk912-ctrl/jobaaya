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
                                
                                // Comprehensive Global Country Codes List
                                val spinnerCountryCode = view.findViewById<Spinner>(R.id.spinnerCountryCode)
                                val globalCountryCodes = arrayOf(
                                    "+1", "+7", "+20", "+27", "+30", "+31", "+32", "+33", "+34", "+36", "+39", "+40", "+41", "+43", "+44", "+45", "+46", "+47", "+48", "+49", 
                                    "+51", "+52", "+53", "+54", "+55", "+56", "+57", "+58", "+60", "+61", "+62", "+63", "+64", "+65", "+66", "+81", "+82", "+84", "+86", "+90", 
                                    "+91", "+92", "+93", "+94", "+95", "+98", "+211", "+212", "+213", "+216", "+218", "+220", "+221", "+222", "+223", "+224", "+225", "+226", 
                                    "+227", "+228", "+229", "+230", "+231", "+232", "+233", "+234", "+235", "+236", "+237", "+238", "+239", "+240", "+241", "+242", "+243", 
                                    "+244", "+245", "+248", "+249", "+250", "+251", "+252", "+253", "+254", "+255", "+256", "+257", "+258", "+260", "+261", "+262", "+263", 
                                    "+264", "+265", "+266", "+267", "+268", "+269", "+290", "+291", "+297", "+298", "+299", "+350", "+351", "+352", "+353", "+354", "+355", 
                                    "+356", "+357", "+358", "+359", "+370", "+371", "+372", "+373", "+374", "+375", "+376", "+377", "+378", "+380", "+381", "+382", "+383", 
                                    "+385", "+386", "+387", "+389", "+420", "+421", "+423", "+500", "+501", "+502", "+503", "+504", "+505", "+506", "+507", "+509", "+590", 
                                    "+591", "+592", "+593", "+594", "+595", "+596", "+597", "+598", "+599", "+670", "+672", "+673", "+674", "+675", "+676", "+677", "+678", 
                                    "+679", "+680", "+681", "+682", "+683", "+685", "+686", "+687", "+688", "+689", "+690", "+691", "+692", "+850", "+852", "+853", "+855", 
                                    "+856", "+880", "+886", "+960", "+961", "+962", "+963", "+964", "+965", "+966", "+967", "+968", "+970", "+971", "+972", "+973", "+974", 
                                    "+975", "+976", "+977", "+992", "+993", "+994", "+995", "+996", "+998"
                                ).distinct().sortedBy { it.substring(1).toInt() }.toTypedArray()

                                val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, globalCountryCodes)
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                spinnerCountryCode.adapter = adapter
                                
                                view
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(value = editExperience, onValueChange = { editExperience = it }, label = { Text("Years of Experience") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editAbout, onValueChange = { editAbout = it }, label = { Text("About Me") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
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

