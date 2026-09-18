package com.gyoheul.calm_routine.data

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.firestore
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.gyoheul.calm_routine.BuildConfig
import com.gyoheul.calm_routine.model.BackupDocument
import com.gyoheul.calm_routine.model.UserStoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.coroutines.resume

object FirebaseModel {
    private lateinit var _signInLauncher: ActivityResultLauncher<Intent>
    private val _signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(
            arrayListOf(
                AuthUI.IdpConfig.GoogleBuilder().build(),
            )
        )
        .build()

    private var _signInCallback: ((Int, FirebaseUser?) -> Unit)? = null

    var firebaseConfigActivated by mutableStateOf(false)
        private set

    val forceUpdate: Boolean
        get() = Firebase.remoteConfig.getBoolean("force_update") && isVersionOlder(BuildConfig.VERSION_NAME, minVersion)
    private val minVersion: String
        get() = Firebase.remoteConfig.getString("min_version_name")

    init {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                "force_update" to false,
                "min_version_name" to "1.0.0",
            )
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseConfigActivated = true
            }
        }
    }

    fun setSignInLauncher(activity: ComponentActivity) {
        _signInLauncher = activity.registerForActivityResult(
            FirebaseAuthUIActivityResultContract(),
        ) { res ->
            CoroutineScope(Dispatchers.IO).launch {
                onSignInResult(res)
            }
        }
    }

    fun signInLauncher(callback: ((Int, FirebaseUser?) -> Unit)?) {
        _signInCallback = callback
        _signInLauncher.launch(_signInIntent)
    }

    private suspend fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        _signInCallback?.invoke(result.resultCode, getUser())
    }

    suspend fun getUser(): FirebaseUser? =
        if (NetworkModel.hasInternetAccess()) {
            FirebaseAuth.getInstance().currentUser
        } else {
            null
        }

    suspend fun signOut(context: Context): Boolean {
        return suspendCancellableCoroutine { cont ->
            AuthUI.getInstance()
                .signOut(context)
                .addOnCompleteListener {
                    cont.resume(true)
                }
                .addOnFailureListener {
                    cont.resume(false)
                }
        }
    }

    suspend fun backupUserData(userStoreData: UserStoreData): Boolean {
        try {
            val userId = getUser()?.uid ?: return false
            val expireAt = Timestamp(Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7日後
            val backupDoc = BackupDocument(data = userStoreData, expireAt = expireAt)
            Firebase.firestore.collection("backups")
                .document(userId)
                .set(backupDoc)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    suspend fun restoreUserData(): UserStoreData? {
        try {
            val userId = getUser()?.uid ?: return null
            val snapshot = Firebase.firestore.collection("backups")
                .document(userId)
                .get()
                .await()

            val backup = snapshot.toObject(BackupDocument::class.java)
            return backup?.data
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun isVersionOlder(currentVersion: String, minVersion: String): Boolean {
        val currentParts = currentVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val minParts = minVersion.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(currentParts.size, minParts.size)
        for (i in 0 until maxLength) {
            val current = currentParts.getOrElse(i) { 0 }
            val min = minParts.getOrElse(i) { 0 }

            if (current < min) return true
            if (current > min) return false
        }
        return true // 同一の場合
    }
}