package co.alcheclub.ai.trading.assistant.data.remote.service

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/**
 * Uploads chart images to R2 via Supabase Edge Function.
 */
class ImageUploadService(
    private val client: HttpClient,
    private val supabaseClient: SupabaseClient,
    private val supabaseUrl: String,
    private val supabaseAnonKey: String
) {
    companion object {
        private const val TAG = "ImageUpload"
    }

    suspend fun uploadImage(imageData: ByteArray): Result<String> {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
                ?: return Result.failure(Exception("No active session"))

            val filename = "chart-${System.currentTimeMillis() / 1000}.jpg"
            val url = "$supabaseUrl/functions/v1/upload-chart-image"

            val response = client.post(url) {
                contentType(ContentType.Image.JPEG)
                header("x-filename", filename)
                header("Authorization", "Bearer $supabaseAnonKey")
                header("apikey", supabaseAnonKey)
                header("x-user-token", "Bearer ${session.accessToken}")
                setBody(imageData)
            }

            val statusCode = response.status.value
            if (statusCode !in 200..299) {
                val body = try { response.body<String>() } catch (_: Exception) { "no body" }
                Log.e(TAG, "Chart upload HTTP $statusCode: $body")
                return Result.failure(Exception("Upload failed: HTTP $statusCode: $body"))
            }

            val uploadResponse = response.body<UploadResponseDTO>()
            Result.success(uploadResponse.url)
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
            Result.failure(e)
        }
    }
}

@Serializable
private data class UploadResponseDTO(val url: String)
