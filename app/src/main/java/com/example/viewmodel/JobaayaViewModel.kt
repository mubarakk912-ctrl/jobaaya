package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.core.net.toUri
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.data.database.JobaayaDatabase
import com.example.data.auth.SessionManager
import com.example.data.model.AccountType
import com.example.data.model.ChatMessage
import com.example.data.model.Subscription
import com.example.data.model.SystemNotification
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
import com.example.data.model.WorkStatus
import com.example.data.model.PartnershipDeal
import com.example.data.model.DealMessage
import com.example.data.model.DealAuditLog
import com.example.data.repository.AuthRepository
import com.example.data.repository.JobaayaRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

private data class FilterSet1(val query: String, val avail: String, val rating: Float, val exp: Int)
private data class FilterSet2(val lang: String, val dist: Float, val myProf: UserProfile?)

class JobaayaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        JobaayaDatabase::class.java,
        "jobaaya_database"
    ).fallbackToDestructiveMigration().build()

    private val sessionManager = SessionManager(application)
    private val authRepository = AuthRepository(sessionManager)

    private val repository = JobaayaRepository(
        db.userProfileDao,
        db.userReviewDao,
        db.chatMessageDao,
        db.userConnectionDao,
        db.subscriptionDao,
        db.profileMediaDao,
        db.utilityNoteDao,
        db.systemNotificationDao,
        db.partnershipDealDao
    )

    // Current app-wide language state
    private val _currentLanguage = MutableStateFlow(JobaayaLocalization.detectDeviceLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Auth states
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn

    private val _otpDispatched = MutableStateFlow(false)
    val otpDispatched: StateFlow<Boolean> = _otpDispatched.asStateFlow()

    private var firebaseVerificationId: String = ""

    private val _loginMobileNumber = MutableStateFlow("")
    val loginMobileNumber: StateFlow<String> = _loginMobileNumber.asStateFlow()

    private val _onboardingStep = MutableStateFlow(false) // If true, show register questionnaire
    val onboardingStep: StateFlow<Boolean> = _onboardingStep.asStateFlow()

    private val _authError = MutableSharedFlow<String>()
    val authError: SharedFlow<String> = _authError.asSharedFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // My own profile StateFlow
    val myProfile: StateFlow<UserProfile?> = repository.myProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Dynamic search settings
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterAvailability = MutableStateFlow("ALL") // "ALL", "AVAILABLE", "BUSY", "OFFLINE"
    val filterAvailability: StateFlow<String> = _filterAvailability.asStateFlow()

    private val _filterRating = MutableStateFlow(0.0f) // minimum rating
    val filterRating: StateFlow<Float> = _filterRating.asStateFlow()

    private val _filterExperience = MutableStateFlow(0) // minimum years
    val filterExperience: StateFlow<Int> = _filterExperience.asStateFlow()

    private val _filterLanguage = MutableStateFlow("ALL") // "ALL", "Hindi", "English", "Malayalam" etc.
    val filterLanguage: StateFlow<String> = _filterLanguage.asStateFlow()

    private val _filterDistanceKm = MutableStateFlow(50f) // distance slider
    val filterDistanceKm: StateFlow<Float> = _filterDistanceKm.asStateFlow()

    // Combine filters with repository otherProfiles
    private val filterSet1Flow = combine(
        _searchQuery,
        _filterAvailability,
        _filterRating,
        _filterExperience
    ) { query, avail, rating, exp ->
        FilterSet1(query, avail, rating, exp)
    }

    private val filterSet2Flow = combine(
        _filterLanguage,
        _filterDistanceKm,
        myProfile
    ) { lang, dist, myProf ->
        FilterSet2(lang, dist, myProf)
    }

    val filteredProfiles: StateFlow<List<UserProfile>> = combine(
        repository.otherProfiles,
        filterSet1Flow,
        filterSet2Flow
    ) { profiles: List<UserProfile>, set1: FilterSet1, set2: FilterSet2 ->
        val query = set1.query
        val availability = set1.avail
        val rating = set1.rating
        val exp = set1.exp
        
        val lang = set2.lang
        val distance = set2.dist
        val myProf = set2.myProf

        // Extract city from my address for local filtering
        val myCity = myProf?.fullAddress?.split(",")?.lastOrNull()?.trim()

        profiles.filter { profile: UserProfile ->
            // Skip blocked profiles
            if (profile.isBlocked) return@filter false

            // City Match (Only show profiles from same city if user has a city set)
            val matchesCity = if (myCity != null && myCity.isNotEmpty()) {
                val profileCity = profile.fullAddress.split(",").lastOrNull()?.trim()
                profileCity.equals(myCity, ignoreCase = true)
            } else {
                true // Show all if user has no address/city
            }

            // Search query match (profession, name, skills)
            val matchesQuery = query.isBlank() || 
                    profile.name.contains(query, ignoreCase = true) ||
                    profile.profession.contains(query, ignoreCase = true) ||
                    profile.skillsRaw.contains(query, ignoreCase = true) ||
                    profile.fullAddress.contains(query, ignoreCase = true)

            // Availability match
            val matchesAvailability = availability == "ALL" || profile.availabilityStatus == availability

            // Rating match
            val matchesRating = profile.averageRating >= rating

            // Experience match
            val matchesExperience = profile.yearsOfExperience >= exp

            // Language match
            val matchesLanguage = lang == "ALL" || profile.languagesRaw.contains(lang, ignoreCase = true)

            // Mock distance check relative to me
            val calculatedDistance = if (myProf != null) {
                calculateDistanceKm(myProf.latitude, myProf.longitude, profile.latitude, profile.longitude)
            } else {
                // assume central New Delhi relative distance
                calculateDistanceKm(28.6139, 77.2090, profile.latitude, profile.longitude)
            }
            val matchesDistance = calculatedDistance <= distance

            matchesCity && matchesQuery && matchesAvailability && matchesRating && matchesExperience && matchesLanguage && matchesDistance
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val blockedProfiles: StateFlow<List<UserProfile>> = repository.otherProfiles.map { profiles ->
        profiles.filter { it.isBlocked }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableCategories: StateFlow<List<String>> = repository.otherProfiles.map { profiles ->
        val highRank = listOf("Software Engineer", "IT Professional", "Web Developer", "Digital Marketer")
        val dynamic = profiles.map { it.profession }.distinct()
        val sortedDynamic = dynamic.sorted()
        (listOf("All") + highRank + sortedDynamic + listOf("Other")).distinct()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("All")
    )

    // Chat handling
    private val _activeChatUserId = MutableStateFlow<String?>(null)
    val activeChatUserId: StateFlow<String?> = _activeChatUserId.asStateFlow()

    private val _isPartnerTyping = MutableStateFlow(false)
    val isPartnerTyping: StateFlow<Boolean> = _isPartnerTyping.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatUserId.flatMapLatest { profileId ->
        if (profileId.isNullOrEmpty()) {
            flowOf(emptyList<ChatMessage>())
        } else {
            repository.getChatMessages(profileId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic inbox messages map (latest message per user)
    val chatInboxList: StateFlow<List<ChatInbox>> = repository.allMessages.combine(repository.otherProfiles) { msgList: List<ChatMessage>, profiles: List<UserProfile> ->
        val grouped = msgList.groupBy { it.chatWithProfileId }
        grouped.mapNotNull { entry ->
            val profileId = entry.key
            val messages = entry.value
            val profile = profiles.find { it.id == profileId } ?: return@mapNotNull null
            val latest = messages.maxByOrNull { it.timestamp } ?: return@mapNotNull null
            val unreadCount = messages.count { !it.isRead && !it.isFromMe }
            ChatInbox(profile, latest, unreadCount)
        }.sortedWith(compareByDescending<ChatInbox> { it.partnerProfile.isPinned }.thenByDescending { it.lastMessage.timestamp })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Notes persistence
    val allNotes: StateFlow<List<UtilityNote>> = repository.allNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Activity notifications mimicking real-time updates
    val notifications: StateFlow<List<SystemNotification>> = repository.allNotifications.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Change Language from UI
    fun changeLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    // Auth production APIs
    fun setLoginMobile(mobile: String) {
        _loginMobileNumber.value = mobile
    }

    fun loginWithEmail(email: String, password: CharSequence) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.loginWithEmail(email, password).onSuccess {
                checkProfileStatusAndNavigate()
            }.onFailure {
                _authError.emit(it.message ?: "Login failed")
            }
            _isLoading.value = false
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.signInWithGoogle("mock_google_token").onSuccess {
                checkProfileStatusAndNavigate()
            }.onFailure {
                _authError.emit(it.message ?: "Google Sign-In failed")
            }
            _isLoading.value = false
        }
    }

    private suspend fun checkProfileStatusAndNavigate() {
        val myProfileDirect = repository.getMyProfileDirect()
        if (myProfileDirect == null || myProfileDirect.name == "Guest User" || myProfileDirect.name.isBlank()) {
            _onboardingStep.value = true
        } else {
            // Already has a profile
            _onboardingStep.value = false
        }
    }

    fun completeOnboardingRegistration(name: String, profession: String, skills: String, accountType: AccountType, email: String, address: String, exp: Int, languages: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val existing = repository.getMyProfileDirect()
            val finalId = existing?.id ?: UUID.randomUUID().toString()
            val updated = UserProfile(
                id = finalId,
                name = name,
                profession = profession,
                skillsRaw = skills,
                mobileNumber = _loginMobileNumber.value.ifBlank { "+91 99999 88888" },
                emailAddress = email,
                fullAddress = address,
                latitude = existing?.latitude ?: 28.7159,
                longitude = existing?.longitude ?: 77.1006,
                yearsOfExperience = exp,
                languagesRaw = languages,
                aboutSection = "Verified $profession providing customer success. Certified skills: $skills.",
                accountType = accountType.name,
                profilePhotoUrl = existing?.profilePhotoUrl ?: "",
                isMe = true,
                isVerified = false,
                availabilityStatus = WorkStatus.AVAILABLE.name
            )
            repository.insertProfile(updated)
            _onboardingStep.value = false
            addSystemNotification("Profile Configured", "Your profile cards have been published! Welcome to jobaaya.")
            _isLoading.value = false
        }
    }

    fun updateMyProfessionalProfile(profile: UserProfile) {
        viewModelScope.launch {
            val existing = repository.getMyProfileDirect()
            val finalProfile = if (existing != null) {
                profile.copy(profilePhotoUrl = existing.profilePhotoUrl)
            } else {
                profile
            }
            repository.updateProfile(finalProfile)
            addSystemNotification("Profile Alert", "Your professional profile details were updated.")
        }
    }

    fun toggleBookmarkProfile(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                val newStatus = !prof.bookmarkStatus
                repository.updateProfile(prof.copy(bookmarkStatus = newStatus))
                if (newStatus) {
                    addSystemNotification("Profile Shortlisted", "${prof.name} has been added to your professional shortlist.")
                }
            }
        }
    }

    // Connect/Follow trigger
    fun toggleConnectWithUser(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            val me = myProfile.value
            if (prof != null && me != null) {
                val nextStatus = when (prof.followStatus) {
                    0 -> 2 // Connected
                    2 -> 0 // Disconnected
                    else -> 0
                }
                
                // Update local profile status
                repository.updateProfile(prof.copy(
                    followStatus = nextStatus,
                    interactionsCount = prof.interactionsCount + (if (nextStatus == 2) 1 else 0)
                ))
                
                // Update UserConnection table
                repository.toggleConnection(me.id, prof.id)

                if (nextStatus == 2) {
                    addSystemNotification("Connection Success", "You are now connected with ${prof.name}.")
                }
            }
        }
    }

    // Chat features
    fun selectActiveChat(profileId: String?) {
        _activeChatUserId.value = profileId
        if (profileId != null) {
            viewModelScope.launch {
                repository.markChatAsRead(profileId)
            }
        }
    }

    fun sendChatMessage(
        text: String, 
        mediaType: String? = null, 
        mediaUrl: String? = null, 
        forwardedFrom: String? = null,
        replyToId: Int? = null,
        replyToText: String? = null
    ) {
        val destId = _activeChatUserId.value
        if (!destId.isNullOrEmpty()) {
            viewModelScope.launch {
                var finalMediaUrl = mediaUrl
                
                // If it's a content URI (e.g. from picker), copy to internal storage for persistence
                if (mediaUrl != null && mediaUrl.startsWith("content://")) {
                    finalMediaUrl = saveMediaToInternalStorage(mediaUrl)
                }

                val msg = ChatMessage(
                    chatWithProfileId = destId,
                    isFromMe = true,
                    text = text,
                    mediaType = mediaType,
                    mediaUrl = finalMediaUrl,
                    forwardedFrom = forwardedFrom,
                    replyToId = replyToId,
                    replyToText = replyToText
                )
                repository.insertMessage(msg)
                
                // Track interaction count
                val other = db.userProfileDao.getProfileByIdDirect(destId)
                if (other != null) {
                    repository.updateProfile(other.copy(interactionsCount = other.interactionsCount + 1))
                }
            }
        }
    }

    fun uploadProfilePhoto(uri: android.net.Uri, scale: Float = 1f, offsetX: Float = 0f, offsetY: Float = 0f) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<android.app.Application>()
                val fileName = "avatar_${System.currentTimeMillis()}.jpg"
                val file = java.io.File(context.filesDir, fileName)
                
                withContext(Dispatchers.IO) {
                    context.filesDir.listFiles()?.forEach { 
                        if (it.name.startsWith("avatar_") && it.name.endsWith(".jpg")) {
                            it.delete()
                        }
                    }
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        java.io.FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                val finalPath = file.absolutePath
                val me = repository.getMyProfileDirect()
                if (me != null) {
                    repository.updateProfile(me.copy(profilePhotoUrl = finalPath))
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Photo Uploaded & Saved!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(getApplication(), "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveMediaToInternalStorage(uriString: String, customFileName: String? = null): String {
        return try {
            val context = getApplication<Application>()
            val uri = android.net.Uri.parse(uriString)
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = customFileName ?: "chat_media_${System.currentTimeMillis()}"
            val file = File(context.filesDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            uriString
        }
    }

    fun editChatMessage(message: ChatMessage, newText: String) {
        viewModelScope.launch {
            repository.updateMessage(message.copy(text = newText, isEdited = true))
        }
    }

    fun deleteChatMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.deleteMessage(message)
        }
    }

    fun deleteChatMessages(messages: List<ChatMessage>) {
        viewModelScope.launch {
            messages.forEach { repository.deleteMessage(it) }
        }
    }

    fun toggleStarMessage(message: ChatMessage) {
        viewModelScope.launch {
            repository.updateMessage(message.copy(isStarred = !message.isStarred))
        }
    }

    fun togglePinChat(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isPinned = !prof.isPinned))
            }
        }
    }

    fun toggleMuteChat(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isMuted = !prof.isMuted))
            }
        }
    }

    fun sendLocationMessage(lat: Double, lng: Double, address: String) {
        sendChatMessage(text = address, mediaType = "LOCATION", mediaUrl = "$lat,$lng")
    }

    fun sendContactMessage(name: String, phone: String) {
        sendChatMessage(text = "$name|$phone", mediaType = "CONTACT")
    }

    fun sendDirectDealMessage(title: String, budget: String, deadline: String) {
        sendChatMessage(text = "$title|$budget|$deadline", mediaType = "DEAL")
    }

    fun sendPollMessage(question: String, options: List<String>) {
        val optionsStr = options.joinToString("|")
        sendChatMessage(text = "$question|$optionsStr", mediaType = "POLL")
    }

    fun clearChat(profileId: String) {
        viewModelScope.launch {
            val messages = activeChatMessages.value
            messages.forEach { repository.deleteMessage(it) }
            addSystemNotification("Chat Cleared", "Conversation history with this user has been erased.")
        }
    }

    fun forwardChatMessage(message: ChatMessage, targetProfileId: String) {
        viewModelScope.launch {
            val forwardMsg = ChatMessage(
                chatWithProfileId = targetProfileId,
                isFromMe = true,
                text = message.text,
                mediaType = message.mediaType,
                mediaUrl = message.mediaUrl,
                forwardedFrom = message.forwardedFrom ?: "Original Sender" // Simplified
            )
            repository.insertMessage(forwardMsg)
        }
    }

    fun forwardChatMessages(messages: List<ChatMessage>, targetProfileId: String) {
        viewModelScope.launch {
            messages.forEach { message ->
                val forwardMsg = ChatMessage(
                    chatWithProfileId = targetProfileId,
                    isFromMe = true,
                    text = message.text,
                    mediaType = message.mediaType,
                    mediaUrl = message.mediaUrl,
                    forwardedFrom = message.forwardedFrom ?: "Original Sender"
                )
                repository.insertMessage(forwardMsg)
            }
        }
    }

    fun submitClientReview(profileId: String, reviewerName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val rev = UserReview(
                targetProfileId = profileId,
                reviewerId = "me_user",
                reviewerName = reviewerName,
                rating = rating,
                reviewText = comment
            )
            repository.insertReview(rev)
            addSystemNotification("Review Added", "Your review has been successfully submitted.")
        }
    }

    fun incrementViewsCount(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(profileViewsCount = prof.profileViewsCount + 1))
            }
        }
    }

    // Safety and reports
    fun reportUserProfile(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isReported = true))
                addSystemNotification("Report Submitted", "You reported ${prof.name}. jobaaya compliance system is auditing their activity log.")
            }
        }
    }

    fun blockUserProfile(profileId: String) {
         viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isBlocked = true))
                addSystemNotification("User Blocked", "${prof.name} has been removed from all lists.")
            }
        }
    }

    fun unblockUserProfile(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isBlocked = false))
                addSystemNotification("User Unblocked", "${prof.name} is now visible again.")
            }
        }
    }

    // Admin commands
    fun adminToggleVerification(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isVerified = !prof.isVerified))
                addSystemNotification("Verification Updated", "${prof.name} verification status is altered.")
            }
        }
    }

    fun adminPardonUser(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(isReported = false, isBlocked = false))
            }
        }
    }

    // Note actions
    fun saveUtilityNote(title: String, content: String, id: Int = 0, bgColor: Long = 0xFFFFFFFF, font: String = "Normal", fontColor: Long = 0xFF000000, textAlign: String = "Left", isBold: Boolean = false, isItalic: Boolean = false, isLocked: Boolean = false, lockPin: String? = null, reminderTimestamp: Long? = null) {
        viewModelScope.launch {
            repository.insertNote(UtilityNote(id = id, title = title, content = content, backgroundColor = bgColor, fontStyle = font, fontColor = fontColor, textAlign = textAlign, isBold = isBold, isItalic = isItalic, isLocked = isLocked, lockPin = lockPin, reminderTimestamp = reminderTimestamp))
        }
    }

    fun deleteUtilityNote(note: UtilityNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun markNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // Partnership Deals
    fun getMyDeals(): StateFlow<List<PartnershipDeal>> {
        val myId = myProfile.value?.id ?: ""
        return repository.getMyDeals(myId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getDealById(dealId: Int) = repository.getDealById(dealId)
    fun getDealMessages(dealId: Int) = repository.getDealMessages(dealId)
    fun getAuditLogs(dealId: Int) = repository.getAuditLogs(dealId)

    fun startNewDeal(proId: String) {
        viewModelScope.launch {
            val me = myProfile.value ?: return@launch
            val deal = PartnershipDeal(partnerId = me.id, proId = proId)
            val id = repository.createDeal(deal)
            repository.insertAuditLog(DealAuditLog(dealId = id.toInt(), userId = me.id, action = "Deal Created"))
            addSystemNotification("Deal Started", "A new partnership proposal has been initialized.")
        }
    }

    fun updateDeal(deal: PartnershipDeal, action: String) {
        viewModelScope.launch {
            repository.updateDeal(deal)
            repository.insertAuditLog(DealAuditLog(dealId = deal.id, userId = myProfile.value?.id ?: "", action = action))
            
            val notificationTitle = when(action) {
                "Deal Done" -> "Deal Finalized"
                "Edit Requested" -> "Edit Requested"
                "Edit Approved" -> "Deal Editable Again"
                else -> "Deal Updated"
            }
            addSystemNotification(notificationTitle, "Deal #${deal.id}: $action by user.")
        }
    }

    fun sendDealMessage(dealId: Int, text: String) {
        viewModelScope.launch {
            val me = myProfile.value ?: return@launch
            repository.insertDealMessage(DealMessage(dealId = dealId, senderId = me.id, text = text))
        }
    }

    fun getProfileMedia(profileId: String) = repository.getMediaForProfile(profileId)

    fun getProfileReviews(profileId: String) = repository.getReviewsForProfile(profileId)

    fun addProfileMedia(media: com.example.data.model.ProfileMedia) {
        viewModelScope.launch {
            repository.insertMedia(media)
        }
    }

    fun deleteProfileMedia(media: com.example.data.model.ProfileMedia) {
        viewModelScope.launch {
            repository.deleteMedia(media)
        }
    }

    // Helper notifications list add
    private fun addSystemNotification(title: String, message: String) {
        viewModelScope.launch {
            repository.insertNotification(SystemNotification(title = title, content = message))
        }
    }

    fun setSearchQuery(q: String) {
        _searchQuery.value = q
    }

    fun setFilterAvailability(status: String) {
        _filterAvailability.value = status
    }

    fun setFilterRating(r: Float) {
        _filterRating.value = r
    }

    fun setFilterExperience(years: Int) {
        _filterExperience.value = years
    }

    fun setFilterLanguage(lang: String) {
        _filterLanguage.value = lang
    }

    fun setFilterDistance(distance: Float) {
        _filterDistanceKm.value = distance
    }

    // App Settings states
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fontSizeMultiplier = MutableStateFlow(1.0f)
    val fontSizeMultiplier: StateFlow<Float> = _fontSizeMultiplier.asStateFlow()

    private val _isMobilePublic = MutableStateFlow(true)
    val isMobilePublic: StateFlow<Boolean> = _isMobilePublic.asStateFlow()

    private val _isAccountPrivate = MutableStateFlow(false)
    val isAccountPrivate: StateFlow<Boolean> = _isAccountPrivate.asStateFlow()

    private val _serviceRadius = MutableStateFlow(20f)
    val serviceRadius: StateFlow<Float> = _serviceRadius.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setFontSizeMultiplier(multiplier: Float) {
        _fontSizeMultiplier.value = multiplier
    }

    fun setMobilePublic(enabled: Boolean) {
        _isMobilePublic.value = enabled
    }

    fun setAccountPrivate(enabled: Boolean) {
        _isAccountPrivate.value = enabled
    }

    fun setServiceRadius(radius: Float) {
        _serviceRadius.value = radius
    }

    fun handleLogout() {
        authRepository.logout()
        _otpDispatched.value = false
        _onboardingStep.value = false
    }

    fun startOnboarding() {
        _onboardingStep.value = true
    }

    // Great Spherical Cosine formula (Haversine approximation)
    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}

data class ChatInbox(
    val partnerProfile: UserProfile,
    val lastMessage: ChatMessage,
    val unreadCount: Int
)
