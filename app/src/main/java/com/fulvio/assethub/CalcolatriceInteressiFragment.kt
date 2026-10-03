package com.fulvio.assethub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

        val taxOptions = arrayOf("26,00%", "12,50%")
        binding.spinnerTassazione.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, taxOptions))
        binding.spinnerTassazione.setText(taxOptions[0], false)

        binding.btnCalcola.setOnClickListener {
            calcolaInteressi()
        }
    }

    private fun calcolaInteressi() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)

        val capitaleStr = binding.editCapitale.text.toString().replace(',', '.')
        val tassoStr = binding.editTasso.text.toString().replace(',', '.')
        val durataStr = binding.editDurata.text.toString()
        val taxSelection = binding.spinnerTassazione.text.toString()

        val capitale = capitaleStr.toDoubleOrNull() ?: 0.0
        val tassoLordoAnno = tassoStr.toDoubleOrNull() ?: 0.0
        val mesi = durataStr.toIntOrNull() ?: 0

        if (capitale <= 0 || tassoLordoAnno < 0 || mesi <= 0) {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Dati non validi")
                .setMessage("Inserisci valori validi per Capitale, Tasso e Durata.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val tassazione = if (taxSelection.contains("12,50")) 0.125 else 0.26

        // Calcolo interessi lordi basato su base annua (mesi / 12)
        val lordo = (capitale * (tassoLordoAnno / 100.0) * mesi) / 12.0
        val imposte = lordo * tassazione
        val netto = lordo - imposte
        val capitaleFinale = capitale + netto
        val tassoNettoAnno = tassoLordoAnno * (1.0 - tassazione)

        binding.textLordo.text = currencyFormatter.format(lordo)
        binding.textTassazioneImporto.text = "- ${currencyFormatter.format(imposte)}"
        binding.textNetto.text = currencyFormatter.format(netto)
        binding.textCapitaleFinale.text = currencyFormatter.format(capitaleFinale)
        binding.textTassoNetto.text = String.format(Locale.ITALY, "%.2f%%", tassoNettoAnno)

        binding.cardRisultati.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
