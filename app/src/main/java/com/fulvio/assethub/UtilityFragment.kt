package com.fulvio.assethub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.fulvio.assethub.databinding.FragmentUtilityBinding

class UtilityFragment : Fragment() {

    private var _binding: FragmentUtilityBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUtilityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardCalcolatrice.setOnClickListener {
            findNavController().navigate(R.id.action_utility_to_calcolatrice)
        }

        binding.cardSaltoStaffa.setOnClickListener {
            findNavController().navigate(R.id.action_utility_to_salto_staffa)
        }

        binding.cardLinksUtili.setOnClickListener {
            findNavController().navigate(R.id.action_utility_to_links_utili)
        }

        binding.btnInfoStaffa.setOnClickListener {
            mostraInfoSalto()
        }
    }

    private fun mostraInfoSalto() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Il Salto della Staffa")
            .setMessage("Il \"Salto della Staffa\" (chiamato anche amichevolmente \"Salto della Quaglia\") in ambito finanziario è una mossa pratica usata per spostare i propri risparmi da un conto a un altro, evitare o ridurre il pagamento dell'imposta di bollo e far rientrare i fondi dopo pochi giorni.\n\n" +
                    "Come funziona sui conti deposito:\n" +
                    "• Consiste nel bonificare i soldi dal conto deposito al conto corrente ordinario nei giorni in cui viene scattato il rendiconto (ad esempio il 31 dicembre o alla fine del trimestre).\n" +
                    "• Serve a svuotare il conto deposito alla data stabilita per azzerare o minimizzare il saldo tassabile.\n" +
                    "• Si riportano poi i soldi sul conto deposito subito dopo la data del rendiconto.\n\n" +
                    "Questa utility ti aiuta a capire se il risparmio del bollo è superiore alla perdita di interessi causata dallo svincolo anticipato.")
            .setPositiveButton("Capito", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}