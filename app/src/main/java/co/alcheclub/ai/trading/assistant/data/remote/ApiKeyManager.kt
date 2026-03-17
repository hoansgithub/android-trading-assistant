package co.alcheclub.ai.trading.assistant.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe round-robin API key rotator with failure tracking.
 * Marks keys as failed on 429/401 errors, skips failed keys on next rotation.
 */
class ApiKeyManager(private val keys: List<String>) {

    private val mutex = Mutex()
    private var currentIndex = 0
    private val failedKeys = mutableSetOf<Int>()

    val availableKeyCount: Int
        get() = keys.size - failedKeys.size

    suspend fun nextKey(): String? = mutex.withLock {
        if (keys.isEmpty()) return@withLock null

        val startIndex = currentIndex
        var attempts = 0

        while (attempts < keys.size) {
            val index = (startIndex + attempts) % keys.size
            if (index !in failedKeys) {
                currentIndex = (index + 1) % keys.size
                return@withLock keys[index]
            }
            attempts++
        }

        null // All keys exhausted
    }

    suspend fun markKeyFailed(key: String) = mutex.withLock {
        val index = keys.indexOf(key)
        if (index >= 0) {
            failedKeys.add(index)
            android.util.Log.w("ApiKeyManager", "Key ${index + 1}/${keys.size} marked failed")
        }
    }

    suspend fun resetFailures() = mutex.withLock {
        failedKeys.clear()
    }
}

/**
 * Paired key rotator for APIs that require keyID + secretKey (e.g., Alpaca).
 */
class KeyPairManager(keyIds: List<String>, secretKeys: List<String>) {

    data class KeyPair(val keyId: String, val secretKey: String)

    private val pairs = keyIds.zip(secretKeys).map { (id, secret) -> KeyPair(id, secret) }
    private val mutex = Mutex()
    private var currentIndex = 0
    private val failedPairs = mutableSetOf<Int>()

    suspend fun nextKeyPair(): KeyPair? = mutex.withLock {
        if (pairs.isEmpty()) return@withLock null

        val startIndex = currentIndex
        var attempts = 0

        while (attempts < pairs.size) {
            val index = (startIndex + attempts) % pairs.size
            if (index !in failedPairs) {
                currentIndex = (index + 1) % pairs.size
                return@withLock pairs[index]
            }
            attempts++
        }

        null
    }

    suspend fun markKeyPairFailed(keyId: String) = mutex.withLock {
        val index = pairs.indexOfFirst { it.keyId == keyId }
        if (index >= 0) failedPairs.add(index)
    }

    suspend fun resetFailures() = mutex.withLock {
        failedPairs.clear()
    }
}
