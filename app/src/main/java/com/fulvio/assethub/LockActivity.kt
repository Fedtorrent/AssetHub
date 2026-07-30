package com.fulvio.assethub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.fulvio.assethub.databinding.ActivityLockBinding

class LockActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockBinding
    private var mode: String? = null
    private var firstPin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Applicazione Protezione Background (FLAG_SECURE)
        val securityPrefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val backgroundProtection = securityPrefs.getBoolean("background_protection", false)
        if (backgroundProtection) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }

        binding = ActivityLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Impostiamo l\u0027oscuramento con asterischi invece dei pallini standard
        binding.editPin.transformationMethod = object : android.text.method.PasswordTransformationMethod() {
            override fun getTransformation(source: CharSequence, view: View): CharSequence {
                return AsteriskPasswordCharSequence(source)
            }

            inner class AsteriskPasswordCharSequence(private val source: CharSequence) : CharSequence {
                override val length: Int get() = source.length
                override fun get(index: Int): Char = '*'
                override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                    return source.subSequence(startIndex, endIndex)
                }
            }
        }

        mode = intent.getStringExtra("MODE") // "SET", "VERIFY", "UNLOCK", "CHANGE"
        
        updateTitle()

        binding.btnConfirmPin.setOnClickListener {
            handleConfirm()
        }

        val autoBiometric = intent.getBooleanExtra("AUTO_BIOMETRIC", false)
        if (autoBiometric && mode == "UNLOCK") {
            showBiometricPrompt()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mode == "UNLOCK") {
                    finishAffinity()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun updateTitle() {
        when (mode) {
            "SET" -> {
                binding.textLockTitle.text = if (firstPin == null) "Imposta nuovo PIN (4 cifre)" else "Conferma nuovo PIN"
            }
            "VERIFY" -> binding.textLockTitle.text = "Conferma PIN attuale"
            "UNLOCK" -> binding.textLockTitle.text = "Inserisci PIN di sblocco"
            "CHANGE" -> binding.textLockTitle.text = "Inserisci vecchio PIN"
        }
    }

    private fun handleConfirm() {
        val pin = binding.editPin.text.toString()
        if (pin.length < 4) {
            Toast.makeText(this, "Il PIN deve essere di 4 cifre", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", null)

        when (mode) {
            "SET" -> {
                if (firstPin == null) {
                    firstPin = pin
                    binding.editPin.text?.clear()
                    updateTitle()
                } else {
                    if (pin == firstPin) {
                        prefs.edit().putString("user_pin", pin).apply()
                        Toast.makeText(this, "PIN salvato con successo!", Toast.LENGTH_SHORT).show()
                        finishWithResult(true)
                    } else {
                        Toast.makeText(this, "I PIN non coincidono! Riprova.", Toast.LENGTH_SHORT).show()
                        firstPin = null
                        binding.editPin.text?.clear()
                        updateTitle()
                    }
                }
            }
            "VERIFY", "UNLOCK" -> {
                if (pin == savedPin) {
                    finishWithResult(true)
                } else {
                    Toast.makeText(this, "PIN errato!", Toast.LENGTH_SHORT).show()
                    binding.editPin.text?.clear()
                }
            }
            "CHANGE" -> {
                if (pin == savedPin) {
                    mode = "SET"
                    binding.editPin.text?.clear()
                    updateTitle()
                } else {
                    Toast.makeText(this, "PIN attuale errato!", Toast.LENGTH_SHORT).show()
                    binding.editPin.text?.clear()
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    finishWithResult(true)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LockActivity, "Autenticazione fallita", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sblocco Applicazione")
            .setSubtitle("Usa la biometria per accedere")
            .setNegativeButtonText("Usa PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun finishWithResult(success: Boolean) {
        if (success && mode == "UNLOCK") {
            startActivity(Intent(this, MainActivity::class.java).apply {
                putExtra("IS_UNLOCKED", true)
            })
            finish()
        } else if (success) {
            setResult(RESULT_OK)
            finish()
        } else {
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
