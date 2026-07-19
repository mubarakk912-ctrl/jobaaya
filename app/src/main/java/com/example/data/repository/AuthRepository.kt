package com.example.data.repository

import com.example.data.auth.SessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthRepository(private val sessionManager: SessionManager) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    suspend fun verifyFirebaseOtp(verificationId: String, otp: String, mobileNumber: String): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            
            if (user != null) {
                val userId = user.uid
                val token = userId // Using UID as token for session management consistency
                
                sessionManager.saveAuthToken(token)
                sessionManager.saveUserData(userId, user.email, mobileNumber)
                
                Result.success(AuthResult(userId, token, isNewUser = authResult.additionalUserInfo?.isNewUser ?: false))
            } else {
                Result.failure(Exception("Firebase User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        firebaseAuth.signOut()
        sessionManager.clearSession()
    }
}

data class AuthResult(
    val userId: String,
    val token: String,
    val isNewUser: Boolean
)
