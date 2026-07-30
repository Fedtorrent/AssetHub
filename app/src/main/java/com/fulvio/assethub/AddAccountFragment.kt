package com.fulvio.assethub

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentAddAccountBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    
    private var categories: List<Category> = emptyList()
    private var selectedCategoryId: Long = -1L
    private var bankId: Long = -1L
    private var accountId: Long = -1L
    
    private var banks: List<Bank> = emptyList()
    private var selectedBankId: Long = -1L
    private var currentAccount: Account? = null
    private var isAssetPersonaliView: Boolean = false

    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bankId = arguments?.getLong("bankId") ?: -1L
        accountId = arguments?.getLong("accountId") ?: -1L
        selectedBankId = bankId

        setupDatePicker()
        setupRendicontazioneSpinner()
        setupBolloSpinner()

        if (accountId != -1L) {
            caricaDatiAccount(accountId)
        } else {
            setupNuovoAccount()
            binding.editDateAccount.setText(dateFormatter.format(calendar.time))
        }

        viewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            val ctx = context ?: return@observe
            categories = cats
            val names = cats.map { it.name }
            val adapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, names)
            binding.spinnerCategory.setAdapter(adapter)
            
            // Se siamo in Asset Personali, pre-selezioniamo Immobili e filtriamo la lista
            if (isAssetPersonaliView) {
                val physicalCats = cats.filter { 
                    it.systemType == Category.TYPE_IMMOBILI || 
                    it.systemType == Category.TYPE_CONTANTI || 
                    it.systemType == Category.TYPE_VEICOLI || 
                    it.systemType == Category.TYPE_GIOIELLI || 
                    it.systemType == Category.TYPE_OGGETTI
                }
                val physicalNames = physicalCats.map { it.name }
                binding.spinnerCategory.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, physicalNames))
                
                cats.find { it.systemType == Category.TYPE_IMMOBILI }?.let {
                    if (selectedCategoryId == -1L) {
                        selectedCategoryId = it.id
                        binding.spinnerCategory.setText(it.name, false)
                        aggiornaVisibilitaCampi(it.systemType)
                    }
                }
            } else {
                val financialCats = cats.filter { 
                    it.systemType != Category.TYPE_IMMOBILI &&
                    it.systemType != Category.TYPE_CONTANTI && 
                    it.systemType != Category.TYPE_VEICOLI && 
                    it.systemType != Category.TYPE_GIOIELLI && 
                    it.systemType != Category.TYPE_OGGETTI 
                }
                val financialNames = financialCats.map { it.name }
                binding.spinnerCategory.setAdapter(ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, financialNames))
            }

            currentAccount?.let { acc ->
                val cat = cats.find { it.id == acc.categoryId }
                cat?.let {
                    binding.spinnerCategory.setText(it.name, false)
                    selectedCategoryId = it.id
                    aggiornaVisibilitaCampi(it.systemType)
                }
            }
        }
        
        binding.spinnerCategory.setOnClickListener {
            if (!isAssetPersonaliView) binding.spinnerCategory.showDropDown()
        }
        
        binding.spinnerCategory.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !isAssetPersonaliView) binding.spinnerCategory.showDropDown()
        }

        binding.spinnerCategory.setOnItemClickListener { _, _, position, _ ->
            val adapter = binding.spinnerCategory.adapter
            val selectedName = adapter.getItem(position).toString()
            val cat = categories.find { it.name == selectedName }
            cat?.let {
                selectedCategoryId = it.id
                aggiornaVisibilitaCampi(it.systemType)
            }
        }

        binding.btnSaveAccount.setOnClickListener {
            saveAccount()
        }
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            binding.editDateAccount.setText(dateFormatter.format(calendar.time))
        }

        binding.editDateAccount.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupRendicontazioneSpinner() {
        val options = arrayOf("Trimestrale", "Annuale")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        binding.spinnerRendicontazioneAccount.setAdapter(adapter)
        binding.spinnerRendicontazioneAccount.setText(options[0], false)
    }

    private fun setupBolloSpinner() {
        val options = arrayOf("No", "Sì")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        binding.spinnerBolloAccount.setAdapter(adapter)
        binding.spinnerBolloAccount.setText(options[0], false)
    }

    private fun aggiornaVisibilitaCampi(systemType: String) {
        val color = arguments?.getInt("customColor", -1) ?: -1
        if (color != -1) {
            binding.btnSaveAccount.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
        }

        if (isAssetPersonaliView || systemType == Category.TYPE_IMMOBILI) {
            binding.layoutBankSelector.visibility = View.GONE
            
            if (selectedBankId == -1L || isAssetPersonaliView) {
                viewModel.allBanks.observe(viewLifecycleOwner) { list ->
                    list.find { it.name == "Asset Personali" }?.let {
                        selectedBankId = it.id
                    }
                }
            }
            
            if (isAssetPersonaliView) {
                binding.layoutCategory.visibility = View.VISIBLE
                binding.layoutCategory.hint = "Tipo di Asset"
                binding.layoutRendicontazioneAccount.visibility = View.GONE
                binding.layoutAccountName.hint = "Nome Asset"
                binding.layoutSaldoLibero.hint = "Valore Asset"
                binding.containerDynamicFields.visibility = View.VISIBLE
                binding.btnSaveAccount.text = if (accountId != -1L) "AGGIORNA ASSET" else "SALVA ASSET"
            }
        } else if (bankId == -1L) {
            binding.layoutBankSelector.visibility = View.VISIBLE
        }

        if (!isAssetPersonaliView) {
            binding.layoutCategory.visibility = View.VISIBLE
            binding.layoutCategory.hint = "Tipologia"
            binding.layoutAccountName.hint = "Nome Conto"
            binding.btnSaveAccount.text = if (accountId != -1L) "AGGIORNA CONTO" else "SALVA CONTO"
        }

        // Rendicontazione visibile SOLO per Conto Corrente e CD Libero
        binding.layoutRendicontazioneAccount.visibility = if ((systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO) && !isAssetPersonaliView) View.VISIBLE else View.GONE
        
        // Bollo visibile per CC, Deposito Libero, Deposito e Titoli
        binding.layoutBolloAccount.visibility = if (systemType == Category.TYPE_CORRENTE || 
            systemType == Category.TYPE_DEPOSITO_LIBERO ||
            systemType == Category.TYPE_DEPOSITO || 
            systemType == Category.TYPE_TITOLI) View.VISIBLE else View.GONE

        // Tasso d'interesse visibile SOLO per Conto Corrente / CD Libero e SOLO in creazione
        binding.layoutTassoAccount.visibility = if ((systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO) && accountId == -1L) View.VISIBLE else View.GONE
        
        // Periodicità Interessi visibile SOLO per Conto Corrente / CD Libero e SOLO in creazione
        binding.layoutPeriodoInteressiAccount.visibility = if ((systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO) && accountId == -1L) View.VISIBLE else View.GONE

        // Note visibili per tutti gli Asset Personali
        val isPhysicalAsset = systemType == Category.TYPE_IMMOBILI || systemType == Category.TYPE_CONTANTI || 
                              systemType == Category.TYPE_VEICOLI || systemType == Category.TYPE_GIOIELLI || 
                              systemType == Category.TYPE_OGGETTI
        
        binding.layoutNoteAccount.visibility = if (isPhysicalAsset) View.VISIBLE else View.GONE

        when (systemType) {
            Category.TYPE_CORRENTE, Category.TYPE_DEPOSITO_LIBERO, Category.TYPE_PENSIONE, Category.TYPE_IMMOBILI,
            Category.TYPE_CONTANTI, Category.TYPE_VEICOLI, Category.TYPE_GIOIELLI, Category.TYPE_OGGETTI -> {
                binding.containerDynamicFields.visibility = View.VISIBLE
                if (systemType == Category.TYPE_IMMOBILI || systemType == Category.TYPE_CONTANTI || 
                    systemType == Category.TYPE_VEICOLI || systemType == Category.TYPE_GIOIELLI || 
                    systemType == Category.TYPE_OGGETTI) {
                    binding.layoutSaldoLibero.hint = "Valore stimato"
                } else {
                    binding.layoutSaldoLibero.hint = "Saldo Iniziale"
                }
            }
            else -> {
                binding.containerDynamicFields.visibility = View.GONE
            }
        }
    }

    private fun caricaDatiAccount(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val account = viewModel.getAccountById(id)
                account?.let {
                    currentAccount = it
                    selectedBankId = it.bankId
                    selectedCategoryId = it.categoryId
                    binding.editAccountName.setText(it.name)
                    binding.spinnerRendicontazioneAccount.setText(it.frequenzaRendicontazione, false)
                    binding.spinnerBolloAccount.setText(if (it.bolloCaricoBanca) "Sì" else "No", false)
                    
                    calendar.timeInMillis = it.lastUpdate
                    binding.editDateAccount.setText(dateFormatter.format(Date(it.lastUpdate)))
                    
                    val bank = viewModel.getBankById(it.bankId)
                    isAssetPersonaliView = bank?.name == "Asset Personali"
                    
                    (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = 
                        if (isAssetPersonaliView) "Modifica Asset" else "Modifica Conto: ${bank?.name}"
                    
                    // Recuperiamo la categoria per passare il systemType corretto
                    val cat = categories.find { c -> c.id == it.categoryId }
                    cat?.let { c -> aggiornaVisibilitaCampi(c.systemType) }
                }
            } catch (e: Exception) {}
        }
    }

    private fun setupNuovoAccount() {
        if (bankId == -1L) {
            binding.layoutBankSelector.visibility = View.VISIBLE
            viewModel.allBanks.observe(viewLifecycleOwner) { list ->
                val ctx = context ?: return@observe
                banks = list
                val names = list.map { it.name }
                val adapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, names)
                binding.spinnerBank.setAdapter(adapter)
            }
            binding.spinnerBank.setOnItemClickListener { _, _, position, _ ->
                if (position >= 0 && position < banks.size) {
                    val bank = banks[position]
                    selectedBankId = bank.id
                    isAssetPersonaliView = bank.name == "Asset Personali"
                    
                    if (isAssetPersonaliView) {
                        categories.find { it.systemType == Category.TYPE_IMMOBILI }?.let {
                            selectedCategoryId = it.id
                            binding.spinnerCategory.setText(it.name, false)
                        }
                    }
                    aggiornaVisibilitaCampi(if (isAssetPersonaliView) Category.TYPE_IMMOBILI else "")
                }
            }
        } else {
            binding.layoutBankSelector.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                val bank = viewModel.getBankById(bankId)
                bank?.let {
                    isAssetPersonaliView = it.name == "Asset Personali"
                    (activity as? androidx.appcompat.app.AppCompatActivity)?.supportActionBar?.title = 
                        if (isAssetPersonaliView) "Nuovo Asset" else "Nuovo Conto: ${it.name}"
                    
                    if (isAssetPersonaliView) {
                        categories.find { it.systemType == Category.TYPE_IMMOBILI }?.let { c ->
                            selectedCategoryId = c.id
                        }
                    }
                    aggiornaVisibilitaCampi(if (isAssetPersonaliView) Category.TYPE_IMMOBILI else "")
                }
            }
        }
    }

    private fun saveAccount() {
        val name = binding.editAccountName.text.toString()
        val saldoStr = binding.editSaldoLibero.text.toString()
        
        if (selectedBankId == -1L) {
            binding.layoutBankSelector.error = "Seleziona una banca"
            Toast.makeText(context, "Manca la banca di riferimento", Toast.LENGTH_SHORT).show()
            return
        }
        if (name.isBlank()) {
            binding.layoutAccountName.error = "Obbligatorio"
            return
        }
        if (selectedCategoryId == -1L) {
            binding.layoutCategory.error = "Seleziona una categoria"
            return
        }

        val cat = categories.find { it.id == selectedCategoryId }
        val systemType = cat?.systemType ?: ""
        val saldo = saldoStr.replace(',', '.').toDoubleOrNull() ?: 0.0
        val note = binding.editNoteAccount.text.toString()
        
        // Lettura tasso d'interesse (solo per CC / CD Libero e solo in creazione)
        val tassoStr = binding.editTassoAccount.text.toString()
        val tassoIniziale = if ((systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO) && accountId == -1L) {
            tassoStr.replace(',', '.').toDoubleOrNull() ?: 0.0
        } else 0.0

        // Lettura periodicità interessi (solo per CC / CD Libero e solo in creazione)
        val periodStr = binding.editPeriodoInteressiAccount.text.toString()
        val periodIniziale = if ((systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO) && accountId == -1L) {
            periodStr.toIntOrNull() ?: 12 // Default 12 mesi
        } else 0
        
        val rendicontazione = binding.spinnerRendicontazioneAccount.text.toString()
        val selectedDate = if (binding.containerDynamicFields.visibility == View.VISIBLE) calendar.timeInMillis else System.currentTimeMillis()

        val account = Account(
            id = if (accountId != -1L) accountId else 0L,
            bankId = selectedBankId,
            name = name,
            categoryId = selectedCategoryId,
            lastUpdate = selectedDate,
            isDeleted = currentAccount?.isDeleted ?: false,
            frequenzaRendicontazione = rendicontazione,
            bolloCaricoBanca = binding.spinnerBolloAccount.text.toString() == "Sì"
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                if (accountId != -1L) {
                    viewModel.updateAccount(account)
                } else {
                    val newId = viewModel.insertAccount(account)
                    
                    if (systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO || systemType == Category.TYPE_PENSIONE || systemType == Category.TYPE_IMMOBILI ||
                        systemType == Category.TYPE_CONTANTI || systemType == Category.TYPE_VEICOLI || systemType == Category.TYPE_GIOIELLI || systemType == Category.TYPE_OGGETTI) {
                        val vincolo = Vincolo(
                            accountId = newId,
                            nome = "Saldo Iniziale",
                            dataDecorrenza = selectedDate,
                            durataMesi = 0,
                            svincolabile = true,
                            importo = saldo,
                            tassoVincolo = tassoIniziale,
                            tassoSvincolo = 0.0,
                            periodoCedolaMesi = periodIniziale,
                            tassazione = if (systemType == Category.TYPE_IMMOBILI || systemType == Category.TYPE_CONTANTI || systemType == Category.TYPE_VEICOLI || systemType == Category.TYPE_GIOIELLI || systemType == Category.TYPE_OGGETTI) 0.0 else 0.26,
                            bolloCaricoBanca = false,
                            tipo = when(systemType) {
                                Category.TYPE_CORRENTE -> "Conto Corrente"
                                Category.TYPE_DEPOSITO_LIBERO -> "Conto Deposito Libero"
                                Category.TYPE_PENSIONE -> "Fondo Pensione"
                                Category.TYPE_IMMOBILI -> "Immobili"
                                Category.TYPE_CONTANTI -> "Contanti"
                                Category.TYPE_VEICOLI -> "Veicoli"
                                Category.TYPE_GIOIELLI -> "Gioielli"
                                Category.TYPE_OGGETTI -> "Oggetti di valore"
                                else -> "Conto Corrente"
                            },
                            note = note,
                            frequenzaRendicontazione = rendicontazione
                        )
                        viewModel.insert(vincolo)
                    }
                }
                val msg = if (isAssetPersonaliView) "Asset salvato!" else "Conto salvato!"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Errore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
