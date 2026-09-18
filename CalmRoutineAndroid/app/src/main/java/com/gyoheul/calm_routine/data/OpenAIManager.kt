package com.gyoheul.calm_routine.data

import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

class OpenAIManager {
    interface OpenAIService {
        @POST("chat")
        suspend fun getChat(@Body request: ChatRequest): ChatResultResponse
    }

    data class ChatRequest(
        val model: String =
            if (PremiumStatusRepository.isPremium.value) {
                "gpt-5.6-luna"
            } else {
                "gpt-5-nano"
            },
        val messages: List<Message>
    )

    data class Message(
        val role: String,
        val content: String
    )

    data class ChatResultResponse(
        val success: Boolean,
        val message: String?,
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://your-api-endpoint.execute-api.region.amazonaws.com/prod/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(OpenAIService::class.java)

    suspend fun sendMessageToChatGPT(messages: List<Message>): Message? {
        return try {
            val request = ChatRequest(
                messages = messages
            )

            val response = apiService.getChat(request)

            if (!response.success) {
                LogModel.e(this, "submitChatJob failed: $response")
                return null
            }

            val resultText = response.message

            if (resultText.isNullOrBlank()) {
                return null
            }

            Message(
                role = "assistant",
                content = resultText
            )
        } catch (e: Exception) {
            e.printStackTrace()

            if (e is HttpException) {
                LogModel.e(
                    this,
                    "sendMessageToChatGPT Error ${e.response()?.errorBody()?.string()}",
                    e
                )
                LogModel.e(this, "messages : $messages")
            } else {
                LogModel.e(this, "sendMessageToChatGPT Error ${e.message}", e)
            }

            null
        }
    }
}