package com.fulvio.assethub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fulvio.assethub.databinding.FragmentSaltoStaffaBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class SaltoStaffaFragment : Fragment() {

    private var _binding: FragmentSaltoStaffaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    private var selectedItem: VincoloWithAccount? = null

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSaltoStaffaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCercaVincolo.setOnClickListener {
            cercaVincolo()
        }

        binding.btnInfoId.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Trova ID")
                .setMessage("Puoi trovare il numero ID dello strumento nella pagina \u0027Lista Strumenti\u0027, visualizzato nell\u0027angolo in basso a sinistra di ogni scheda (es. 01, 02...).")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnInfoScenario.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Scenario post-salto")
                .setMessage("• Inserisci il nuovo tasso lordo che prevedi di ottenere reinvestendo la somma nel nuovo anno.\n\n" +
                        "• Inserisci quanti giorni intercorrono tra lo svincolo e l\u0027attivazione del nuovo vincolo, considerando festività e tempi tecnici dei bonifici. In questo periodo la somma non produrrà interessi.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.btnCalcolaStaffa.setOnClickListener {
            if (selectedItem != null) {
                calcolaConvenienza()
                nascondiTastiera()
            } else {
                Toast.makeText(requireContext(), "Cerca prima un vincolo tramite ID", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun cercaVincolo() {
        val codiceStr = binding.editCodiceRicerca.text.toString()
        val codice = codiceStr.toIntOrNull()
        
        if (codice == null) {
            binding.layoutCodiceRicerca.error = "ID non valido"
            return
        }
        binding.layoutCodiceRicerca.error = null

        viewLifecycleOwner.lifecycleScope.launch {
            val item = viewModel.getByCodice(codice)
            if (item != null) {
                selectedItem = item
                binding.layoutDatiVincolo.visibility = View.VISIBLE
                binding.textInfoVincolo.text = "Banca: ${item.account.name}\n" +
                        "Importo: ${currencyFormatter.format(item.vincolo.importo)}\n" +
                        "Tasso Vincolo: ${item.vincolo.tassoVincolo}%\n" +
                        "Tasso Svincolo: ${item.vincolo.tassoSvincolo}%"
                
                // Pre-popola il nuovo tasso col tasso attuale come suggerimento
                binding.editNuovoTasso.setText(item.vincolo.tassoVincolo.toString())
            } else {
                selectedItem = null
                binding.layoutDatiVincolo.visibility = View.GONE
                Toast.makeText(requireContext(), "Vincolo non trovato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calcolaConvenienza() {
        val item = selectedItem ?: return
        val vincolo = item.vincolo
        
        val nuovoTasso = binding.editNuovoTasso.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val giorniPersi = binding.editGiorniPersi.text.toString().toIntOrNull() ?: 0

        val fineAnno = Calendar.getInstance().apply { set(2026, Calendar.DECEMBER, 31, 23, 59, 59) }
        val ultimaCedola = getUltimaCedola(vincolo, fineAnno)
        val prossimaCedola = getProssimaCedola(vincolo, fineAnno)
        
        val df = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
        val rangeStr = "${df.format(ultimaCedola.time)} - ${df.format(prossimaCedola.time)}"

        // Giorni totali del periodo a cavallo del 31/12
        val ggTotali = ((prossimaCedola.timeInMillis - ultimaCedola.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        // Giorni dal pagamento ultima cedola al 31/12
        val ggPreSalto = ((fineAnno.timeInMillis - ultimaCedola.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        // --- 1. Scenario RESTA (Senza Salto) ---
        val intLordoResta = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * ggTotali) / 365.0
        val intNettoResta = intLordoResta * (1.0 - vincolo.tassazione)
        val bollo = vincolo.importo * 0.002
        val totaleResta = intNettoResta - bollo

        // --- 2. Scenario SALTO (Con Salto) ---
        // A. Interessi svincolo fino al 31/12
        val intLordoStaffaA = (vincolo.importo * (vincolo.tassoSvincolo / 100.0) * ggPreSalto) / 365.0
        val intNettoStaffaA = intLordoStaffaA * (1.0 - vincolo.tassazione)
        
        // B. Interessi dopo il salto dal rientro alla prossima cedola
        val dataRientro = Calendar.getInstance().apply { 
            set(2027, Calendar.JANUARY, 1, 0, 0, 0)
            add(Calendar.DAY_OF_YEAR, giorniPersi)
        }
        val ggPostStaffa = ((prossimaCedola.timeInMillis - dataRientro.timeInMillis) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        val intLordoStaffaB = (vincolo.importo * (nuovoTasso / 100.0) * ggPostStaffa) / 365.0
        val intNettoStaffaB = intLordoStaffaB * (1.0 - vincolo.tassazione)
        
        val totaleStaffa = intNettoStaffaA + intNettoStaffaB

        // --- RISULTATO UI ---
        binding.cardRisultatiStaffa.visibility = View.VISIBLE
        
        binding.textSez1Dettaglio.text = "A. Interessi netti ($rangeStr): ${currencyFormatter.format(intNettoResta)}\n" +
                "B. Imposta di bollo (al 31/12): ${currencyFormatter.format(bollo)}"
        binding.textSez1Totale.text = "TOTALE (A-B): ${currencyFormatter.format(totaleResta)}"

        binding.textSez2Dettaglio.text = "A. Interessi svincolo (fino al 31/12): ${currencyFormatter.format(intNettoStaffaA)}\n" +
                "B. Interessi post-salto (rientro ${df.format(dataRientro.time)}): ${currencyFormatter.format(intNettoStaffaB)}"
        binding.textSez2Totale.text = "TOTALE (A+B): ${currencyFormatter.format(totaleStaffa)}"

        val diff = totaleStaffa - totaleResta
        if (diff > 0) {
            binding.textVerdetto.text = "CONVIENE IL SALTO"
            binding.textVerdetto.setTextColor(0xFF4CAF50.toInt())
            binding.textDifferenza.text = "Guadagno extra: ${currencyFormatter.format(diff)}"
        } else {
            binding.textVerdetto.text = "NON CONVIENE IL SALTO"
            binding.textVerdetto.setTextColor(0xFFF44336.toInt())
            binding.textDifferenza.text = "Perdita extra: ${currencyFormatter.format(Math.abs(diff))}"
        }
    }

    private fun getUltimaCedola(vincolo: Vincolo, fineAnno: Calendar): Calendar {
        val calInizio = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
        if (vincolo.periodoCedolaMesi <= 0) return calInizio
        
        val current = Calendar.getInstance().apply { timeInMillis = calInizio.timeInMillis }
        val lastValid = Calendar.getInstance().apply { timeInMillis = calInizio.timeInMillis }
        
        while (current.before(fineAnno)) {
            lastValid.timeInMillis = current.timeInMillis
            current.add(Calendar.MONTH, vincolo.periodoCedolaMesi)
        }
        return lastValid
    }

    private fun getProssimaCedola(vincolo: Vincolo, fineAnno: Calendar): Calendar {
        val calInizio = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
        
        // Se cedola alla scadenza
        if (vincolo.periodoCedolaMesi <= 0) {
            return Calendar.getInstance().apply {
                timeInMillis = vincolo.dataDecorrenza
                add(Calendar.MONTH, vincolo.durataMesi)
            }
        }
        
        val current = Calendar.getInstance().apply { timeInMillis = calInizio.timeInMillis }
        while (!current.after(fineAnno)) {
            current.add(Calendar.MONTH, vincolo.periodoCedolaMesi)
        }
        return current
    }

    private fun nascondiTastiera() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
