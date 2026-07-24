package com.example.data.repository

import android.util.Log
import com.example.data.database.ChatMessageDao
import com.example.data.database.ProfileMediaDao
import com.example.data.database.SubscriptionDao
import com.example.data.database.SystemNotificationDao
import com.example.data.database.UserConnectionDao
import com.example.data.database.UserProfileDao
import com.example.data.database.UserReviewDao
import com.example.data.database.UtilityNoteDao
import com.example.data.database.PartnershipDealDao
import com.example.data.model.ChatMessage
import com.example.data.model.ContactMessage
import com.example.data.model.DealAuditLog
import com.example.data.model.DealMessage
import com.example.data.model.PartnershipDeal
import com.example.data.model.ProfileMedia
import com.example.data.model.Subscription
import com.example.data.model.SystemNotification
import com.example.data.model.UserConnection
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.UUID

class JobaayaRepository(
    private val profileDao: UserProfileDao,
    private val reviewDao: UserReviewDao,
    private val msgDao: ChatMessageDao,
    private val connectionDao: UserConnectionDao,
    private val subDao: SubscriptionDao,
    private val mediaDao: ProfileMediaDao,
    private val noteDao: UtilityNoteDao,
    private val notificationDao: SystemNotificationDao,
    private val dealDao: PartnershipDealDao,
    private val productDao: com.example.data.database.ProductDao
) {
    val otherProfiles: Flow<List<UserProfile>> = profileDao.getOtherProfiles()
    val myProfile: Flow<UserProfile?> = profileDao.getMyProfileFlow()

    // Products logic
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    fun getProductsBySeller(sellerId: String) = productDao.getProductsBySeller(sellerId)
    fun searchProducts(query: String) = productDao.searchProducts(query)
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)

    suspend fun getMyProfileDirect(): UserProfile? = profileDao.getMyProfileDirect()

    suspend fun insertProfile(profile: UserProfile) = profileDao.insertProfile(profile)

    suspend fun updateProfile(profile: UserProfile) = profileDao.updateProfile(profile)

    // Reviews
    fun getReviewsForProfile(profileId: String): Flow<List<UserReview>> =
        reviewDao.getReviewsForProfile(profileId)

    suspend fun insertReview(review: UserReview) {
        reviewDao.insertReview(review)
        val profile = profileDao.getProfileByIdDirect(review.targetProfileId)
        if (profile != null) {
            val allReviews = reviewDao.getReviewsForProfile(review.targetProfileId).firstOrNull() ?: emptyList()
            val newReviewCount = allReviews.size
            val sumRating = allReviews.sumOf { it.rating.toDouble() }.toFloat()
            val avgRating = if (newReviewCount > 0) sumRating / newReviewCount else profile.averageRating
            profileDao.updateProfile(profile.copy(
                averageRating = avgRating,
                reviewCount = newReviewCount
            ))
        }
    }

    // Chat
    fun getChatMessages(profileId: String): Flow<List<ChatMessage>> = msgDao.getChatMessages(profileId)

    val allMessages: Flow<List<ChatMessage>> = msgDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage) = msgDao.insertMessage(message)

    suspend fun updateMessage(message: ChatMessage) = msgDao.updateMessage(message)

    suspend fun deleteMessage(message: ChatMessage) = msgDao.deleteMessage(message)

    suspend fun markChatAsRead(profileId: String) = msgDao.markChatAsRead(profileId, System.currentTimeMillis())

    // Connections

    suspend fun toggleConnection(userId: String, connectionId: String) {
        if (connectionDao.isConnected(userId, connectionId)) {
            connectionDao.deleteConnection(userId, connectionId)
        } else {
            connectionDao.insertConnection(UserConnection(userId, connectionId))
        }
    }

    // Media
    fun getMediaForProfile(profileId: String): Flow<List<ProfileMedia>> = mediaDao.getMediaForProfile(profileId)

    suspend fun insertMedia(media: ProfileMedia) = mediaDao.insertMedia(media)

    suspend fun deleteMedia(media: ProfileMedia) = mediaDao.deleteMedia(media)

    // Notes
    val allNotes: Flow<List<UtilityNote>> = noteDao.getAllNotes()

    suspend fun insertNote(note: UtilityNote) = noteDao.insertNote(note)

    suspend fun deleteNote(note: UtilityNote) = noteDao.deleteNote(note)

    // Notifications (local, in-app list)
    val allNotifications: Flow<List<SystemNotification>> = notificationDao.getAllNotifications()

    suspend fun insertNotification(notification: SystemNotification) = notificationDao.insertNotification(notification)

    suspend fun markAllNotificationsAsRead() = notificationDao.markAllAsRead()

    suspend fun clearAllNotifications() = notificationDao.clearAll()

    suspend fun deleteNotification(notification: SystemNotification) = notificationDao.deleteNotification(notification)

    // ==========================================
    // PUSH NOTIFICATION TRIGGER (NEW - Phase 2)
    // ==========================================
    // Writes a lightweight "please notify this user" record to Firestore.
    // A Cloud Function (Phase 3) listens for new documents in this collection,
    // looks up the target user's fcmToken from users/{targetUserId}, sends the
    // push notification, then marks/deletes this trigger document.
    // This does NOT store any chat/review/deal content itself - just enough
    // to know who to notify and what short message to show.
    suspend fun submitContactMessage(contactMsg: ContactMessage): Result<Unit> {
        return try {
            FirebaseFirestore.getInstance()
                .collection("contact_messages")
                .add(
                    mapOf(
                        "message" to contactMsg.message,
                        "userId" to contactMsg.userId,
                        "userName" to contactMsg.userName,
                        "registeredMobile" to contactMsg.registeredMobile,
                        "email" to contactMsg.email,
                        "deviceModel" to contactMsg.deviceModel,
                        "androidVersion" to contactMsg.androidVersion,
                        "appVersion" to contactMsg.appVersion,
                        "status" to contactMsg.status,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun pushNotificationTrigger(
        targetUserId: String,
        type: String,
        title: String,
        body: String
    ) {
        if (targetUserId.isBlank()) return
        try {
            FirebaseFirestore.getInstance()
                .collection("notification_triggers")
                .add(
                    mapOf(
                        "targetUserId" to targetUserId,
                        "type" to type,
                        "title" to title,
                        "body" to body,
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "status" to "pending"
                    )
                )
        } catch (e: Exception) {
            Log.e("JobaayaRepository", "pushNotificationTrigger error", e)
        }
    }

    fun getDealMessages(dealId: Int): Flow<List<DealMessage>> = dealDao.getDealMessages(dealId)

    suspend fun insertDealMessage(msg: DealMessage) = dealDao.insertDealMessage(msg)

    fun getMyDeals(userId: String): Flow<List<PartnershipDeal>> = dealDao.getMyDeals(userId)

    fun getDealById(dealId: Int): Flow<PartnershipDeal?> = dealDao.getDealById(dealId)

    suspend fun createDeal(deal: PartnershipDeal): Long = dealDao.insertDeal(deal)

    suspend fun updateDeal(deal: PartnershipDeal) = dealDao.updateDeal(deal)

    suspend fun insertAuditLog(log: DealAuditLog) = dealDao.insertAuditLog(log)

    fun getAuditLogs(dealId: Int) = dealDao.getAuditLogs(dealId)

    suspend fun seedDatabaseIfEmpty() {
        val existing = profileDao.getOtherProfiles().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val seedProfiles = listOf(
                UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Amit Sharma",
                    profession = "Electrician",
                    skillsRaw = "Wiring, Repair, Home Appliance Fixing",
                    mobileNumber = "+91 98765 43210",
                    emailAddress = "amit@jobaaya.com",
                    fullAddress = "Lajpat Nagar, Delhi",
                    latitude = 28.5684,
                    longitude = 77.2435,
                    yearsOfExperience = 8,
                    languagesRaw = "Hindi, English",
                    aboutSection = "Expert residential electrician with 8 years of experience in Delhi area.",
                    isVerified = true,
                    averageRating = 4.8f,
                    reviewCount = 1
                ),
                UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Priya Nair",
                    profession = "Nutritionist",
                    skillsRaw = "Dietetics, Weight Management",
                    mobileNumber = "+91 87654 32109",
                    emailAddress = "priya@jobaaya.com",
                    fullAddress = "Vasant Kunj, Delhi",
                    latitude = 28.5244,
                    longitude = 77.1558,
                    yearsOfExperience = 6,
                    languagesRaw = "Hindi, English",
                    aboutSection = "Certified clinical nutritionist specializing in weight management.",
                    isVerified = true,
                    averageRating = 4.9f,
                    reviewCount = 1
                ),
                UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Rahul Verma",
                    profession = "Software Engineer",
                    skillsRaw = "Kotlin, Android, Firebase, Jetpack Compose",
                    mobileNumber = "+91 99988 77766",
                    emailAddress = "rahul@jobaaya.com",
                    fullAddress = "Cyber City, Gurgaon",
                    latitude = 28.4950,
                    longitude = 77.0890,
                    yearsOfExperience = 5,
                    languagesRaw = "English, Hindi",
                    aboutSection = "Professional Android Developer with 5 years experience in building high-scale apps.",
                    isVerified = true,
                    averageRating = 5.0f,
                    reviewCount = 3
                ),
                UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    name = "Suresh Khanna",
                    profession = "IT Professional",
                    skillsRaw = "Networking, CCTV, Laptop Repair, System Admin",
                    mobileNumber = "+91 91122 33344",
                    emailAddress = "suresh@jobaaya.com",
                    fullAddress = "Rohini, Delhi",
                    latitude = 28.7041,
                    longitude = 77.1025,
                    yearsOfExperience = 10,
                    languagesRaw = "Hindi, English",
                    aboutSection = "IT infrastructure and hardware specialist providing 24/7 support services.",
                    isVerified = true,
                    averageRating = 4.7f,
                    reviewCount = 12
                )
            )
            profileDao.insertProfiles(seedProfiles)

            notificationDao.insertNotification(SystemNotification(title = "System Alert", content = "Welcome to jobaaya! Set up your multi-trade business or professional card today."))
            notificationDao.insertNotification(SystemNotification(title = "Network Insight", content = "Professional connections in Delhi are growing. Connect with Amit Sharma to expand your reach."))
            notificationDao.insertNotification(SystemNotification(title = "Security Tip", content = "Keep your profile verified to gain more client trust and higher rankings."))
        }

        val notifyExisting = notificationDao.getAllNotifications().firstOrNull()
        if (notifyExisting.isNullOrEmpty()) {
            notificationDao.insertNotification(SystemNotification(title = "System Alert", content = "Welcome to jobaaya! Your activity log is ready."))
        }

        val me = profileDao.getMyProfileDirect()
        if (me == null) {
            profileDao.insertProfile(UserProfile(
                id = java.util.UUID.randomUUID().toString(),
                name = "", // Empty name to force onboarding
                profession = "",
                skillsRaw = "",
                mobileNumber = "",
                emailAddress = "",
                fullAddress = "",
                latitude = 28.7159,
                longitude = 77.1006,
                yearsOfExperience = 0,
                languagesRaw = "",
                aboutSection = "",
                isMe = true
            ))
        }
    }
}