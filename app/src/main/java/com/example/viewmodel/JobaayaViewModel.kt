package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.database.JobaayaDatabase
import com.example.data.model.AccountType
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
import com.example.data.model.WorkStatus
import com.example.data.repository.JobaayaRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private data class FilterSet1(val query: String, val avail: String, val rating: Float, val exp: Int)
private data class FilterSet2(val lang: String, val dist: Float, val myProf: UserProfile?)

class JobaayaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        JobaayaDatabase::class.java,
        "jobaaya_database"
    ).fallbackToDestructiveMigration().build()

    private val repository = JobaayaRepository(
        db.userProfileDao,
        db.userReviewDao,
        db.chatMessageDao,
        db.utilityNoteDao
    )

    // Current app-wide language state
    private val _currentLanguage = MutableStateFlow(JobaayaLocalization.detectDeviceLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Auth states
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _otpDispatched = MutableStateFlow(false)
    val otpDispatched: StateFlow<Boolean> = _otpDispatched.asStateFlow()

    private val _loginMobileNumber = MutableStateFlow("")
    val loginMobileNumber: StateFlow<String> = _loginMobileNumber.asStateFlow()

    private val _onboardingStep = MutableStateFlow(false) // If true, show register questionnaire
    val onboardingStep: StateFlow<Boolean> = _onboardingStep.asStateFlow()

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
    ) { profiles, set1, set2 ->
        val query = set1.query
        val availability = set1.avail
        val rating = set1.rating
        val exp = set1.exp
        
        val lang = set2.lang
        val distance = set2.dist
        val myProf = set2.myProf

        profiles.filter { profile ->
            // Skip blocked profiles
            if (profile.isBlocked) return@filter false

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

            matchesQuery && matchesAvailability && matchesRating && matchesExperience && matchesLanguage && matchesDistance
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Chat handling
    private val _activeChatUserId = MutableStateFlow<String?>(null)
    val activeChatUserId: StateFlow<String?> = _activeChatUserId.asStateFlow()

    val activeChatMessages: StateFlow<List<ChatMessage>> = _activeChatUserId.flatMapLatest { profileId ->
        if (profileId.isNullOrEmpty()) {
            flowOf(emptyList())
        } else {
            repository.getChatMessages(profileId)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic inbox messages map (latest message per user)
    val chatInboxList: StateFlow<List<ChatInbox>> = repository.allMessages.combine(repository.otherProfiles) { msgList, profiles ->
        val grouped = msgList.groupBy { it.chatWithProfileId }
        grouped.mapNotNull { (profileId, messages) ->
            val profile = profiles.find { it.id == profileId } ?: return@mapNotNull null
            val latest = messages.maxByOrNull { it.timestamp } ?: return@mapNotNull null
            val unreadCount = messages.count { !it.isRead && !it.isFromMe }
            ChatInbox(profile, latest, unreadCount)
        }.sortedByDescending { it.lastMessage.timestamp }
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
    private val _notifications = MutableStateFlow<List<ActivityNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Add custom dynamic notifications
            _notifications.value = listOf(
                ActivityNotification("System Alert", "Welcome to JOBAAYA! Set up your multi-trade business or professional card today.", System.currentTimeMillis() - 500000),
                ActivityNotification("Lead Generation", "Amit Sharma (Electrician) is available for nearby bookings in Delhi.", System.currentTimeMillis() - 1200000)
            )
        }
    }

    // Change Language from UI
    fun changeLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    // Auth simulation APIs
    fun setLoginMobile(mobile: String) {
        _loginMobileNumber.value = mobile
    }

    fun triggerSendMockOTP() {
        if (_loginMobileNumber.value.isNotBlank()) {
            _otpDispatched.value = true
            addSystemNotification("OTP Request", "Your 6-digit Jobaaya OTP verification code is 422045")
        }
    }

    fun verifyMockOTP(otp: String) {
        if (otp == "422045" || otp == "123456" || otp.length == 6) {
            viewModelScope.launch {
                val myProfileDirect = repository.getMyProfileDirect()
                if (myProfileDirect == null || myProfileDirect.name == "Guest User" || myProfileDirect.name.isBlank()) {
                    _onboardingStep.value = true
                } else {
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun simulateEmailLogin(email: String) {
        viewModelScope.launch {
            _isLoggedIn.value = true
        }
    }

    fun simulateGoogleSignIn() {
        viewModelScope.launch {
            _isLoggedIn.value = true
        }
    }

    fun completeOnboardingRegistration(name: String, profession: String, skills: String, accountType: AccountType, email: String, address: String, exp: Int, languages: String) {
        viewModelScope.launch {
            val existing = repository.getMyProfileDirect()
            val finalId = existing?.id ?: "me_user"
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
                isMe = true,
                isVerified = false,
                availabilityStatus = WorkStatus.AVAILABLE.name
            )
            repository.insertProfile(updated)
            _onboardingStep.value = false
            _isLoggedIn.value = true
            addSystemNotification("Profile Configured", "Your profile cards have been published! Welcome to JOBAAYA.")
        }
    }

    fun updateMyProfessionalProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            addSystemNotification("Profile Alert", "Your availability and location coordinates was modified successfully.")
        }
    }

    // Toggle Bookmarks/Favorites
    fun toggleBookmarkProfile(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                repository.updateProfile(prof.copy(bookmarkStatus = !prof.bookmarkStatus))
            }
        }
    }

    // Connect/Follow trigger
    fun toggleConnectWithUser(profileId: String) {
        viewModelScope.launch {
            val prof = db.userProfileDao.getProfileByIdDirect(profileId)
            if (prof != null) {
                val nextStatus = when (prof.followStatus) {
                    0 -> 2 // Connected
                    2 -> 0 // Disconnected
                    else -> 0
                }
                repository.updateProfile(prof.copy(
                    followStatus = nextStatus,
                    interactionsCount = prof.interactionsCount + (if (nextStatus == 2) 1 else 0)
                ))
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

    fun sendChatMessage(text: String, mediaType: String? = null, mediaUri: String? = null) {
        val destId = _activeChatUserId.value
        if (!destId.isNullOrEmpty()) {
            viewModelScope.launch {
                val msg = ChatMessage(
                    chatWithProfileId = destId,
                    isFromMe = true,
                    text = text,
                    mediaType = mediaType,
                    mediaUri = mediaUri
                )
                repository.insertMessage(msg)
                
                // Track interaction count
                val other = db.userProfileDao.getProfileByIdDirect(destId)
                if (other != null) {
                    repository.updateProfile(other.copy(interactionsCount = other.interactionsCount + 1))
                }

                // Simulate reply in a short delay
                simulateOtherUserReply(destId, text)
            }
        }
    }

    private fun simulateOtherUserReply(partnerId: String, textSent: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1800)
            val name = db.userProfileDao.getProfileByIdDirect(partnerId)?.name ?: "Professional"
            val replyText = when {
                textSent.contains("price", ignoreCase = true) || textSent.contains("how much", ignoreCase = true) || textSent.contains("rate", ignoreCase = true) -> {
                    "Sure! I charge approximately $50 per hour. You can use our built-in Calculator in JOBAAYA utilities tab to generate an exact quotation or taxes estimate! Let me know."
                }
                textSent.contains("location", ignoreCase = true) || textSent.contains("where", ignoreCase = true) -> {
                    "You can view my location markers in real-time under the JOBAAYA 'Near Me' maps view. Feel free to initiate path routing directions!"
                }
                textSent.contains("available", ignoreCase = true) || textSent.contains("time", ignoreCase = true) -> {
                    "Yes, I am available as per my listed working hours on my card! Let's schedule the session now."
                }
                else -> "Thank you! I received your inquiry text. I am reviewing the details of your service session requirements right now."
            }
            val msgReply = ChatMessage(
                chatWithProfileId = partnerId,
                isFromMe = false,
                text = replyText
            )
            repository.insertMessage(msgReply)
            addSystemNotification("New Chat Message", "$name sent you a message: \"$replyText\"")
        }
    }

    fun submitClientReview(profileId: String, reviewerName: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val rev = UserReview(
                targetProfileId = profileId,
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
                addSystemNotification("Report Submitted", "You reported ${prof.name}. Jobaaya compliance system is auditing their activity log.")
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
    fun saveUtilityNote(title: String, content: String) {
        viewModelScope.launch {
            repository.insertNote(UtilityNote(title = title, content = content))
        }
    }

    fun deleteUtilityNote(note: UtilityNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // Helper notifications list add
    private fun addSystemNotification(title: String, message: String) {
        val next = _notifications.value.toMutableList()
        next.add(0, ActivityNotification(title, message, System.currentTimeMillis()))
        _notifications.value = next
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

    fun handleLogout() {
        _isLoggedIn.value = false
        _otpDispatched.value = false
        _onboardingStep.value = false
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

data class ActivityNotification(
    val title: String,
    val text: String,
    val timestamp: Long
)
