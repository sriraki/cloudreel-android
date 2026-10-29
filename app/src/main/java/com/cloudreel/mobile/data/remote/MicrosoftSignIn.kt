package com.cloudreel.mobile.data.remote

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import kotlin.coroutines.resume

class MicrosoftSignInException(message: String) : Exception(message)

data class MicrosoftIdentity(val idToken: String, val email: String)

/**
 * Microsoft (Entra ID) sign-in with the authorization code + PKCE flow.
 * The tenant and client id are the same ones the web app uses (VITE_OIDC_* in its env files).
 *
 * The redirect uri below must be added to that app registration in Azure
 * (platform "Mobile and desktop applications"), otherwise Microsoft rejects the sign-in.
 */
class MicrosoftSignIn(context: Context) {

    private val authorizationService = AuthorizationService(context)

    fun createSignInIntent(): Intent {
        val configuration = AuthorizationServiceConfiguration(
            Uri.parse("$AUTHORITY/oauth2/v2.0/authorize"),
            Uri.parse("$AUTHORITY/oauth2/v2.0/token")
        )
        val request = AuthorizationRequest.Builder(
            configuration,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScope("openid profile email")
            .setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
            .build()
        return authorizationService.getAuthorizationRequestIntent(request)
    }

    /** Exchanges the sign-in result for an id token and reads the account email from it. */
    suspend fun completeSignIn(result: Intent?): MicrosoftIdentity {
        val data = result ?: throw MicrosoftSignInException("Sign-in was cancelled")
        val response = AuthorizationResponse.fromIntent(data)
        val error = AuthorizationException.fromIntent(data)
        if (response == null) {
            throw MicrosoftSignInException(
                if (error?.code == AuthorizationException.GeneralErrors.USER_CANCELED_AUTH_FLOW.code) {
                    "Sign-in was cancelled"
                } else {
                    error?.errorDescription ?: "Microsoft sign-in failed"
                }
            )
        }

        val (idToken, tokenError) = suspendCancellableCoroutine<Pair<String?, String?>> { continuation ->
            authorizationService.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, exception ->
                continuation.resume(tokenResponse?.idToken to exception?.errorDescription)
            }
        }
        if (idToken == null) {
            throw MicrosoftSignInException(tokenError ?: "Microsoft did not return a sign-in token")
        }

        val email = readEmail(idToken)
            ?: throw MicrosoftSignInException("Your Microsoft account has no email address")
        return MicrosoftIdentity(idToken, email)
    }

    fun dispose() = authorizationService.dispose()

    /** The backend validates the token itself, so the app only reads the claims it needs. */
    private fun readEmail(idToken: String): String? {
        val payload = idToken.split('.').getOrNull(1) ?: return null
        val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
        val claims: JsonObject = JsonParser.parseString(json).asJsonObject
        return (claims.get("email") ?: claims.get("preferred_username"))
            ?.takeIf { !it.isJsonNull }?.asString
    }

    private companion object {
        const val AUTHORITY = "https://login.microsoftonline.com/d46b599e-0168-43ba-97d1-675a2fec3c1f"
        const val CLIENT_ID = "5311c9f3-ce80-4877-a148-9ca8f0a3af9d"
        const val REDIRECT_URI = "com.cloudreel.mobile://oauth2redirect"
    }
}
