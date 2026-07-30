package com.fulvio.assethub

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class AssetHubApp : Application() {

    companion object {
        // Flag temporaneo per ignorare il blocco privacy al rientro (es. dopo selettore file per backup)
        var ignoreNextForegroundBlock = false
    }

    override fun onCreate() {
        super.onCreate()
        
        // Forza la modalità scura per tutta l'app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        
        // Osserva il ciclo di vita dell'intera applicazione
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                // Se il flag è attivo, lo consumiamo e saltiamo il blocco
                if (ignoreNextForegroundBlock) {
                    ignoreNextForegroundBlock = false
                    return
                }

                // L\u0027app sta tornando in primo piano
                val securityPrefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
                val pinEnabled = securityPrefs.getBoolean("pin_enabled", false)
                val biometryEnabled = securityPrefs.getBoolean("biometry_enabled", false)
                val backgroundProtection = securityPrefs.getBoolean("background_protection", false)

                if (backgroundProtection && (pinEnabled || biometryEnabled)) {
                    // Avviamo la schermata di sblocco
                    val intent = Intent(this@AssetHubApp, LockActivity::class.java).apply {
                        putExtra("MODE", "UNLOCK")
                        if (biometryEnabled) putExtra("AUTO_BIOMETRIC", true)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    startActivity(intent)
                }
            }
        })
    }
}
