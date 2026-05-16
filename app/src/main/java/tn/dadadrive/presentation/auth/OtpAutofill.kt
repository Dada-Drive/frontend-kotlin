package tn.dadadrive.presentation.auth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

private const val TAG = "OtpAutofill"

private val sixDigitRegex = Regex("\\d{6}")

/** Extrait un code OTP à 6 chiffres depuis un texte (SMS). */
internal fun extractSixDigitOtp(text: String): String? {
    val matches = sixDigitRegex.findAll(text).map { it.value }.toList()
    if (matches.isEmpty()) return null
    // En général le code est le dernier groupe de 6 chiffres dans le message
    return matches.last()
}

/**
 * Auto-remplissage OTP.
 *
 * - **SMS** : SMS Retriever API de Google Play Services (sans permission READ_SMS),
 *   ciblé par le hash de l'app → ne remplit que si le message vient réellement
 *   d'être reçu pour cette app.
 * - **WhatsApp** : **pas d'auto-fill**. On ne touche jamais au presse-papiers :
 *   c'était la source du "code pré-rempli" non souhaité (un vieux code OTP déjà
 *   copié, ou n'importe quel code 6 chiffres collé dans le clipboard de l'appareil
 *   se retrouvait injecté à l'arrivée sur l'écran). L'utilisateur peut toujours
 *   coller manuellement via long-press sur le champ — c'est le comportement standard.
 *
 * [sessionKey] remet à zéro la mémoire "code déjà rempli" quand un nouveau flow
 * démarre (nouveau numéro, renvoi), pour qu'un nouveau SMS reçu puisse bien
 * déclencher un remplissage propre.
 */
@Composable
fun OtpAutofillEffects(
    enabled: Boolean,
    sessionKey: Any? = null,
    onOtpFilled: (String) -> Unit,
) {
    val context = LocalContext.current
    val onOtpUpdated by rememberUpdatedState(onOtpFilled)

    // Survit aux ré-abonnements du DisposableEffect, remis à zéro par sessionKey.
    val lastAppliedCode = remember(sessionKey) { mutableStateOf<String?>(null) }

    DisposableEffect(enabled, context) {
        if (!enabled) {
            return@DisposableEffect onDispose { }
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action != SmsRetriever.SMS_RETRIEVED_ACTION) return
                val extras = intent.extras ?: return
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status ?: return
                when (status.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                        extractSixDigitOtp(message)?.let { code ->
                            if (code == lastAppliedCode.value) return@let
                            lastAppliedCode.value = code
                            Log.d(TAG, "OTP reçu via SMS Retriever")
                            onOtpUpdated(code)
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> Log.d(TAG, "SMS Retriever timeout")
                    else -> Log.d(TAG, "SMS Retriever status=${status.statusCode}")
                }
            }
        }

        val filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        try {
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                SmsRetriever.SEND_PERMISSION,
                null,
                ContextCompat.RECEIVER_EXPORTED,
            )
        } catch (e: Exception) {
            Log.w(TAG, "registerReceiver SMS", e)
        }

        val task = SmsRetriever.getClient(context).startSmsRetriever()
        task.addOnFailureListener { e -> Log.w(TAG, "startSmsRetriever", e) }

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }
}
