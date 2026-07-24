package com.example.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.JobaayaDatabase
import com.example.data.model.ContactMessage
import com.example.data.model.UserProfile
import com.example.data.repository.JobaayaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ContactUsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JobaayaDatabase.getDatabase(application)
    private val repository = JobaayaRepository(
        db.userProfileDao,
        db.userReviewDao,
        db.chatMessageDao,
        db.userConnectionDao,
        db.subscriptionDao,
        db.profileMediaDao,
        db.utilityNoteDao,
        db.systemNotificationDao,
        db.partnershipDealDao,
        db.productDao
    )

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiEvent = MutableStateFlow<ContactUsUiEvent?>(null)
    val uiEvent: StateFlow<ContactUsUiEvent?> = _uiEvent.asStateFlow()

    fun onMessageChange(newMessage: String) {
        _message.value = newMessage
    }

    fun sendMessage() {
        val currentMessage = _message.value.trim()
        if (currentMessage.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val myProfile = repository.myProfile.first()
                if (myProfile != null) {
                    val contactMsg = ContactMessage(
                        message = currentMessage,
                        userId = myProfile.id,
                        userName = myProfile.name,
                        registeredMobile = myProfile.mobileNumber,
                        email = myProfile.emailAddress.ifBlank { null },
                        deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                        androidVersion = Build.VERSION.RELEASE,
                        appVersion = getAppVersion(),
                        status = "Pending"
                    )

                    val result = repository.submitContactMessage(contactMsg)
                    if (result.isSuccess) {
                        _message.value = ""
                        _uiEvent.value = ContactUsUiEvent.Success("Your message has been sent successfully.")
                    } else {
                        _uiEvent.value = ContactUsUiEvent.Error("Message could not be sent. Please try again.")
                    }
                } else {
                    _uiEvent.value = ContactUsUiEvent.Error("User profile not found.")
                }
            } catch (e: Exception) {
                _uiEvent.value = ContactUsUiEvent.Error("Message could not be sent. Please try again.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetUiEvent() {
        _uiEvent.value = null
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
}

sealed class ContactUsUiEvent {
    data class Success(val message: String) : ContactUsUiEvent()
    data class Error(val message: String) : ContactUsUiEvent()
}
