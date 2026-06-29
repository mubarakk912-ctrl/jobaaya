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

    suspend fun sendOtp(mobileNumber: String): Result<String> = withContext(Dispatchers.IO) {
        // Validation
        if (mobileNumber.isBlank() || mobileNumber.length < 10) {
            return@withContext Result.failure(IllegalArgumentException("Invalid mobile number"))
        }

        // NOTE: Firebase Phone Auth requires Activity context and Callbacks
        // In a real production environment, you would handle the verificationId
        // via a callback sent from the UI/ViewModel.
        // For now, we remain in Simulation mode for the repository structure
        // while the dependencies are fully linked.
        
        delay(1000) // Simulate network latency
        Result.success("verification_id_" + UUID.randomUUID().toString())
    }

    suspend fun verifyFirebaseOtp(verificationId: String, otp: String, mobileNumber: String): Result<AuthResult> = withContext(Dispatchers.IO) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val user = authResult.user
            
            if (user != null) {
                val token = UUID.randomUUID().toString() // In real prod, use user.getIdToken()
                val userId = user.uid
                
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

    suspend fun loginWithEmail(email: String, password: CharSequence): Result<AuthResult> = withContext(Dispatchers.IO) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return@withContext Result.failure(IllegalArgumentException("Invalid email format"))
        }
        if (password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Password too short"))
        }

        delay(1000)

        val token = "jwt_token_" + UUID.randomUUID().toString()
        val userId = "user_" + UUID.randomUUID().toString()
        
        sessionManager.saveAuthToken(token)
        sessionManager.saveUserData(userId, email, null)

        Result.success(AuthResult(userId, token, isNewUser = false))
    }

    suspend fun signInWithGoogle(googleToken: String): Result<AuthResult> = withContext(Dispatchers.IO) {
        if (googleToken.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Invalid Google Token"))
        }

        delay(1200)

        val token = "jwt_token_" + UUID.randomUUID().toString()
        val userId = "user_" + UUID.randomUUID().toString()
        
        sessionManager.saveAuthToken(token)
        sessionManager.saveUserData(userId, "google_user@jobaaya.com", null)

        Result.success(AuthResult(userId, token, isNewUser = false))
    }

    fun logout() {
        sessionManager.clearSession()
    }
}

data class AuthResult(
    val userId: String,
    val token: String,
    val isNewUser: Boolean
)
