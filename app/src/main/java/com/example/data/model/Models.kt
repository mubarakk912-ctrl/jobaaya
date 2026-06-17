package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AccountType {
    CUSTOMER,
    PROFESSIONAL,
    BUSINESS
}

enum class WorkStatus {
    AVAILABLE,
    BUSY,
    OFFLINE
}

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val profession: String,
    val skillsRaw: String, // Comma separated list of skills
    val mobileNumber: String,
    val emailAddress: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val isLocationPublic: Boolean = true,
    val yearsOfExperience: Int,
    val languagesRaw: String, // Comma separated languages
    val aboutSection: String,
    val profilePhotoUrl: String = "", // Holds simulated resource identifier or local uri
    val accountType: String = AccountType.PROFESSIONAL.name,
    val availabilityStatus: String = WorkStatus.AVAILABLE.name,
    val workingHours: String = "09:00 - 17:00",
    val isVerified: Boolean = false,
    val isMe: Boolean = false,
    val followStatus: Int = 0, // 0: None, 1: Requested, 2: Connected/Followed
    val bookmarkStatus: Boolean = false,
    val profileViewsCount: Int = 0,
    val interactionsCount: Int = 0,
    val isBlocked: Boolean = false,
    val isReported: Boolean = false,
    val averageRating: Float = 4.5f,
    val reviewCount: Int = 0
) {
    val skills: List<String>
        get() = if (skillsRaw.isBlank()) emptyList() else skillsRaw.split(",").map { it.trim() }

    val languages: List<String>
        get() = if (languagesRaw.isBlank()) emptyList() else languagesRaw.split(",").map { it.trim() }
}

@Entity(tableName = "user_reviews")
data class UserReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetProfileId: String,
    val reviewerName: String,
    val rating: Float,
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatWithProfileId: String,
    val isFromMe: Boolean,
    val text: String,
    val mediaType: String? = null, // "VOICE", "PHOTO", "VIDEO", "DOCUMENT", or null for text
    val mediaUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "utility_notes")
data class UtilityNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
