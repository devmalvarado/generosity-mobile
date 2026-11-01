package org.generosity.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Browser authorization; only Generosity's short-lived session token reaches this app. */
class AccountSession(private val context: Context, private val scope: CoroutineScope, private val appState: GenerosityAppState) {
    var busy by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set
    private val storage = AtomicFile(File(context.noBackupFilesDir, "generosity-account"))
    private val base = BuildConfig.API_BASE_URL.trimEnd('/')
    private val alias = "generosity.account.v1"

    fun signIn() {
        if (busy) return
        failed = false
        runCatching {
            val pending = JSONObject().put("verifier", random()).put("state", random()).put("created", System.currentTimeMillis())
            val saved = read().put("pending", pending)
            write(saved)
            val uri = Uri.parse(base + "/auth/sign-in").buildUpon()
                .appendQueryParameter("client", "android")
                .appendQueryParameter("locale", appState.state.value.locale.code)
                .appendQueryParameter("state", pending.getString("state"))
                .appendQueryParameter("code_challenge", challenge(pending.getString("verifier")))
                .build()
            check(uri.scheme == "https")
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }.onFailure { failed = true }
    }

    fun callback(uri: Uri?): Boolean {
        if (uri?.scheme != "org.generosity.app.android" || uri.path != "/oauth/callback" || uri.host != null) return false
        if (busy) return true
        runAction {
            val saved = read()
            val pending = saved.optJSONObject("pending") ?: error("No pending sign-in")
            check(System.currentTimeMillis() - pending.getLong("created") in 0..600_000)
            check(uri.getQueryParameters("state").size == 1 && uri.getQueryParameters("code").size == 1)
            val returnedState = uri.getQueryParameter("state") ?: error("Missing state")
            check(MessageDigest.isEqual(returnedState.toByteArray(), pending.getString("state").toByteArray()))
            val code = uri.getQueryParameter("code") ?: error("Missing code")
            check(code.matches(Regex("[a-zA-Z0-9]{64}")))
            saved.remove("pending")
            write(saved)
            val result = request("/api/v1/auth/exchange", JSONObject().put("client", "android").put("code", code).put("code_verifier", pending.getString("verifier")))
            write(JSONObject().put("token", result.getString("token")))
            identify(result.getJSONObject("user"))
        }
        return true
    }

    fun restore() {
        if (busy) return
        runAction {
            val token = read().optString("token")
            if (token.isEmpty()) return@runAction
            try {
                identify(request("/api/v1/me", token = token).getJSONObject("user"))
            } catch (error: Exception) {
                appState.continueAnonymously()
                throw error
            }
        }
    }

    fun signOut() {
        if (busy) return
        runAction {
            val token = read().optString("token")
            if (token.isNotEmpty()) request("/api/v1/auth/logout", JSONObject(), token)
            storage.delete()
            appState.continueAnonymously()
        }
    }

    private fun identify(user: JSONObject) {
        appState.signIn(user.getLong("id").toString(), user.getString("name"), remoteAccount = true)
    }

    private fun runAction(action: suspend () -> Unit) {
        busy = true
        failed = false
        scope.launch {
            try { action() } catch (error: Exception) { failed = true } finally { busy = false }
        }
    }

    private suspend fun request(path: String, body: JSONObject? = null, token: String? = null): JSONObject = withContext(Dispatchers.IO) {
        val url = URL(base + path)
        check(url.protocol == "https")
        val connection = url.openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 15_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            if (connection.responseCode == 401 && token != null) {
                storage.delete()
                appState.continueAnonymously()
                if (path.endsWith("/logout")) return@withContext JSONObject()
            }
            check(connection.responseCode in 200..299)
            if (connection.responseCode == 204) JSONObject() else connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
        } finally { connection.disconnect() }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return store.getKey(alias, null) as? SecretKey ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    private fun read(): JSONObject {
        if (!storage.baseFile.exists()) return JSONObject()
        return try {
            val bytes = storage.readFully()
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            JSONObject(String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8))
        } catch (error: Exception) {
            storage.delete()
            JSONObject()
        }
    }

    private fun write(value: JSONObject) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val output = storage.startWrite()
        try {
            output.write(cipher.iv + cipher.doFinal(value.toString().toByteArray()))
            storage.finishWrite(output)
        } catch (error: Exception) {
            storage.failWrite(output)
            throw error
        }
    }

    private fun random() = encode(ByteArray(32).also { SecureRandom().nextBytes(it) })
    companion object {
        internal fun challenge(verifier: String) = encode(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()))
        private fun encode(bytes: ByteArray) = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
