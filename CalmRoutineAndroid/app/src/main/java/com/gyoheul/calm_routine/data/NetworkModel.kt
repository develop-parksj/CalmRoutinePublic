package com.gyoheul.calm_routine.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object NetworkModel {
    suspend fun hasInternetAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://clients3.google.com/generate_204")
            val connection = url.openConnection() as HttpURLConnection
            connection.apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
                doInput = true
                connect()
            }
            connection.responseCode == 204
        } catch (_: Exception) {
            false
        }
    }
}