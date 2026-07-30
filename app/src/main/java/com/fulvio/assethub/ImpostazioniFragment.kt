package com.fulvio.assethub

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentImpostazioniBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImpostazioniFragment : Fragment() {
    
    private var _binding: FragmentImpostazioniBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { exportDataToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { confermaImportazione(it) }
    }

    private val setPinLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val securityPrefs = requireContext().getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            securityPrefs.edit().putBoolean("pin_enabled", true).apply()
            binding.switchPin.isChecked = true
        } else {
            binding.switchPin.isChecked = securityPrefs.getBoolean("pin_enabled", false)
        }
    }

    private val verifyPinForBiometryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val securityPrefs = requireContext().getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            securityPrefs.edit().apply {
                putBoolean("biometry_enabled", true)
                putBoolean("pin_enabled", true)
                apply()
            }
            binding.switchBiometria.isChecked = true
            binding.switchPin.isChecked = true
        } else {
            binding.switchBiometria.isChecked = securityPrefs.getBoolean("biometry_enabled", false)
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(requireContext(), "Le notifiche non funzioneranno senza permesso", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImpostazioniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCollapsibleSections()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.settings_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_expand_all -> {
                        toggleAll(true)
                        true
                    }
                    R.id.action_collapse_all -> {
                        toggleAll(false)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val securityPrefs = requireContext().getSharedPreferences("security_prefs", Context.MODE_PRIVATE)

        binding.switchShowDeleted.isChecked = prefs.getBoolean("show_deleted", false)
        binding.switchShowDeleted.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_deleted", isChecked).apply()
        }

        binding.switchShowExpired.isChecked = prefs.getBoolean("show_expired", false)
        binding.switchShowExpired.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_expired", isChecked).apply()
        }

        binding.switchShowNotActive.isChecked = prefs.getBoolean("show_not_active", true)
        binding.switchShowNotActive.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("show_not_active", isChecked).apply()
        }

        binding.switchNotifyCedole.isChecked = prefs.getBoolean("notify_cedole", false)
        binding.switchNotifyCedole.setOnClickListener {
            val isChecked = binding.switchNotifyCedole.isChecked
            if (isChecked) {
                requestNotificationPermission()
                mostraDialogNotifiche("notify_cedole", "Notifiche Cedole")
            } else {
                prefs.edit().putBoolean("notify_cedole", false).apply()
                checkAndCancelWork()
            }
        }

        binding.switchNotifyScadenze.isChecked = prefs.getBoolean("notify_scadenze", false)
        binding.switchNotifyScadenze.setOnClickListener {
            val isChecked = binding.switchNotifyScadenze.isChecked
            if (isChecked) {
                requestNotificationPermission()
                mostraDialogNotifiche("notify_scadenze", "Notifiche Scadenze")
            } else {
                prefs.edit().putBoolean("notify_scadenze", false).apply()
                checkAndCancelWork()
            }
        }

        binding.btnTestNotifiche.setOnClickListener {
            NotificationHelper(requireContext()).triggerTestCheck()
            Toast.makeText(requireContext(), "Test avviato!", Toast.LENGTH_SHORT).show()
        }

        binding.switchPin.isChecked = securityPrefs.getBoolean("pin_enabled", false)
        binding.switchPin.setOnClickListener {
            val isChecked = binding.switchPin.isChecked
            if (isChecked) {
                val intent = Intent(requireContext(), LockActivity::class.java).apply {
                    putExtra("MODE", "SET")
                }
                setPinLauncher.launch(intent)
            } else {
                securityPrefs.edit().apply {
                    putBoolean("pin_enabled", false)
                    putBoolean("biometry_enabled", false)
                    apply()
                }
                binding.switchBiometria.isChecked = false
            }
        }

        binding.switchBiometria.isChecked = securityPrefs.getBoolean("biometry_enabled", false)
        binding.switchBiometria.setOnClickListener {
            val isChecked = binding.switchBiometria.isChecked
            if (isChecked) {
                val hasPin = securityPrefs.getString("user_pin", null) != null
                if (!hasPin) {
                    Toast.makeText(requireContext(), "Imposta prima un PIN valido", Toast.LENGTH_LONG).show()
                    binding.switchBiometria.isChecked = false
                } else {
                    val intent = Intent(requireContext(), LockActivity::class.java).apply {
                        putExtra("MODE", "VERIFY")
                    }
                    verifyPinForBiometryLauncher.launch(intent)
                }
            } else {
                securityPrefs.edit().putBoolean("biometry_enabled", false).apply()
            }
        }

        binding.switchBackgroundProtection.isChecked = securityPrefs.getBoolean("background_protection", false)
        binding.switchBackgroundProtection.setOnCheckedChangeListener { _, isChecked ->
            securityPrefs.edit().putBoolean("background_protection", isChecked).apply()
            // Applichiamo il flag immediatamente se attivo
            if (isChecked) {
                activity?.window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        binding.btnModificaPin.setOnClickListener {
            val hasPin = securityPrefs.getString("user_pin", null) != null
            if (hasPin) {
                val intent = Intent(requireContext(), LockActivity::class.java).apply {
                    putExtra("MODE", "CHANGE")
                }
                setPinLauncher.launch(intent)
            } else {
                Toast.makeText(requireContext(), "Nessun PIN impostato", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEliminaPin.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Elimina PIN")
                .setMessage("Sei sicuro di voler eliminare il PIN?")
                .setPositiveButton("ELIMINA") { _, _ ->
                    securityPrefs.edit().apply {
                        remove("user_pin")
                        putBoolean("pin_enabled", false)
                        putBoolean("biometry_enabled", false)
                        apply()
                    }
                    binding.switchPin.isChecked = false
                    binding.switchBiometria.isChecked = false
                    Toast.makeText(requireContext(), "PIN eliminato", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        binding.btnMesiCedole.setOnClickListener {
            mostraDialogMesi("mesi_cedole", "Visualizzazione Cedole")
        }

        binding.btnMesiVincoli.setOnClickListener {
            mostraDialogMesi("mesi_vincoli", "Visualizzazione Vincoli")
        }

        binding.btnGuida.setOnClickListener {
            startActivity(Intent(requireContext(), WelcomeActivity::class.java))
        }

        binding.btnChangelog.setOnClickListener {
            findNavController().navigate(R.id.action_impostazioni_to_changelog)
        }

        binding.btnDatiProva.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Carica Dati di Prova")
                .setMessage("Vuoi caricare i vincoli di esempio?")
                .setPositiveButton("Sì, Carica") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        WelcomeActivity.caricaDatiEsempioStatico(requireContext())
                        Toast.makeText(requireContext(), "Dati di prova caricati!", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        binding.btnEsportaBackup.setOnClickListener {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            AssetHubApp.ignoreNextForegroundBlock = true
            exportLauncher.launch("AssetHub_Backup_$timestamp.csv")
        }

        binding.btnImportaBackup.setOnClickListener {
            AssetHubApp.ignoreNextForegroundBlock = true
            importLauncher.launch(arrayOf("text/comma-separated-values", "text/csv", "application/csv"))
        }

        binding.btnResetTotale.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Reset Totale")
                .setMessage("Sei sicuro di voler eliminare TUTTI i dati? L'operazione è irreversibile.")
                .setPositiveButton("ELIMINA TUTTO") { _, _ ->
                    val appContext = requireContext().applicationContext
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            // 1. Chiude e cancella fisicamente il database
                            val database = AppDatabase.getDatabase(appContext)
                            database.close()
                            appContext.deleteDatabase("imieivincoli_database")

                            // 2. Sincronizza la cancellazione delle preferenze (commit invece di apply)
                            appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().clear().commit()
                            appContext.getSharedPreferences("security_prefs", Context.MODE_PRIVATE).edit().clear().commit()
                            appContext.getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE).edit().clear().commit()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(appContext, "Reset completato. L'app verrà chiusa.", Toast.LENGTH_LONG).show()
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    activity?.finishAffinity()
                                    System.exit(0)
                                }, 2000)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(appContext, "Errore durante il reset: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
                .setNegativeButton("Annulla", null)
                .show()
        }

        binding.btnInfo.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_impostazioni_to_infoAppFragment)
        }

        aggiornaTestoPulsantiMesi()
    }

    private fun aggiornaTestoPulsantiMesi() {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val valCedole = prefs.getInt("mesi_cedole", 2)
        val testoCedole = when(valCedole) {
            999 -> "Tutto"
            else -> "$valCedole mesi"
        }
        binding.btnMesiCedole.text = "VISUAL. CEDOLE IN ARRIVO ($testoCedole)"

        val valVincoli = prefs.getInt("mesi_vincoli", 2)
        val testoVincoli = when(valVincoli) {
            999 -> "Tutto"
            else -> "$valVincoli mesi"
        }
        binding.btnMesiVincoli.text = "VISUAL. STRUM. IN SCAD. ($testoVincoli)"
    }

    private fun setupCollapsibleSections() {
        binding.headerDatiVis.setOnClickListener { toggleSection(binding.contentDatiVis, binding.imgArrowDatiVis) }
        binding.headerNotifiche.setOnClickListener { toggleSection(binding.contentNotifiche, binding.imgArrowNotifiche) }
        binding.headerBackup.setOnClickListener { toggleSection(binding.contentBackup, binding.imgArrowBackup) }
        binding.headerSicurezza.setOnClickListener { toggleSection(binding.contentSicurezza, binding.imgArrowSicurezza) }
        binding.headerInfo.setOnClickListener { toggleSection(binding.contentInfo, binding.imgArrowInfo) }
        binding.headerManutenzione.setOnClickListener { toggleSection(binding.contentManutenzione, binding.imgArrowManutenzione) }
    }

    private fun toggleSection(content: View, arrow: View) {
        val isVisible = content.visibility == View.VISIBLE
        content.visibility = if (isVisible) View.GONE else View.VISIBLE
        arrow.rotation = if (isVisible) 0f else 180f
    }

    private fun toggleAll(expand: Boolean) {
        val visibility = if (expand) View.VISIBLE else View.GONE
        val rotation = if (expand) 180f else 0f
        binding.contentDatiVis.visibility = visibility
        binding.imgArrowDatiVis.rotation = rotation
        binding.contentNotifiche.visibility = visibility
        binding.imgArrowNotifiche.rotation = rotation
        binding.contentBackup.visibility = visibility
        binding.imgArrowBackup.rotation = rotation
        binding.contentSicurezza.visibility = visibility
        binding.imgArrowSicurezza.rotation = rotation
        binding.contentInfo.visibility = visibility
        binding.imgArrowInfo.rotation = rotation
        binding.contentManutenzione.visibility = visibility
        binding.imgArrowManutenzione.rotation = rotation
    }

    private fun confermaImportazione(uri: Uri) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Conferma Importazione")
            .setMessage("L'importazione sostituirà TUTTI i dati attuali. Vuoi procedere?")
            .setPositiveButton("SÌ, IMPORTA") { _, _ ->
                importDataFromUri(uri)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun importDataFromUri(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(appContext)
                val prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

                appContext.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = inputStream.bufferedReader()
                    val dao = database.vincoloDao()
                    
                    database.clearAllTables()
                    AppDatabase.seedCategories(appContext, dao)
                    
                    // Mappa delle categorie per systemType per ricollegamento sicuro
                    val seededCategories = dao.getAllCategories().first()
                    val categoryTypeMap = seededCategories.associateBy { it.systemType }
                    
                    val bankIdMap = mutableMapOf<Long, Long>()
                    val accountIdMap = mutableMapOf<Long, Long>()
                    val depositoCategory = categoryTypeMap[Category.TYPE_DEPOSITO]
                    val defaultCategoryId = depositoCategory?.id ?: 1L
                    val vincoliDaInserire = mutableListOf<Vincolo>()
                    var linksImported = false

                    for (line in reader.lineSequence()) {
                        if (line.isBlank()) continue
                        val parts = line.split(";") // Torniamo a split semplice

                        when (parts[0]) {
                            "VALUES" -> {
                                if (parts.size >= 4) {
                                    prefs.edit().apply {
                                        putBoolean("hide_expired", parts[1].toBoolean())
                                        putInt("mesi_cedole", parts[2].toIntOrNull() ?: 2)
                                        putInt("mesi_vincoli", parts[3].toIntOrNull() ?: 2)
                                        apply()
                                    }
                                }
                            }
                            "BANK" -> {
                                if (parts.size >= 4) {
                                    val oldId = parts[1].toLongOrNull() ?: 0L
                                    val newId = dao.insertBank(Bank(
                                        name = unescapeCsv(parts[2]) ?: "",
                                        color = parts[3].toIntOrNull() ?: 0,
                                        isDeleted = if (parts.size >= 5) parts[4].toBoolean() else false
                                    ))
                                    bankIdMap[oldId] = newId
                                }
                            }
                            "ACCOUNT" -> {
                                if (parts.size >= 5) {
                                    val oldId = parts[1].toLongOrNull() ?: 0L
                                    val oldBankId = parts[2].toLongOrNull() ?: 0L
                                    val newBankId = bankIdMap[oldBankId] ?: 0L
                                    
                                    val catType = if (parts.size >= 9) parts[8] else null
                                    val mappedCategoryId = if (!catType.isNullOrBlank() && categoryTypeMap.containsKey(catType)) {
                                        categoryTypeMap[catType]!!.id
                                    } else {
                                        defaultCategoryId
                                    }

                                    val newId = dao.insertAccount(Account(
                                        bankId = newBankId,
                                        categoryId = mappedCategoryId,
                                        name = unescapeCsv(parts[4]) ?: "",
                                        frequenzaRendicontazione = if (parts.size >= 6) parts[5] else "Trimestrale",
                                        lastUpdate = if (parts.size >= 7) parts[6].toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis(),
                                        isDeleted = if (parts.size >= 8) parts[7].toBoolean() else false,
                                        bolloCaricoBanca = if (parts.size >= 10) parts[9].toBoolean() else false
                                    ))
                                    accountIdMap[oldId] = newId
                                }
                            }
                            "VINCOLO" -> {
                                if (parts.size >= 16) {
                                    val oldAccId = parts[2].toLongOrNull() ?: 0L
                                    val mappedAccId = if (accountIdMap.containsKey(oldAccId)) accountIdMap[oldAccId]!! else {
                                        oldAccAccId(parts, oldAccId, accountIdMap, dao, defaultCategoryId)
                                    }
                                    
                                    val v = Vincolo(
                                        id = 0,
                                        accountId = mappedAccId,
                                        nome = unescapeCsv(parts[3]) ?: "",
                                        dataDecorrenza = parts[4].toLongOrNull() ?: 0L,
                                        durataMesi = parseSafeInt(parts[5]),
                                        svincolabile = parts[6].toBoolean(),
                                        importo = parts[7].toDoubleOrNull() ?: 0.0,
                                        tassoVincolo = parts[8].toDoubleOrNull() ?: 0.0,
                                        tassoSvincolo = parts[9].toDoubleOrNull() ?: 0.0,
                                        periodoCedolaMesi = parseSafeInt(parts[10]),
                                        tassazione = parts[11].toDoubleOrNull() ?: 0.26,
                                        bolloCaricoBanca = parts[12].toBoolean(),
                                        tipo = unescapeCsv(parts[13]) ?: "Conto Deposito",
                                        note = unescapeCsv(parts[14]),
                                        codiceVincolo = parseSafeInt(parts[15]),
                                        interessiMaturatiPrecedenti = if (parts.size >= 17) parts[16].toDoubleOrNull() ?: 0.0 else 0.0,
                                        frequenzaRendicontazione = if (parts.size >= 18) parts[17] else "Trimestrale",
                                        bolliConsolidati = if (parts.size >= 19) parts[18].toDoubleOrNull() ?: 0.0 else 0.0,
                                        strumentoDettaglio = if (parts.size >= 20) unescapeCsv(parts[19]) else null,
                                        isDeleted = if (parts.size >= 21) parts[20].toBoolean() else false,
                                        quotaVariazione = if (parts.size >= 22) parts[21].toDoubleOrNull() ?: 0.0 else 0.0,
                                        numeroQuote = if (parts.size >= 23) parts[22].toDoubleOrNull() ?: 0.0 else 0.0,
                                        prezzoAcquisto = if (parts.size >= 24) parts[23].toDoubleOrNull() ?: 0.0 else 0.0
                                    )
                                    vincoliDaInserire.add(v)
                                }
                            }
                            "USEFUL_LINK" -> {
                                if (parts.size >= 6) {
                                    linksImported = true
                                    dao.insertUsefulLink(UsefulLink(
                                        title = unescapeCsv(parts[2]) ?: "",
                                        description = unescapeCsv(parts[3]) ?: "",
                                        url = unescapeCsv(parts[4]) ?: "",
                                        iconResId = parts[5].toIntOrNull() ?: R.drawable.ic_globe
                                    ))
                                }
                            }
                        }
                    }

                    for (v in vincoliDaInserire) {
                        dao.insertVincolo(v)
                    }

                    // Se il backup è vecchio e non aveva link, ripristiniamo quelli di default
                    if (!linksImported) {
                        AppDatabase.seedUsefulLinks(dao)
                    }

                    val maxCodice = vincoliDaInserire.maxOfOrNull { it.codiceVincolo } ?: 0
                    prefs.edit().putInt("last_codice_vincolo", maxCodice).apply()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(appContext, "Ripristino completato con successo!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private suspend fun oldAccAccId(parts: List<String>, oldAccId: Long, accountIdMap: MutableMap<Long, Long>, dao: VincoloDao, defaultCat: Long): Long {
        if (accountIdMap.containsKey(oldAccId)) return accountIdMap[oldAccId]!!
        
        val bancaNome = unescapeCsv(parts[3]) ?: "Recuperata"
        val existingBanks = dao.getAllBanks().first()
        var bank = existingBanks.find { it.name == bancaNome }
        if (bank == null) {
            val bId = dao.insertBank(Bank(name = bancaNome, color = android.graphics.Color.GRAY))
            bank = Bank(id = bId, name = bancaNome, color = android.graphics.Color.GRAY)
        }
        
        val existingAccounts = dao.getAllAccounts().first()
        var account = existingAccounts.find { it.bankId == bank.id && it.name == bancaNome }
        if (account == null) {
            val aId = dao.insertAccount(Account(bankId = bank.id, categoryId = defaultCat, name = bancaNome))
            account = Account(id = aId, bankId = bank.id, categoryId = defaultCat, name = bancaNome)
        }
        accountIdMap[oldAccId] = account.id
        return account.id
    }

    private fun exportDataToUri(uri: Uri) {
        val appContext = requireContext().applicationContext
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val database = AppDatabase.getDatabase(appContext)
                val dao = database.vincoloDao()
                val prefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                
                val banks = dao.getAllBanks().first()
                val accounts = dao.getAllAccounts().first()
                val vincoli = dao.getAllVincoli().first()

                appContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write("SETTINGS;hide_expired;mesi_cedole;mesi_vincoli")
                        writer.newLine()
                        writer.write("VALUES;${prefs.getBoolean("hide_expired", false)};${prefs.getInt("mesi_cedole", 2)};${prefs.getInt("mesi_vincoli", 2)}")
                        writer.newLine()
                        writer.newLine()

                        writer.write("HEADER_BANKS;id;name;color;isDeleted")
                        writer.newLine()
                        for (b in banks) {
                            writer.write("BANK;${b.id};${escapeCsv(b.name)};${b.color};${b.isDeleted}")
                            writer.newLine()
                        }
                        writer.newLine()

                        writer.write("HEADER_ACCOUNTS;id;bankId;categoryId;name;frequenzaRendicontazione;lastUpdate;isDeleted;categoryType;bolloCaricoBanca")
                        writer.newLine()
                        for (a in accounts) {
                            // Recuperiamo il systemType della categoria per rendere il backup indipendente dall'ID
                            val catType = try {
                                dao.getAllCategories().first().find { it.id == a.categoryId }?.systemType ?: ""
                            } catch (_: Exception) { "" }
                            
                            writer.write("ACCOUNT;${a.id};${a.bankId};${a.categoryId};${escapeCsv(a.name)};${a.frequenzaRendicontazione};${a.lastUpdate};${a.isDeleted};$catType;${a.bolloCaricoBanca}")
                            writer.newLine()
                        }
                        writer.newLine()

                        writer.write("HEADER_VINCOLI;id;accountId;nome;dataDecorrenza;durataMesi;svincolabile;importo;tassoVincolo;tassoSvincolo;periodoCedolaMesi;tassazione;bolloCaricoBanca;tipo;note;codiceVincolo;interessiMaturatiPrecedenti;frequenzaRendicontazione;bolliConsolidati;strumentoDettaglio;isDeleted;quotaVariazione;numeroQuote;prezzoAcquisto")
                        writer.newLine()
                        for (v in vincoli) {
                            val line = "VINCOLO;${v.id};${v.accountId};${escapeCsv(v.nome)};${v.dataDecorrenza};${v.durataMesi};${v.svincolabile};${v.importo};${v.tassoVincolo};${v.tassoSvincolo};${v.periodoCedolaMesi};${v.tassazione};${v.bolloCaricoBanca};${escapeCsv(v.tipo)};${escapeCsv(v.note)};${v.codiceVincolo};${v.interessiMaturatiPrecedenti};${v.frequenzaRendicontazione};${v.bolliConsolidati};${escapeCsv(v.strumentoDettaglio)};${v.isDeleted};${v.quotaVariazione};${v.numeroQuote};${v.prezzoAcquisto}"
                            writer.write(line)
                            writer.newLine()
                        }
                        writer.newLine()

                        val links = dao.getAllUsefulLinks().first()
                        writer.write("HEADER_USEFUL_LINKS;id;title;description;url;iconResId")
                        writer.newLine()
                        for (l in links) {
                            writer.write("USEFUL_LINK;${l.id};${escapeCsv(l.title)};${escapeCsv(l.description)};${escapeCsv(l.url)};${l.iconResId}")
                            writer.newLine()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Backup salvato con successo!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun escapeCsv(text: String?): String {
        if (text == null) return ""
        return text.replace("\n", "[NWL]").replace(";", "[SMC]")
    }

    private fun unescapeCsv(text: String?): String? {
        if (text == null || text.isBlank() || text == "null") return null
        return text.replace("[NWL]", "\n").replace("[SMC]", ";")
    }

    private fun parseSafeInt(value: String?): Int {
        if (value == null || value.isBlank()) return 0
        return try {
            // Gestisce formati come "12", "12.0", "12,0"
            value.replace(',', '.').toDouble().toInt()
        } catch (e: Exception) {
            0
        }
    }

    private fun mostraDialogMesi(key: String, title: String) {
        val options = arrayOf("2 mesi (Default)", "4 mesi", "6 mesi", "Tutto")
        val values = intArrayOf(2, 4, 6, 999)
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val currentValue = prefs.getInt(key, 2)
        val currentSelection = values.indexOf(currentValue).coerceAtLeast(0)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                prefs.edit().putInt(key, values[which]).apply()
                aggiornaTestoPulsantiMesi()
                dialog.dismiss()
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun mostraDialogNotifiche(key: String, title: String) {
        val options = arrayOf("Il giorno stesso (8:00 AM)", "Il giorno prima (8:00 AM)", "Entrambe le scelte")
        val values = intArrayOf(1, 2, 3)
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val modeKey = "${key}_mode"
        val currentValue = prefs.getInt(modeKey, 1)
        val currentSelection = values.indexOf(currentValue).coerceAtLeast(0)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                prefs.edit().apply {
                    putBoolean(key, true)
                    putInt(modeKey, values[which])
                    apply()
                }
                NotificationHelper(requireContext()).scheduleDailyCheck()
                dialog.dismiss()
            }
            .setNegativeButton("Annulla") { _, _ ->
                if (!prefs.getBoolean(key, false)) {
                    if (key == "notify_cedole") binding.switchNotifyCedole.isChecked = false
                    else binding.switchNotifyScadenze.isChecked = false
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun checkAndCancelWork() {
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("notify_cedole", false) && !prefs.getBoolean("notify_scadenze", false)) {
            NotificationHelper(requireContext()).cancelDailyCheck()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
