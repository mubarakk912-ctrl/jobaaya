package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.ChatMessage
import com.example.data.model.ProfileMedia
import com.example.data.model.Subscription
import com.example.data.model.SystemNotification
import com.example.data.model.UserConnection
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
import com.example.data.model.PartnershipDeal
import com.example.data.model.DealMessage
import com.example.data.model.DealAuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE isMe = 0 ORDER BY averageRating DESC")
    fun getOtherProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profiles WHERE isMe = 1 LIMIT 1")
    fun getMyProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE isMe = 1 LIMIT 1")
    suspend fun getMyProfileDirect(): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    fun getProfileById(id: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileByIdDirect(id: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<UserProfile>)

    @Update
    suspend fun updateProfile(profile: UserProfile)

    @Delete
    suspend fun deleteProfile(profile: UserProfile)
    
    @Query("SELECT * FROM user_profiles WHERE isMe = 0 AND (name LIKE '%' || :query || '%' OR profession LIKE '%' || :query || '%' OR skillsRaw LIKE '%' || :query || '%' OR fullAddress LIKE '%' || :query || '%')")
    fun searchProfiles(query: String): Flow<List<UserProfile>>
}

@Dao
interface UserReviewDao {
    @Query("SELECT * FROM user_reviews WHERE targetProfileId = :profileId ORDER BY timestamp DESC")
    fun getReviewsForProfile(profileId: String): Flow<List<UserReview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: UserReview)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chatWithProfileId = :profileId ORDER BY timestamp ASC")
    fun getChatMessages(profileId: String): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Update
    suspend fun updateMessage(message: ChatMessage)

    @Delete
    suspend fun deleteMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET isRead = 1, readAt = :readAt WHERE chatWithProfileId = :profileId AND isFromMe = 0 AND isRead = 0")
    suspend fun markChatAsRead(profileId: String, readAt: Long = System.currentTimeMillis())

    @Query("UPDATE chat_messages SET isRead = 1 WHERE chatWithProfileId = :profileId AND isFromMe = 1")
    suspend fun markMyMessagesAsReadByOther(profileId: String)
}

@Dao
interface UserConnectionDao {
    @Query("SELECT * FROM user_connections WHERE userId = :userId OR connectionId = :userId")
    fun getConnections(userId: String): Flow<List<UserConnection>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: UserConnection)

    @Query("DELETE FROM user_connections WHERE (userId = :userId AND connectionId = :connectionId) OR (userId = :connectionId AND connectionId = :userId)")
    suspend fun deleteConnection(userId: String, connectionId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_connections WHERE (userId = :userId AND connectionId = :connectionId) OR (userId = :connectionId AND connectionId = :userId) LIMIT 1)")
    suspend fun isConnected(userId: String, connectionId: String): Boolean
}

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId LIMIT 1")
    fun getSubscription(userId: String): Flow<Subscription?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: Subscription)
}

@Dao
interface ProfileMediaDao {
    @Query("SELECT * FROM profile_media WHERE profileId = :profileId")
    fun getMediaForProfile(profileId: String): Flow<List<ProfileMedia>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: ProfileMedia)

    @Delete
    suspend fun deleteMedia(media: ProfileMedia)
}

@Dao
interface UtilityNoteDao {
    @Query("SELECT * FROM utility_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<UtilityNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: UtilityNote)

    @Delete
    suspend fun deleteNote(note: UtilityNote)
}

@Dao
interface SystemNotificationDao {
    @Query("SELECT * FROM system_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<SystemNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SystemNotification)

    @Query("UPDATE system_notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM system_notifications")
    suspend fun clearAll()
}

@Dao
interface PartnershipDealDao {
    @Query("SELECT * FROM partnership_deals WHERE partnerId = :userId OR proId = :userId ORDER BY createdAt DESC")
    fun getMyDeals(userId: String): Flow<List<PartnershipDeal>>

    @Query("SELECT * FROM partnership_deals WHERE id = :dealId")
    fun getDealById(dealId: Int): Flow<PartnershipDeal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeal(deal: PartnershipDeal): Long

    @Update
    suspend fun updateDeal(deal: PartnershipDeal)

    @Delete
    suspend fun deleteDeal(deal: PartnershipDeal)

    @Query("SELECT * FROM deal_messages WHERE dealId = :dealId ORDER BY timestamp ASC")
    fun getDealMessages(dealId: Int): Flow<List<DealMessage>>

    @Insert
    suspend fun insertDealMessage(message: DealMessage)

    @Query("SELECT * FROM deal_audit_logs WHERE dealId = :dealId ORDER BY timestamp DESC")
    fun getAuditLogs(dealId: Int): Flow<List<DealAuditLog>>

    @Insert
    suspend fun insertAuditLog(log: DealAuditLog)
}

@Database(
    entities = [
        UserProfile::class, 
        UserReview::class, 
        ChatMessage::class, 
        UtilityNote::class, 
        UserConnection::class, 
        Subscription::class, 
        ProfileMedia::class,
        SystemNotification::class,
        PartnershipDeal::class,
        DealMessage::class,
        DealAuditLog::class
    ],
    version = 23,
    exportSchema = false
)
abstract class JobaayaDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val userReviewDao: UserReviewDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val userConnectionDao: UserConnectionDao
    abstract val subscriptionDao: SubscriptionDao
    abstract val profileMediaDao: ProfileMediaDao
    abstract val utilityNoteDao: UtilityNoteDao
    abstract val systemNotificationDao: SystemNotificationDao
    abstract val partnershipDealDao: PartnershipDealDao

    companion object {
        @Volatile
        private var INSTANCE: JobaayaDatabase? = null

        fun getDatabase(context: android.content.Context): JobaayaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    JobaayaDatabase::class.java,
                    "jobaaya_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
