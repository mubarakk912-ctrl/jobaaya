package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["mobileNumber"], unique = true), Index(value = ["emailAddress"], unique = true)]
)
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
    val profilePhotoUrl: String = "",
    val accountType: String = AccountType.PROFESSIONAL.name,
    val availabilityStatus: String = WorkStatus.AVAILABLE.name,
    val workingHours: String = "09:00 - 17:00",
    val isVerified: Boolean = false,
    val isMe: Boolean = false,
    val averageRating: Float = 0.0f,
    val reviewCount: Int = 0,
    val lastSeen: Long = System.currentTimeMillis(),
    
    // UI and Interaction states
    val interactionsCount: Int = 0,
    val profileViewsCount: Int = 0,
    val isBlocked: Boolean = false,
    val isReported: Boolean = false,
    val bookmarkStatus: Boolean = false,
    val followStatus: Int = 0, // 0: None, 1: Requested, 2: Connected
    val isVerifiedPartner: Boolean = false,
    val partnerRating: Float = 0.0f
) {
    val skills: List<String>
        get() = if (skillsRaw.isBlank()) emptyList() else skillsRaw.split(",").map { it.trim() }

    val languages: List<String>
        get() = if (languagesRaw.isBlank()) emptyList() else languagesRaw.split(",").map { it.trim() }
}

@Entity(
    tableName = "user_reviews",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["targetProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["targetProfileId"])]
)
data class UserReview(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetProfileId: String,
    val reviewerId: String,
    val reviewerName: String,
    val rating: Float,
    val reviewText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["chatWithProfileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatWithProfileId"]), Index(value = ["timestamp"])]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatWithProfileId: String,
    val isFromMe: Boolean,
    val text: String,
    val mediaType: String? = null,
    val mediaUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val forwardedFrom: String? = null, // Store name or ID of original sender
    val readAt: Long? = null,
    val replyToId: Int? = null,
    val replyToText: String? = null
)

@Entity(
    tableName = "user_connections",
    primaryKeys = ["userId", "connectionId"],
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["connectionId"])]
)
data class UserConnection(
    val userId: String,
    val connectionId: String,
    val status: Int = 2,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subscriptions",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Subscription(
    @PrimaryKey val userId: String,
    val planType: String,
    val expiryTimestamp: Long,
    val isActive: Boolean = true
)

@Entity(
    tableName = "profile_media",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class ProfileMedia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: String,
    val mediaType: String,
    val mediaUrl: String,
    val description: String? = null
)

@Entity(tableName = "utility_notes")
data class UtilityNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val backgroundColor: Long = 0xFFFFFFFF,
    val fontStyle: String = "Normal",
    val fontColor: Long = 0xFF000000,
    val textAlign: String = "Left", // Left, Center, Right
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isLocked: Boolean = false,
    val lockPin: String? = null,
    val reminderTimestamp: Long? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_notifications")
data class SystemNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(
    tableName = "partnership_deals",
    foreignKeys = [
        ForeignKey(entity = UserProfile::class, parentColumns = ["id"], childColumns = ["partnerId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserProfile::class, parentColumns = ["id"], childColumns = ["proId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("partnerId"), Index("proId")]
)
data class PartnershipDeal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partnerId: String,
    val proId: String,
    val title: String = "",
    val description: String = "",
    val workType: String = "",
    val terms: String = "",
    val notes: String = "",
    val commissionPercentage: Int = 5,
    val status: String = "Draft", // Draft, Pending Approval, Deal Done, Edited, Cancelled
    val partnerAgreed: Boolean = false,
    val proAgreed: Boolean = false,
    val partnerDone: Boolean = false,
    val proDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val finalizedAt: Long? = null,
    val editRequestActive: Boolean = false,
    val editRequestFromId: String? = null
)

@Entity(
    tableName = "deal_messages",
    foreignKeys = [
        ForeignKey(entity = PartnershipDeal::class, parentColumns = ["id"], childColumns = ["dealId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("dealId")]
)
data class DealMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dealId: Int,
    val senderId: String,
    val text: String,
    val attachmentUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "deal_audit_logs",
    foreignKeys = [
        ForeignKey(entity = PartnershipDeal::class, parentColumns = ["id"], childColumns = ["dealId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("dealId")]
)
data class DealAuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dealId: Int,
    val userId: String,
    val action: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
