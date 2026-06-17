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
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
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

    @Query("UPDATE chat_messages SET isRead = 1 WHERE chatWithProfileId = :profileId AND isFromMe = 0")
    suspend fun markChatAsRead(profileId: String)
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

@Database(
    entities = [UserProfile::class, UserReview::class, ChatMessage::class, UtilityNote::class],
    version = 1,
    exportSchema = false
)
abstract class JobaayaDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val userReviewDao: UserReviewDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val utilityNoteDao: UtilityNoteDao
}
