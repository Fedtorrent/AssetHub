package com.fulvio.assethub

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentAddVincoloBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AddVincoloFragment : Fragment() {

    private var _binding: FragmentAddVincoloBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VincoliViewModel by viewModels()
    private var vincoloId: Long = -1L
    private var isDuplicate: Boolean = false
    private var currentVincolo: Vincolo? = null
    private var selectedAccountId: Long = -1L
    private var selectedBankId: Long = -1L
    private var argAssetName: String? = null
    
    private var allBanks: List<Bank> = emptyList()
    private var allAccounts: List<Account> = emptyList()
    private var categories: List<Category> = emptyList()

    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    private var isUpdatingQuotes = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddVincoloBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vincoloId = arguments?.getLong("vincoloId") ?: -1L
        isDuplicate = arguments?.getBoolean("isDuplicate") ?: false
        val argAccountId = arguments?.getLong("accountId") ?: -1L
        argAssetName = arguments?.getString("assetName")

        setupDatePicker()
        setupSpinners()
        setupBidirectionalListeners()

        // Caricamento Dati Iniziali
        viewModel.allCategories.observe(viewLifecycleOwner) { categories = it }
        
        viewModel.allBanksWithAccounts.observe(viewLifecycleOwner) { banksWithAccounts ->
            allBanks = banksWithAccounts.map { it.bank }.filter { !it.isDeleted }
            allAccounts = banksWithAccounts.flatMap { it.accounts.map { a -> a.account } }.filter { !it.isDeleted }
            
            setupBankSpinner()

            if (vincoloId != -1L) {
                caricaDatiVincolo(vincoloId)
            } else if (argAccountId != -1L) {
                // INSERIMENTO RAPIDO DA CONTO (+)
                selectedAccountId = argAccountId
                val account = allAccounts.find { it.id == argAccountId }
                account?.let { acc ->
                    selectedBankId = acc.bankId
                    
                    // Mostriamo Banca e Conto in sola visione (disabilitati)
                    binding.layoutBanca.visibility = View.VISIBLE
                    binding.layoutBanca.isEnabled = false
                    binding.layoutAccount.visibility = View.VISIBLE
                    binding.layoutAccount.isEnabled = false
                    
                    val bank = allBanks.find { it.id == acc.bankId }
                    binding.spinnerBank.setText(bank?.name ?: "", false)
                    binding.spinnerAccount.setText(acc.name, false)
                    
                    // Sblocchiamo il passo successivo (Tipo Strumento o Campi)
                    applicaTipoDaAccount(acc.id)
                }
                
                argAssetName?.let {
                    binding.editNome.setText(it)
                    binding.layoutNome.visibility = View.GONE
                }
            } else {
                // INSERIMENTO DA BARRA INFERIORE (Percorso Guidato)
                resetPercorso()
            }
        }

        binding.btnSalva.setOnClickListener {
            salvaVincolo()
        }
    }

    private fun resetPercorso() {
        binding.layoutBanca.visibility = View.VISIBLE
        binding.layoutAccount.visibility = View.GONE
        binding.layoutTipo.visibility = View.GONE
        binding.layoutStrumentoDettaglio.visibility = View.GONE
        nascondiTuttiCampiModello()
    }

    private fun nascondiTuttiCampiModello() {
        binding.layoutNome.visibility = View.GONE
        binding.layoutQuotesContainer.visibility = View.GONE
        binding.layoutDecorrenza.visibility = View.GONE
        binding.layoutDurata.visibility = View.GONE
        binding.layoutSvincolabile.visibility = View.GONE
        binding.layoutImporto.visibility = View.GONE
        binding.layoutTassoVincolo.visibility = View.GONE
        binding.layoutTassoSvincolo.visibility = View.GONE
        binding.layoutPeriodoCedola.visibility = View.GONE
        binding.layoutTassazione.visibility = View.GONE
        binding.layoutRendicontazione.visibility = View.GONE
        binding.layoutNote.visibility = View.GONE
        binding.btnSalva.visibility = View.GONE
        binding.textNoteFondo.visibility = View.GONE
    }

    private fun setupBankSpinner() {
        val bankNames = allBanks.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, bankNames)
        binding.spinnerBank.setAdapter(adapter)
        
        binding.spinnerBank.setOnItemClickListener { _, _, position, _ ->
            val selectedBank = allBanks[position]
            selectedBankId = selectedBank.id
            selectedAccountId = -1L
            binding.spinnerAccount.setText("", false)
            
            // Step 2: Mostra Conto
            binding.layoutAccount.visibility = View.VISIBLE
            binding.layoutTipo.visibility = View.GONE
            binding.layoutStrumentoDettaglio.visibility = View.GONE
            nascondiTuttiCampiModello()
            
            setupAccountSpinner(selectedBank.id)
        }
    }

    private fun setupAccountSpinner(bankId: Long) {
        val filteredAccounts = allAccounts.filter { it.bankId == bankId }
        val accountNames = filteredAccounts.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountNames)
        binding.spinnerAccount.setAdapter(adapter)
        
        binding.spinnerAccount.setOnItemClickListener { _, _, position, _ ->
            val selectedAccount = filteredAccounts[position]
            selectedAccountId = selectedAccount.id
            
            nascondiTuttiCampiModello()
            applicaTipoDaAccount(selectedAccount.id)
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.editDecorrenza.setText(dateFormatter.format(calendar.time))
        }

        binding.editDecorrenza.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSpinners() {
        val strumentoDettaglioOptions = arrayOf("Azioni", "BFP", "BTC", "Certificato", "ETC", "ETF", "ETN", "Fondo", "Obbl.Societarie", "Titoli di Stato")
        val yesNoOptions = arrayOf("No", "Sì")
        val taxOptions = arrayOf("26,00%", "12,50%")
        
        binding.spinnerStrumentoDettaglio.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, strumentoDettaglioOptions))
        binding.spinnerStrumentoDettaglio.setOnItemClickListener { _, _, position, _ ->
            nascondiTuttiCampiModello()
            handleStrumentoDettaglioSelection(strumentoDettaglioOptions[position])
        }

        binding.spinnerSvincolabile.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, yesNoOptions))
        binding.spinnerSvincolabile.setOnItemClickListener { _, _, position, _ ->
            binding.layoutTassoSvincolo.visibility = if (yesNoOptions[position] == "Sì") View.VISIBLE else View.GONE
        }

        binding.spinnerTassazione.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, taxOptions))
    }

    private fun setupBidirectionalListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingQuotes) return
                isUpdatingQuotes = true
                
                val q = binding.editNumeroQuote.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                val p = binding.editPrezzoAcquisto.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                val i = binding.editImporto.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
                
                val focusedView = activity?.currentFocus
                when (focusedView?.id) {
                    R.id.edit_numero_quote, R.id.edit_prezzo_acquisto -> {
                        if (q > 0 && p > 0) {
                            val total = q * p
                            binding.editImporto.setText(String.format(Locale.ITALY, "%.2f", total).replace('.', ','))
                        }
                    }
                    R.id.edit_importo -> {
                        if (i > 0 && p > 0) {
                            val quotes = i / p
                            binding.editNumeroQuote.setText(String.format(Locale.ITALY, "%.4f", quotes).replace('.', ','))
                        }
                    }
                }
                isUpdatingQuotes = false
            }
        }
        binding.editNumeroQuote.addTextChangedListener(watcher)
        binding.editPrezzoAcquisto.addTextChangedListener(watcher)
        binding.editImporto.addTextChangedListener(watcher)
    }

    private fun applicaTipoDaAccount(accountId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val account = viewModel.getAccountById(accountId)
            account?.let { acc ->
                val cat = categories.find { it.id == acc.categoryId }
                cat?.let {
                    val systemType = it.systemType
                    val displayType = getDisplayType(systemType, it.name)
                    binding.spinnerTipo.setText(displayType, false)
                    
                    if (systemType == Category.TYPE_TITOLI) {
                        // Step 3 per Titoli: Scelta Strumento
                        binding.layoutStrumentoDettaglio.visibility = View.VISIBLE
                        // Se abbiamo un nome asset (PAC), cerchiamo l'ultimo inserimento per ereditare il dettaglio
                        if (argAssetName != null) {
                            val lastEntry = viewModel.getLastVincoloByAccountAndName(acc.id, argAssetName!!)
                            lastEntry?.strumentoDettaglio?.let { detail ->
                                binding.spinnerStrumentoDettaglio.setText(detail, false)
                                binding.layoutStrumentoDettaglio.visibility = View.VISIBLE 
                                handleStrumentoDettaglioSelection(detail)
                            }
                        }
                    } else {
                        // Altri tipi: Passiamo subito ai campi
                        binding.layoutStrumentoDettaglio.visibility = View.GONE
                        aggiornaVisibilitaCampi(displayType)
                    }
                }
            }
        }
    }

    private fun getDisplayType(systemType: String, defaultName: String): String {
        return when (systemType) {
            Category.TYPE_CORRENTE -> "Conto Corrente"
            Category.TYPE_DEPOSITO_LIBERO -> "Conto Deposito Libero"
            Category.TYPE_DEPOSITO -> "Conto Deposito"
            Category.TYPE_TITOLI -> "Conto Titoli"
            Category.TYPE_PENSIONE -> "Fondo Pensione"
            Category.TYPE_IMMOBILI -> "Immobili"
            Category.TYPE_CONTANTI -> "Contanti"
            Category.TYPE_VEICOLI -> "Veicoli"
            Category.TYPE_GIOIELLI -> "Gioielli"
            Category.TYPE_OGGETTI -> "Oggetti di valore"
            else -> defaultName
        }
    }

    private fun handleStrumentoDettaglioSelection(dettaglio: String) {
        val type = binding.spinnerTipo.text.toString()
        aggiornaVisibilitaCampi(type)
    }

    private fun aggiornaVisibilitaCampi(tipo: String) {
        val color = arguments?.getInt("customColor", -1) ?: -1
        if (color != -1) binding.btnSalva.backgroundTintList = android.content.res.ColorStateList.valueOf(color)

        val isEdit = vincoloId != -1L && !isDuplicate
        val isPAC = argAssetName != null
        val isHistoryAccount = tipo == "Conto Corrente" || tipo == "Conto Deposito Libero" || tipo == "Fondo Pensione" || 
                               tipo == "Immobili" || tipo == "Contanti" || tipo == "Veicoli" || 
                               tipo == "Gioielli" || tipo == "Oggetti di valore"

        // GESTIONE TITOLO PAGINA
        val title = when {
            isHistoryAccount -> if (isEdit) "Modifica Valore" else "Aggiorna Valore Asset"
            isPAC -> if (isEdit) "Modifica Movimento" else "Aggiungi Movimento"
            else -> if (isEdit) "Modifica Strumento" else "Aggiungi Strumento"
        }
        (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = title

        val detail = binding.spinnerStrumentoDettaglio.text.toString()
        
        // MODELLI CONTI TITOLI
        val isQuoteModel = tipo == "Conto Titoli" && InstrumentUtils.isIncremental("Conto Titoli", detail)
        val isFixedIncomeModel = tipo == "Conto Titoli" && (detail == "Certificato" || detail == "Obbl.Societarie")
        val isStatoModel = tipo == "Conto Titoli" && detail == "Titoli di Stato"
        val isBFPModel = tipo == "Conto Titoli" && detail == "BFP"

        nascondiTuttiCampiModello()

        // GESTIONE VISIBILITA BASE
        val isGenericPhysical = tipo == "Immobili" || tipo == "Contanti" || tipo == "Veicoli" || 
                                tipo == "Gioielli" || tipo == "Oggetti di valore"
        
        binding.layoutNome.visibility = if (argAssetName != null || tipo == "Conto Corrente" || tipo == "Conto Deposito Libero" || tipo == "Fondo Pensione" || isGenericPhysical) View.GONE else View.VISIBLE
        binding.layoutDecorrenza.visibility = View.VISIBLE
        binding.layoutImporto.visibility = View.VISIBLE
        binding.layoutNote.visibility = View.VISIBLE
        binding.btnSalva.visibility = View.VISIBLE

        // MODELLO: QUOTE (ETF, Azioni, ecc.)
        if (isQuoteModel) {
            binding.layoutQuotesContainer.visibility = View.VISIBLE
            binding.layoutImporto.hint = "Importo Speso"
            binding.layoutNumeroQuote.hint = "Numero Quote"
            binding.layoutPrezzoAcquisto.hint = "Valore Quota"
            binding.layoutTassazione.visibility = View.VISIBLE
            binding.spinnerTassazione.isEnabled = true
            if (binding.spinnerTassazione.text.isEmpty()) binding.spinnerTassazione.setText("26,00%", false)
        } 
        // MODELLO: FIXED INCOME (Certificati, Obbligazioni)
        else if (isFixedIncomeModel) {
            binding.layoutDurata.visibility = View.VISIBLE
            binding.layoutTassoVincolo.visibility = View.VISIBLE
            binding.layoutPeriodoCedola.visibility = View.VISIBLE
            binding.layoutImporto.hint = "Capitale Investito"
            binding.layoutTassoVincolo.hint = "Tasso d'interesse"
            binding.layoutTassazione.visibility = View.VISIBLE
            binding.spinnerTassazione.isEnabled = true
            if (binding.spinnerTassazione.text.isEmpty()) binding.spinnerTassazione.setText("26,00%", false)
        }
        // MODELLO: TITOLI DI STATO
        else if (isStatoModel) {
            binding.layoutDurata.visibility = View.VISIBLE
            binding.layoutTassoVincolo.visibility = View.VISIBLE
            binding.layoutPeriodoCedola.visibility = View.VISIBLE
            binding.layoutImporto.hint = "Capitale Investito"
            binding.layoutTassoVincolo.hint = "Tasso d'interesse"
            binding.layoutTassazione.visibility = View.VISIBLE
            binding.spinnerTassazione.setText("12,50%", false)
            binding.spinnerTassazione.isEnabled = false // Bloccato al 12.5%
        }
        // MODELLO: BFP
        else if (isBFPModel) {
            binding.layoutDurata.visibility = View.VISIBLE
            binding.layoutDurata.hint = "Numero mesi di vincolo"
            binding.layoutImporto.visibility = View.VISIBLE
            binding.layoutImporto.hint = "Capitale Investito"
            binding.layoutTassazione.visibility = View.GONE
            binding.layoutNote.visibility = View.VISIBLE
        }
        // LEGACY / ALTRI TIPI
        else {
            when (tipo) {
                "Conto Corrente", "Conto Deposito Libero", "Fondo Pensione", "Immobili", 
                "Contanti", "Veicoli", "Gioielli", "Oggetti di valore" -> {
                    binding.layoutTassazione.visibility = View.GONE
                    
                    if (isGenericPhysical) {
                        binding.layoutImporto.hint = "Valore Asset"
                    } else {
                        binding.layoutImporto.hint = "Saldo"
                        binding.layoutTassoVincolo.visibility = View.VISIBLE
                        binding.layoutTassoVincolo.hint = "Tasso d' Interesse"
                        
                        if (tipo == "Conto Corrente" || tipo == "Conto Deposito Libero") {
                            binding.layoutPeriodoCedola.visibility = View.VISIBLE
                            binding.layoutPeriodoCedola.hint = "Periodicità Interessi (Mesi)"
                        }
                    }
                }
                "Conto Deposito" -> {
                    binding.layoutDurata.visibility = View.VISIBLE
                    binding.layoutSvincolabile.visibility = View.VISIBLE
                    binding.layoutTassoVincolo.visibility = View.VISIBLE
                    binding.layoutPeriodoCedola.visibility = View.VISIBLE
                    binding.layoutTassazione.visibility = View.VISIBLE
                    binding.layoutImporto.hint = "Capitale Investito"
                }
            }
        }
    }

    private fun caricaDatiVincolo(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val v = viewModel.getById(id)
            if (v != null) {
                currentVincolo = v
                selectedAccountId = v.accountId
                selectedBankId = allAccounts.find { it.id == v.accountId }?.bankId ?: -1L
                
                binding.spinnerBank.setText(allBanks.find { it.id == selectedBankId }?.name ?: "", false)
                binding.spinnerAccount.setText(allAccounts.find { it.id == v.accountId }?.name ?: "", false)
                
                binding.editNome.setText(v.nome)
                binding.spinnerTipo.setText(v.tipo, false)
                
                if (v.tipo == "Conto Titoli" && v.strumentoDettaglio != null) {
                    binding.spinnerStrumentoDettaglio.setText(v.strumentoDettaglio, false)
                    binding.layoutStrumentoDettaglio.visibility = View.VISIBLE
                }

                if (v.numeroQuote > 0) binding.editNumeroQuote.setText(v.numeroQuote.toString().replace('.', ','))
                if (v.prezzoAcquisto > 0) binding.editPrezzoAcquisto.setText(v.prezzoAcquisto.toString().replace('.', ','))
                binding.editImporto.setText((if (isDuplicate && v.quotaVariazione != 0.0) v.quotaVariazione else v.importo).toString().replace('.', ','))
                binding.editDurata.setText(v.durataMesi.toString())
                binding.editTassoVincolo.setText(v.tassoVincolo.toString().replace('.', ','))
                calendar.timeInMillis = v.dataDecorrenza
                binding.editDecorrenza.setText(dateFormatter.format(calendar.time))
                binding.spinnerSvincolabile.setText(if (v.svincolabile) "Sì" else "No", false)
                binding.editTassoSvincolo.setText(v.tassoSvincolo.toString().replace('.', ','))
                binding.editPeriodoCedola.setText(v.periodoCedolaMesi.toString())
                binding.spinnerTassazione.setText(if (v.tassazione == 0.125) "12,50%" else "26,00%", false)
                binding.editNote.setText(v.note)
                
                binding.layoutBanca.visibility = View.VISIBLE
                binding.layoutAccount.visibility = View.VISIBLE
                aggiornaVisibilitaCampi(v.tipo)
            }
        }
    }

    private fun salvaVincolo() {
        val tipo = binding.spinnerTipo.text.toString()
        val detail = binding.spinnerStrumentoDettaglio.text.toString()
        val nome = binding.editNome.text.toString()
        val importoStr = binding.editImporto.text.toString().replace(',', '.')
        val decorrenzaStr = binding.editDecorrenza.text.toString()
        
        if (selectedAccountId == -1L) { Toast.makeText(requireContext(), "Seleziona Banca e Conto", Toast.LENGTH_SHORT).show(); return }
        if (decorrenzaStr.isBlank()) { binding.layoutDecorrenza.error = "Obbligatorio"; return }
        if (importoStr.isBlank()) { binding.layoutImporto.error = "Obbligatorio"; return }

        val importo = importoStr.toDoubleOrNull() ?: 0.0
        val nQuote = binding.editNumeroQuote.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val prezzo = binding.editPrezzoAcquisto.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val tassazione = if (binding.spinnerTassazione.text.toString() == "12,50%") 0.125 else 0.26
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val nomeAsset = argAssetName ?: nome
                
                // Per l'ereditarietà del tasso, cerchiamo l'ultimo movimento del conto a prescindere dal nome
                val lastEntryAnyName = viewModel.getLastVincoloByAccount(selectedAccountId)
                val existingAsset = viewModel.getLastVincoloByAccountAndName(selectedAccountId, nomeAsset)
                
                val isHistoryBased = InstrumentUtils.isHistoryBased(Vincolo(accountId = selectedAccountId, nome = nomeAsset, tipo = tipo, strumentoDettaglio = detail, dataDecorrenza = 0, durataMesi = 0, svincolabile = false, importo = 0.0, tassoVincolo = 0.0, tassoSvincolo = 0.0, periodoCedolaMesi = 0, tassazione = 0.0, bolloCaricoBanca = false))

                var codiceDaAssegnare = 0
                
                if (isHistoryBased) {
                    // Logica PAC/ETF/CC: Condividono lo stesso ID
                    if (existingAsset != null) {
                        if (existingAsset.codiceVincolo > 0) {
                            codiceDaAssegnare = existingAsset.codiceVincolo
                        } else {
                            val newMax = viewModel.getMaxCodiceVincolo() + 1
                            codiceDaAssegnare = newMax
                            viewModel.updateCodiceVincoloPerAsset(selectedAccountId, nomeAsset, newMax)
                        }
                    } else {
                        codiceDaAssegnare = viewModel.getMaxCodiceVincolo() + 1
                    }
                    
                    // In caso di MODIFICA (non duplicazione), manteniamo l'ID corrente
                    if (vincoloId != -1L && !isDuplicate && currentVincolo != null && currentVincolo!!.codiceVincolo > 0) {
                        codiceDaAssegnare = currentVincolo!!.codiceVincolo
                    }
                } else {
                    // Logica Standalone (BTP, Depositi): Ogni nuovo record/duplicazione = Nuovo ID
                    if (vincoloId != -1L && !isDuplicate) {
                        // È una semplice modifica, manteniamo l'ID
                        codiceDaAssegnare = currentVincolo?.codiceVincolo ?: (viewModel.getMaxCodiceVincolo() + 1)
                    } else {
                        // È un nuovo inserimento o una duplicazione di uno strumento standalone
                        codiceDaAssegnare = viewModel.getMaxCodiceVincolo() + 1
                    }
                }

                var finalImporto = importo
                val isIncremental = InstrumentUtils.isIncremental(tipo, detail)
                if (isIncremental && (vincoloId == -1L || isDuplicate)) {
                    val lastBalance = existingAsset?.importo ?: 0.0
                    finalImporto = lastBalance + importo
                }

                // Logica Ereditarietà Tasso: se vuoto, prendi l'ultimo (solo per nuovi record o duplicazioni)
                val inputTassoStr = binding.editTassoVincolo.text.toString().trim().replace(',', '.')
                var finalTasso: Double
                
                if (inputTassoStr.isEmpty() && (vincoloId == -1L || isDuplicate) && lastEntryAnyName != null) {
                    finalTasso = lastEntryAnyName.tassoVincolo
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Tasso ereditato: $finalTasso%", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    finalTasso = inputTassoStr.toDoubleOrNull() ?: 0.0
                }

                // Logica Ereditarietà Periodicità Interessi (per CC e CD Libero)
                val inputPeriodStr = binding.editPeriodoCedola.text.toString().trim()
                var finalPeriodo: Int
                
                if (inputPeriodStr.isEmpty() && (vincoloId == -1L || isDuplicate) && lastEntryAnyName != null && 
                    (tipo == "Conto Corrente" || tipo == "Conto Deposito Libero")) {
                    finalPeriodo = lastEntryAnyName.periodoCedolaMesi
                } else {
                    finalPeriodo = inputPeriodStr.toIntOrNull() ?: 0
                }

                val vincolo = Vincolo(
                    id = if (vincoloId != -1L && !isDuplicate) vincoloId else 0,
                    accountId = selectedAccountId,
                    nome = if (tipo == "Conto Corrente" || tipo == "Conto Deposito Libero" || tipo == "Fondo Pensione" || 
                               tipo == "Immobili" || tipo == "Contanti" || tipo == "Veicoli" || 
                               tipo == "Gioielli" || tipo == "Oggetti di valore") "Saldo" else nomeAsset,
                    dataDecorrenza = calendar.timeInMillis,
                    durataMesi = binding.editDurata.text.toString().toIntOrNull() ?: 0,
                    svincolabile = binding.spinnerSvincolabile.text.toString() == "Sì",
                    importo = finalImporto,
                    tassoVincolo = finalTasso,
                    tassoSvincolo = binding.editTassoSvincolo.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0,
                    periodoCedolaMesi = finalPeriodo,
                    tassazione = tassazione,
                    bolloCaricoBanca = false, // Ora gestito a livello di Account
                    tipo = tipo,
                    note = binding.editNote.text.toString(),
                    codiceVincolo = codiceDaAssegnare,
                    strumentoDettaglio = if (tipo == "Conto Titoli") detail else null,
                    numeroQuote = nQuote,
                    prezzoAcquisto = prezzo,
                    quotaVariazione = if (isIncremental) importo else 0.0
                )

                if (vincolo.id == 0L) {
                    viewModel.insert(vincolo)
                } else {
                    viewModel.update(vincolo)
                }

                // AUTO-SANIFICAZIONE: Ricalcoliamo tutti i saldi progressivi per questo asset (solo se incrementale)
                if (isIncremental) {
                    val allAssetMovements = viewModel.allVincoli.value?.filter { 
                        it.accountId == selectedAccountId && it.nome == nomeAsset && !it.isDeleted 
                    }?.sortedBy { it.dataDecorrenza } ?: emptyList()
                    
                    var runningSum = 0.0
                    for (m in allAssetMovements) {
                        runningSum += m.quotaVariazione
                        if (m.importo != runningSum) {
                            viewModel.update(m.copy(importo = runningSum))
                        }
                    }
                }
                
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Errore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        _binding = null
    }
}
