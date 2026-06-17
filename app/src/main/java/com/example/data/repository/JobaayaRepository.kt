package com.example.data.repository

import com.example.data.database.ChatMessageDao
import com.example.data.database.UserProfileDao
import com.example.data.database.UserReviewDao
import com.example.data.database.UtilityNoteDao
import com.example.data.model.AccountType
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.model.UserReview
import com.example.data.model.UtilityNote
import com.example.data.model.WorkStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class JobaayaRepository(
    private val userProfileDao: UserProfileDao,
    private val userReviewDao: UserReviewDao,
    private val chatMessageDao: ChatMessageDao,
    private val utilityNoteDao: UtilityNoteDao
) {
    val otherProfiles: Flow<List<UserProfile>> = userProfileDao.getOtherProfiles()
    val myProfile: Flow<UserProfile?> = userProfileDao.getMyProfileFlow()

    fun getProfileById(id: String): Flow<UserProfile?> = userProfileDao.getProfileById(id)

    fun searchProfiles(query: String): Flow<List<UserProfile>> = userProfileDao.searchProfiles(query)

    suspend fun getMyProfileDirect() = userProfileDao.getMyProfileDirect()

    suspend fun insertProfile(profile: UserProfile) = userProfileDao.insertProfile(profile)

    suspend fun updateProfile(profile: UserProfile) = userProfileDao.updateProfile(profile)

    suspend fun deleteProfile(profile: UserProfile) = userProfileDao.deleteProfile(profile)

    // Reviews
    fun getReviewsForProfile(profileId: String): Flow<List<UserReview>> = 
        userReviewDao.getReviewsForProfile(profileId)

    suspend fun insertReview(review: UserReview) {
        userReviewDao.insertReview(review)
        // Recalculate dynamic rating & review count for user profile
        val profile = userProfileDao.getProfileByIdDirect(review.targetProfileId)
        if (profile != null) {
            val allReviews = userReviewDao.getReviewsForProfile(review.targetProfileId).firstOrNull() ?: emptyList()
            val newReviewCount = allReviews.size
            val sumRating = allReviews.sumOf { it.rating.toDouble() }.toFloat()
            val avgRating = if (newReviewCount > 0) sumRating / newReviewCount else profile.averageRating
            userProfileDao.updateProfile(profile.copy(
                averageRating = avgRating,
                reviewCount = newReviewCount
            ))
        }
    }

    // Chat
    fun getChatMessages(profileId: String): Flow<List<ChatMessage>> = chatMessageDao.getChatMessages(profileId)
    
    val allMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessage) = chatMessageDao.insertMessage(message)

    suspend fun markChatAsRead(profileId: String) = chatMessageDao.markChatAsRead(profileId)

    // Notes
    val allNotes: Flow<List<UtilityNote>> = utilityNoteDao.getAllNotes()

    suspend fun insertNote(note: UtilityNote) = utilityNoteDao.insertNote(note)

    suspend fun deleteNote(note: UtilityNote) = utilityNoteDao.deleteNote(note)

    // Seeding database with amazing, highly accurate professionals
    suspend fun seedDatabaseIfEmpty() {
        // Checking if already seeded
        val existing = userProfileDao.getOtherProfiles().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val seedProfiles = listOf(
                // Delhi geographic grid (28.6139, 77.2090)
                UserProfile(
                    id = "prof_amit",
                    name = "Amit Sharma",
                    profession = "Electrician",
                    skillsRaw = "Wiring, Repair, Home Appliance Fixing, Inverter Installation",
                    mobileNumber = "+91 98765 43210",
                    emailAddress = "amit.sharma@jobaaya.com",
                    fullAddress = "A-12, Lajpat Nagar, New Delhi",
                    latitude = 28.5684,
                    longitude = 77.2435,
                    yearsOfExperience = 8,
                    languagesRaw = "Hindi, English",
                    aboutSection = "Professional residential electrician with 8 years of experience. Fully qualified for fixing high-voltage domestic layouts, smart lighting installations, appliance repairs, and emergency wiring solutions.",
                    availabilityStatus = WorkStatus.AVAILABLE.name,
                    isVerified = true,
                    averageRating = 4.8f,
                    reviewCount = 3,
                    profileViewsCount = 241,
                    interactionsCount = 42,
                    accountType = AccountType.PROFESSIONAL.name
                ),
                UserProfile(
                    id = "prof_priya",
                    name = "Priya Nair",
                    profession = "Nutritionist & Dietician",
                    skillsRaw = "Clinical Dietetics, Weight Management, Child Nutrition, Keto Coaching",
                    mobileNumber = "+91 87654 32109",
                    emailAddress = "priya.nair@jobaaya.com",
                    fullAddress = "Block C, Vasant Kunj, New Delhi",
                    latitude = 28.5244,
                    longitude = 77.1558,
                    yearsOfExperience = 6,
                    languagesRaw = "Hindi, English, Malayalam",
                    aboutSection = "Licensed clinical dietician helping professionals build sustainable eating habits. I guide clients with diabetic care plans, dynamic keto coaching, hormonal management diets, and organic meal preparation structures.",
                    availabilityStatus = WorkStatus.AVAILABLE.name,
                    isVerified = true,
                    averageRating = 4.9f,
                    reviewCount = 2,
                    profileViewsCount = 512,
                    interactionsCount = 89,
                    accountType = AccountType.BUSINESS.name
                ),
                UserProfile(
                    id = "prof_rajesh",
                    name = "Rajesh Yadav",
                    profession = "Car Mechanic",
                    skillsRaw = "Engine Tuning, Brake Repair, Car AC Servicing, Electrical Diagnosis",
                    mobileNumber = "+91 76543 21098",
                    emailAddress = "rajesh.yadav@jobaaya.com",
                    fullAddress = "Sector 14, Dwarka, New Delhi",
                    latitude = 28.5921,
                    longitude = 77.0460,
                    yearsOfExperience = 12,
                    languagesRaw = "Hindi",
                    aboutSection = "Complete expert in Japanese, German, and Indian automobile architectures. Working with specialized computerized engine scan layouts, wheel suspensions, automatic transmission repairs, and fluid leaks.",
                    availabilityStatus = WorkStatus.BUSY.name,
                    isVerified = false,
                    averageRating = 4.4f,
                    reviewCount = 1,
                    profileViewsCount = 120,
                    interactionsCount = 19,
                    accountType = AccountType.PROFESSIONAL.name
                ),
                UserProfile(
                    id = "prof_rahul",
                    name = "Rahul Gupta",
                    profession = "Plumber & Pipe Fitter",
                    skillsRaw = "Leak Detection, Water Pump Repair, Sanitary Fitting, Pipeline Clog Removal",
                    mobileNumber = "+91 65432 10987",
                    emailAddress = "rahul.gupta@jobaaya.com",
                    fullAddress = "H-456, Connaught Place, New Delhi",
                    latitude = 28.6304,
                    longitude = 77.2177,
                    yearsOfExperience = 5,
                    languagesRaw = "Hindi, English",
                    aboutSection = "Reliable plumbing solutions for apartments and commercial centers. Certified plumber skilled in deep diagnostic leak detection, pipeline replacements, new washroom fittings, and smart faucet setups.",
                    availabilityStatus = WorkStatus.AVAILABLE.name,
                    isVerified = true,
                    averageRating = 4.6f,
                    reviewCount = 2,
                    profileViewsCount = 198,
                    interactionsCount = 31,
                    accountType = AccountType.PROFESSIONAL.name
                ),
                UserProfile(
                    id = "prof_neha",
                    name = "Neha Malhotra",
                    profession = "Corporate Legal Consultant",
                    skillsRaw = "Company Registration, Contract Drafting, Trademark Filing, Joint Ventures",
                    mobileNumber = "+91 95432 18976",
                    emailAddress = "neha.consulting@jobaaya.com",
                    fullAddress = "Defense Colony, New Delhi",
                    latitude = 28.5742,
                    longitude = 77.2330,
                    yearsOfExperience = 9,
                    languagesRaw = "Hindi, English, Punjabi",
                    aboutSection = "Corporate advocate providing high-quality, professional consultancies for startups and foreign companies. Specialized in commercial contracting layouts, intellectual property, regulatory compliance, and advisory support.",
                    availabilityStatus = WorkStatus.AVAILABLE.name,
                    isVerified = true,
                    averageRating = 4.7f,
                    reviewCount = 1,
                    profileViewsCount = 430,
                    interactionsCount = 76,
                    accountType = AccountType.BUSINESS.name
                )
            )
            userProfileDao.insertProfiles(seedProfiles)

            // Seed reviews
            userReviewDao.insertReview(UserReview(targetProfileId = "prof_amit", reviewerName = "Suresh Khanna", rating = 5.0f, reviewText = "Excellent electrician. Arrived right on time and fixed my complicated distribution board issues in 30 minutes! Highly recommended."))
            userReviewDao.insertReview(UserReview(targetProfileId = "prof_amit", reviewerName = "Divya Sen", rating = 4.0f, reviewText = "Good service and pricing. Polished professional who cleaned up the wiring clutter completely."))
            userReviewDao.insertReview(UserReview(targetProfileId = "prof_amit", reviewerName = "Kunal Verma", rating = 5.0f, reviewText = "Quick and verified service. Very happy with his work experience."))

            userReviewDao.insertReview(UserReview(targetProfileId = "prof_priya", reviewerName = "Anjali Rawat", rating = 5.0f, reviewText = "Exceptional calorie consultation. Lost 8 kilograms and felt more energetic through her dynamic meal chart."))
            userReviewDao.insertReview(UserReview(targetProfileId = "prof_priya", reviewerName = "Sunil Thapa", rating = 4.8f, reviewText = "Extremely professional. Explains the health research behind every single customized client plan."))

            userReviewDao.insertReview(UserReview(targetProfileId = "prof_rajesh", reviewerName = "Mohit Juneja", rating = 4.4f, reviewText = "Honest mechanic who diagnosed the gear shift friction issues accurately. The pricing was fair and transparent."))

            userReviewDao.insertReview(UserReview(targetProfileId = "prof_rahul", reviewerName = "Rakesh Sethi", rating = 5.0f, reviewText = "Fixed the high pressure pump leak beautifully. Very affordable plumbing solution."))
            userReviewDao.insertReview(UserReview(targetProfileId = "prof_rahul", reviewerName = "Ritu Paul", rating = 4.2f, reviewText = "Experienced plumber, resolved the blockage in my kitchen basin instantly."))

            userReviewDao.insertReview(UserReview(targetProfileId = "prof_neha", reviewerName = "Vikram Kapoor", rating = 4.7f, reviewText = "Outstanding legal consulting. Drafted our investor SaaS contract perfectly within 48 hours."))
            
            // Seed a starter system message chat history
            chatMessageDao.insertMessage(ChatMessage(chatWithProfileId = "prof_amit", isFromMe = false, text = "Hello! I am Amit Sharma. Thank you for viewing my profile. Let me know if you need any electrician services today!"))
        }

        // Check if there is a 'Me' profile
        val me = userProfileDao.getMyProfileDirect()
        if (me == null) {
            val myStarterProfile = UserProfile(
                id = "me_user",
                name = "Guest User",
                profession = "Software Developer",
                skillsRaw = "Android, Kotlin, Jetpack Compose, Mobile Architect",
                mobileNumber = "+91 99999 88888",
                emailAddress = "guest@jobaaya.com",
                fullAddress = "Sector 22, Rohini, New Delhi",
                latitude = 28.7159,
                longitude = 77.1006,
                yearsOfExperience = 3,
                languagesRaw = "English, Hindi",
                aboutSection = "Passionate mobile engineer skilled in declarative Compose UI, local offline-first SQLite synchronization engines, and scalable app architectures.",
                isMe = true,
                accountType = AccountType.PROFESSIONAL.name,
                availabilityStatus = WorkStatus.AVAILABLE.name,
                profileViewsCount = 15,
                interactionsCount = 2,
                isVerified = false
            )
            userProfileDao.insertProfile(myStarterProfile)
        }
    }
}
