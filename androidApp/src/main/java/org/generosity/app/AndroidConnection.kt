package org.generosity.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.MessageDigest
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.*
import org.generosity.data.*

class AndroidConnection(context: Context, private val scope: CoroutineScope) : AppConnection {
    var credential: (String) -> String? = { null }
    private val base = BuildConfig.API_BASE_URL
    private val folder = File(context.noBackupFilesDir, "backend-" + digest(base)).apply { mkdirs() }
    private val alias = "generosity.backend.v1"

    override fun send(request: ApiRequest, completion: (ApiResponse) -> Unit) {
        val token = request.accountId?.let(credential)
        if (request.accountId != null && token == null) { completion(ApiResponse(401, "{}")); return }
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                var connection: HttpURLConnection? = null
                try {
                    val url = URL(base + request.path)
                    check(url.protocol == "https" || (BuildConfig.DEBUG && url.protocol == "http" && url.host in listOf("10.0.2.2", "127.0.0.1", "localhost")))
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = request.method; instanceFollowRedirects = false
                        connectTimeout = 15_000; readTimeout = 15_000
                        setRequestProperty("Accept", "application/json")
                        token?.let { setRequestProperty("Authorization", "Bearer $it") }
                        request.operationId?.let { setRequestProperty("Idempotency-Key", it) }
                        request.body?.let { body ->
                            doOutput = true; setRequestProperty("Content-Type", "application/json")
                            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                        }
                    }
                    val status = connection.responseCode
                    val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                    val body = stream?.bufferedReader()?.use { reader ->
                        val buffer = CharArray(8192)
                        val result = StringBuilder()
                        var count = reader.read(buffer)
                        while (count >= 0) {
                            check(result.length + count <= 16 * 1024 * 1024)
                            result.append(buffer, 0, count)
                            count = reader.read(buffer)
                        }
                        result.toString()
                    }.orEmpty()
                    ApiResponse(status, body)
                } catch (error: Exception) { ApiResponse(0, "") }
                finally { connection?.disconnect() }
            }
            completion(result)
        }
    }

    private fun file(key: String) = AtomicFile(File(folder, digest(key)))
    override fun read(key: String): String {
        val source = file(key)
        if (!source.baseFile.exists()) return ""
        val bytes = source.readFully()
        require(bytes.size >= 28)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
    }

    override fun write(key: String, value: String?) {
        val target = file(key)
        if (value == null) { target.delete(); return }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey())
        val bytes = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val output = target.startWrite()
        try { output.write(bytes); target.finishWrite(output) }
        catch (error: Exception) { target.failWrite(output); throw error }
    }

    private fun encryptionKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey(alias, null) as? SecretKey ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    override fun newOperationId() = UUID.randomUUID().toString()
    private fun digest(text: String) = MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
