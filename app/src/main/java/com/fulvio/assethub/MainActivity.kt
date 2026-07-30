package com.fulvio.assethub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.fulvio.assethub.UsefulLink

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Controllo Primo Avvio
        val appPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isFirstRun = appPrefs.getBoolean("first_run", true)
        val isUnlocked = intent.getBooleanExtra("IS_UNLOCKED", false)

        if (isFirstRun && !isUnlocked) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }

        // Controllo Sicurezza
        val securityPrefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val pinEnabled = securityPrefs.getBoolean("pin_enabled", false)
        val biometryEnabled = securityPrefs.getBoolean("biometry_enabled", false)

        if ((pinEnabled || biometryEnabled) && !isUnlocked) {
            // Mostriamo sempre la LockActivity per nascondere il Cruscotto
            startActivity(Intent(this, LockActivity::class.java).apply {
                putExtra("MODE", "UNLOCK")
                if (biometryEnabled) putExtra("AUTO_BIOMETRIC", true)
            })
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Applicazione Protezione Background (FLAG_SECURE)
        val backgroundProtection = securityPrefs.getBoolean("background_protection", false)
        if (backgroundProtection) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Assicura che le notifiche siano pianificate se abilitate
        if (appPrefs.getBoolean("notify_cedole", false) || 
            appPrefs.getBoolean("notify_scadenze", false)) {
            NotificationHelper(this).scheduleDailyCheck()
        }

        // Routine di Sanificazione ID e Etichette (Eseguita una sola volta alla v16)
        if (!appPrefs.getBoolean("sanified_v16_labels", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(this@MainActivity).vincoloDao()
                    
                    // 1. Sanificazione Etichette "/ Libero"
                    dao.updateTipoMassa("Conto Corrente / Libero", "Conto Corrente")
                    dao.updateCategoryNameBySystemType(Category.TYPE_CORRENTE, "Conto Corrente")
                    
                    val allVincoli = dao.getAllVincoli().first()
                    
                    // 2. Standalone (BTP, Depositi) con lo stesso ID
                    val standaloneVincoli = allVincoli.filter { !InstrumentUtils.isHistoryBased(it) }
                    val groupedByCode = standaloneVincoli.groupBy { it.codiceVincolo }
                    var currentMax = dao.getMaxCodiceVincolo() ?: 0
                    
                    for (group in groupedByCode.values) {
                        if (group.size > 1) {
                            for (i in 1 until group.size) {
                                currentMax++
                                dao.updateCodiceVincoloById(group[i].id, currentMax)
                            }
                        }
                    }
                    
                    // 3. PAC/ETF con ID 0
                    val historyVincoli00 = allVincoli.filter { InstrumentUtils.isHistoryBased(it) && it.codiceVincolo == 0 }
                    val groupedByAsset = historyVincoli00.groupBy { "${it.accountId}_${it.nome}" }
                    for (assetGroup in groupedByAsset.values) {
                        currentMax++
                        for (v in assetGroup) {
                            dao.updateCodiceVincoloById(v.id, currentMax)
                        }
                    }
                    appPrefs.edit().putBoolean("sanified_v16_labels", true).apply()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        // Routine di inserimento categoria "Conto Deposito Libero" (Eseguita una sola volta alla v17)
        if (!appPrefs.getBoolean("seeded_deposito_libero_v17", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(this@MainActivity).vincoloDao()
                    val existing = dao.getCategoryByType(Category.TYPE_DEPOSITO_LIBERO)
                    if (existing == null) {
                        dao.insertCategory(Category(
                            name = "Conto Deposito Libero",
                            systemType = Category.TYPE_DEPOSITO_LIBERO,
                            color = android.graphics.Color.parseColor("#00BCD4")
                        ))
                    }
                    appPrefs.edit().putBoolean("seeded_deposito_libero_v17", true).apply()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        // Routine di inserimento nuove categorie asset fisici (Eseguita una sola volta alla v19)
        if (!appPrefs.getBoolean("seeded_physical_assets_v19", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(this@MainActivity).vincoloDao()
                    val categoriesToAdd = listOf(
                        Category(name = "Contanti", systemType = Category.TYPE_CONTANTI, color = android.graphics.Color.parseColor("#9E9E9E")),
                        Category(name = "Veicoli", systemType = Category.TYPE_VEICOLI, color = android.graphics.Color.parseColor("#212121")),
                        Category(name = "Gioielli", systemType = Category.TYPE_GIOIELLI, color = android.graphics.Color.parseColor("#FFD700")),
                        Category(name = "Oggetti di valore", systemType = Category.TYPE_OGGETTI, color = android.graphics.Color.parseColor("#795548"))
                    )
                    
                    for (cat in categoriesToAdd) {
                        if (dao.getCategoryByType(cat.systemType) == null) {
                            dao.insertCategory(cat)
                        }
                    }
                    appPrefs.edit().putBoolean("seeded_physical_assets_v19", true).apply()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        // Routine di Garbage Collection Vincoli Orfani (Eseguita una sola volta alla v18)
        if (!appPrefs.getBoolean("orphan_cleanup_v18", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(this@MainActivity).vincoloDao()
                    val allVincoli = dao.getAllVincoli().first()
                    val allAccounts = dao.getAllAccounts().first()
                    val accountIds = allAccounts.map { it.id }.toSet()
                    
                    for (v in allVincoli) {
                        if (!accountIds.contains(v.accountId)) {
                            dao.deleteVincolo(v) // Eliminazione definitiva orfani
                        }
                    }
                    appPrefs.edit().putBoolean("orphan_cleanup_v18", true).apply()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        // Routine di seeding Link Utili v20
        if (!appPrefs.getBoolean("seeded_links_v20", false)) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val dao = AppDatabase.getDatabase(this@MainActivity).vincoloDao()
                    AppDatabase.seedUsefulLinks(dao)
                    appPrefs.edit().putBoolean("seeded_links_v20", true).apply()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Passaggio di ogni ID menu come set di ID perché ogni
        // menu deve essere considerato come destinazione di primo livello.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_cruscotto, R.id.navigation_conti, R.id.navigation_lista_prodotti, R.id.navigation_lista_vincoli, R.id.navigation_utility, R.id.navigation_impostazioni
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Controllo Aggiornamento per mostrare il Changelog (solo una volta)
        val lastSeenVersion = appPrefs.getInt("last_seen_version", 0)
        val currentVersionCode = try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
            } else {
                packageManager.getPackageInfo(packageName, 0).versionCode
            }
        } catch (e: Exception) { 0 }

        if (currentVersionCode > lastSeenVersion) {
            // Se non è il primo avvio assoluto dell'app, mostra il changelog
            if (lastSeenVersion != 0 && !isFirstRun) {
                navController.navigate(R.id.navigation_changelog)
            }
            // Aggiorna la versione vista
            appPrefs.edit().putInt("last_seen_version", currentVersionCode).apply()
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            toolbar.subtitle = null
            
            // Sincronizzazione automatica dell'icona nella barra inferiore
            val menuId = when (destination.id) {
                R.id.navigation_cruscotto -> R.id.navigation_cruscotto
                R.id.navigation_conti, R.id.navigation_add_bank -> R.id.navigation_conti
                R.id.navigation_lista_prodotti, R.id.navigation_add_product, R.id.navigation_lista_prodotti_detail -> R.id.navigation_lista_prodotti
                R.id.navigation_lista_vincoli, R.id.navigation_add_vincolo, R.id.navigation_dettaglio_vincolo, R.id.navigation_storico_asset, R.id.navigation_lista_vincoli_detail -> R.id.navigation_lista_vincoli
                R.id.navigation_utility, R.id.navigation_calcolatrice_interessi, R.id.navigation_salto_staffa, R.id.navigation_links_utili, R.id.navigation_add_link_utile -> R.id.navigation_utility
                R.id.navigation_impostazioni, R.id.infoAppFragment, R.id.navigation_changelog -> R.id.navigation_impostazioni
                else -> null
            }
            menuId?.let { id ->
                navView.menu.findItem(id)?.let { item ->
                    if (!item.isChecked) {
                        item.isChecked = true
                    }
                }
            }

            val customColor = arguments?.getInt("customColor", -1) ?: -1
            
            val bgColor = if (customColor != -1) {
                customColor
            } else {
                val colorRes = when (destination.id) {
                    R.id.navigation_cruscotto -> R.color.azure_primary
                    R.id.navigation_conti,
                    R.id.navigation_add_bank -> R.color.blue_accounts
                    R.id.navigation_lista_prodotti,
                    R.id.navigation_add_product -> R.color.purple_products
                    R.id.navigation_lista_vincoli, 
                    R.id.navigation_add_vincolo, 
                    R.id.navigation_dettaglio_vincolo -> R.color.teal_list
                    R.id.navigation_utility, 
                    R.id.navigation_calcolatrice_interessi, 
                    R.id.navigation_salto_staffa,
                    R.id.navigation_links_utili,
                    R.id.navigation_add_link_utile -> R.color.yellow_utility
                    R.id.navigation_impostazioni,
                    R.id.infoAppFragment,
                    R.id.navigation_changelog -> R.color.amber_settings
                    else -> R.color.azure_primary
                }
                ContextCompat.getColor(this, colorRes)
            }
            
            toolbar.setBackgroundColor(bgColor)
            
            // Ripristinato il testo e le icone bianche per una migliore leggibilità su un colore di utilità più scuro
            val textColor = ContextCompat.getColor(this, R.color.white)
            toolbar.setTitleTextColor(textColor)
            toolbar.setNavigationIconTint(textColor)
            toolbar.overflowIcon?.setTint(textColor)
            
            // Assicura che le icone siano colorate correttamente dopo gli aggiornamenti di NavigationUI
            toolbar.post {
                toolbar.navigationIcon?.setTint(textColor)
                for (i in 0 until toolbar.menu.size()) {
                    toolbar.menu.getItem(i).icon?.setTint(textColor)
                }
            }
        }

        // Listener personalizzato per Bottom Nav per gestire specificamente lo stato/ripristino
        navView.setOnItemSelectedListener { item ->
            val builder = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(item.itemId != R.id.navigation_lista_vincoli && 
                                item.itemId != R.id.navigation_conti && 
                                item.itemId != R.id.navigation_lista_prodotti) 
                .setPopUpTo(
                    navController.graph.startDestinationId,
                    inclusive = false,
                    saveState = true
                )
            
            try {
                navController.navigate(item.itemId, null, builder.build())
                true
            } catch (e: Exception) {
                false
            }
        }

        toolbar.setNavigationOnClickListener {
            if (!navController.navigateUp()) {
                onBackPressedDispatcher.onBackPressed()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Padding superiore per la toolbar
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            
            // Padding inferiore per l\u0027area della barra di navigazione per evitare sovrapposizioni con la navigazione di sistema
            navView.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
    }
}
