package com.fulvio.assethub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fulvio.assethub.databinding.FragmentCalcolatriceInteressiBinding
import java.text.NumberFormat
import java.util.*

class CalcolatriceInteressiFragment : Fragment() {

    private var _binding: FragmentCalcolatriceInteressiBinding? = null
    private val binding get() = _binding!!
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalcolatriceInteressiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnCalcola.setOnClickListener {
            if (validaCampi()) {
                calcola()
                nascondiTastiera()
            }
        }
    }

    private fun validaCampi(): Boolean {
        var isValido = true
        
        if (binding.editCapitale.text.toString().isBlank()) {
            binding.layoutCapitale.error = "Inserisci il capitale"
            isValido = false
        } else {
            binding.layoutCapitale.error = null
        }
        
        if (binding.editTasso.text.toString().isBlank()) {
            binding.layoutTasso.error = "Inserisci il tasso"
            isValido = false
        } else {
            binding.layoutTasso.error = null
        }
        
        if (binding.editDurata.text.toString().isBlank()) {
            binding.layoutDurata.error = "Inserisci la durata"
            isValido = false
        } else {
            binding.layoutDurata.error = null
        }
        
        return isValido
    }

    private fun nascondiTastiera() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun calcola() {
        val capitale = binding.editCapitale.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val tassoAnnuo = binding.editTasso.text.toString().replace(',', '.').toDoubleOrNull() ?: 0.0
        val durataMesi = binding.editDurata.text.toString().toIntOrNull() ?: 0
        
        val tassazione = if (binding.radio26.isChecked) 0.26 else 0.125
        
        // Calcolo interesse lordo (base mensile 30/360 per semplicità di utility veloce)
        val lordo = (capitale * (tassoAnnuo / 100.0) * durataMesi) / 12.0
        val tasse = lordo * tassazione
        val netto = lordo - tasse
        val tassoNettoPercent = tassoAnnuo * (1.0 - tassazione)

        binding.textRisultatoLordo.text = currencyFormatter.format(lordo)
        binding.textRisultatoTasse.text = "- ${currencyFormatter.format(tasse)}"
        binding.textRisultatoNetto.text = currencyFormatter.format(netto)
        binding.textRisultatoTassoNetto.text = String.format(Locale.ITALY, "%.2f%%", tassoNettoPercent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}