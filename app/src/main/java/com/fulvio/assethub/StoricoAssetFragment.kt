package com.fulvio.assethub

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fulvio.assethub.databinding.FragmentStoricoSaldiBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class StoricoAssetFragment : Fragment() {

    private var _binding: FragmentStoricoSaldiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    
    private var accountId: Long = -1L
    private var assetName: String? = null
    private var customColor: Int = -1
    private var isHeaderExpanded = false
    private var isGraficoExpanded = false
    private var isMovimentiExpanded = false
    
    private var headerUpdateJob: kotlinx.coroutines.Job? = null
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoricoSaldiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accountId = arguments?.getLong("accountId") ?: -1L
        assetName = arguments?.getString("assetName")
        customColor = arguments?.getInt("customColor", -1) ?: -1

        // Sincronizzazione stato iniziale (TUTTO CHIUSO)
        isHeaderExpanded = false
        isGraficoExpanded = false
        isMovimentiExpanded = false

        binding.layoutHeaderContent.visibility = View.GONE
        binding.contentGrafico.visibility = View.GONE
        binding.recyclerViewStorico.visibility = View.GONE
        
        binding.imgArrowHeader.rotation = 0f
        binding.imgArrowGrafico.rotation = 0f
        binding.imgArrowMovimenti.rotation = 0f
        
        // Forza layout compatto per card movimenti
        val params = binding.cardMovimenti.layoutParams as android.widget.LinearLayout.LayoutParams
        params.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        params.weight = 0f
        binding.cardMovimenti.layoutParams = params

        val adapter = SaldoStoricoAdapter(
            onDeleteClick = { item ->
                if (item.isDeleted) {
                    viewModel.restore(item)
                    Toast.makeText(requireContext(), "Ripristinato", Toast.LENGTH_SHORT).show()
                } else {
                    val dateStr = dateFormatter.format(Date(item.dataDecorrenza))
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Elimina")
                        .setMessage("Sei sicuro di voler eliminare la registrazione del $dateStr? Verrà nascosta dallo storico.")
                        .setPositiveButton("ELIMINA") { _, _ ->
                            viewModel.delete(item)
                        }
                        .setNegativeButton("Annulla", null)
                        .show()
                }
            },
            onPermanentDeleteClick = { item ->
                val dateStr = dateFormatter.format(Date(item.dataDecorrenza))
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminazione Definitiva")
                    .setMessage("ATTENZIONE: Questa operazione eliminerà definitivamente il record del $dateStr dal database. Non sarà possibile recuperarlo. Vuoi procedere?")
                    .setPositiveButton("ELIMINA DEFINITIVAMENTE") { _, _ ->
                        viewModel.deletePhysical(item)
                        Toast.makeText(requireContext(), "Eliminato definitivamente", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            },
            onDuplicateClick = { item ->
                val bundle = Bundle().apply {
                    putLong("vincoloId", item.id)
                    putBoolean("isDuplicate", true)
                    putLong("accountId", item.accountId)
                    putString("assetName", assetName)
                    if (customColor != -1) putInt("customColor", customColor)
                }
                findNavController().navigate(R.id.action_storico_to_add_vincolo, bundle)
            },
            onEditClick = { item ->
                val bundle = Bundle().apply {
                    putLong("vincoloId", item.id)
                    putBoolean("isDuplicate", false)
                    putLong("accountId", item.accountId)
                    putString("assetName", assetName)
                    if (customColor != -1) putInt("customColor", customColor)
                }
                findNavController().navigate(R.id.action_storico_to_add_vincolo, bundle)
            }
        )

        binding.recyclerViewStorico.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewStorico.adapter = adapter

        setupHeader()

        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)

        viewModel.allVincoli.observe(viewLifecycleOwner) { allVincoli ->
            val showDeleted = prefs.getBoolean("show_deleted", false)
            val filtered = allVincoli.filter { 
                it.accountId == accountId && 
                (assetName == null || it.nome == assetName) &&
                (showDeleted || !it.isDeleted)
            }.sortedBy { it.dataDecorrenza } // Ordiniamo per calcolare il progressivo

            // Ricalcoliamo il progressivo in memoria per la visualizzazione
            var runningTotal = 0.0
            val sanifiedList = filtered.map { vincolo ->
                if (InstrumentUtils.isIncremental(vincolo.tipo, vincolo.strumentoDettaglio)) {
                    runningTotal += vincolo.quotaVariazione
                    vincolo.copy(importo = runningTotal)
                } else {
                    vincolo
                }
            }.sortedByDescending { it.dataDecorrenza } // Rimettiamo in ordine decrescente per la lista
            
            adapter.submitList(sanifiedList)
            updateHeaderData(filtered)
            
            if (filtered.isNotEmpty() && InstrumentUtils.isHistoryBased(filtered.first())) {
                binding.cardGraficoAndamento.visibility = View.VISIBLE
                setupTrendChart(filtered)
            } else {
                binding.cardGraficoAndamento.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val account = viewModel.getAccountById(accountId)
            account?.let { acc ->
                val title = if (assetName != null) assetName else "Saldo: ${acc.name}"
                (activity as? AppCompatActivity)?.supportActionBar?.title = title
            }
        }

        setupMenu()
    }

    private fun setupHeader() {
        binding.headerTitleSection.setOnClickListener {
            isHeaderExpanded = !isHeaderExpanded
            binding.layoutHeaderContent.visibility = if (isHeaderExpanded) View.VISIBLE else View.GONE
            binding.imgArrowHeader.animate().rotation(if (isHeaderExpanded) 180f else 0f).setDuration(200).start()
        }

        binding.headerGrafico.setOnClickListener {
            isGraficoExpanded = !isGraficoExpanded
            binding.contentGrafico.visibility = if (isGraficoExpanded) View.VISIBLE else View.GONE
            binding.imgArrowGrafico.animate().rotation(if (isGraficoExpanded) 180f else 0f).setDuration(200).start()
            if (isGraficoExpanded) {
                binding.lineChartTrend.animateX(800)
            }
        }

        binding.headerMovimenti.setOnClickListener {
            isMovimentiExpanded = !isMovimentiExpanded
            binding.recyclerViewStorico.visibility = if (isMovimentiExpanded) View.VISIBLE else View.GONE
            binding.imgArrowMovimenti.animate().rotation(if (isMovimentiExpanded) 180f else 0f).setDuration(200).start()
            
            val params = binding.cardMovimenti.layoutParams as android.widget.LinearLayout.LayoutParams
            params.height = if (isMovimentiExpanded) 0 else android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            params.weight = if (isMovimentiExpanded) 1f else 0f
            binding.cardMovimenti.layoutParams = params
        }
        
        // Colore dinamico per il titolo della card se disponibile
        if (customColor != -1) {
            // Potremmo applicarlo a qualche testo o bordo, ma per ora lo lasciamo neutro
        }
    }

    private fun updateHeaderData(vincoli: List<Vincolo>) {
        headerUpdateJob?.cancel() // Cancella eventuali aggiornamenti precedenti in corso

        if (vincoli.isEmpty()) {
            binding.cardHeaderDettaglio.visibility = View.GONE
            return
        }

        val firstActive = vincoli.find { !it.isDeleted } ?: return
        val isPAC = firstActive.tipo == "Conto Titoli" && firstActive.strumentoDettaglio in InstrumentUtils.historyBasedDettagli
        val isOtherHistory = InstrumentUtils.isHistoryBased(firstActive) && !isPAC

        if (!isPAC && !isOtherHistory) {
            binding.cardHeaderDettaglio.visibility = View.GONE
            return
        }

        binding.cardHeaderDettaglio.visibility = View.VISIBLE
        
        // Sincronizzazione UI con lo stato delle variabili
        binding.layoutHeaderContent.visibility = if (isHeaderExpanded) View.VISIBLE else View.GONE
        binding.imgArrowHeader.rotation = if (isHeaderExpanded) 180f else 0f
        
        binding.contentGrafico.visibility = if (isGraficoExpanded) View.VISIBLE else View.GONE
        binding.imgArrowGrafico.rotation = if (isGraficoExpanded) 180f else 0f
        
        binding.recyclerViewStorico.visibility = if (isMovimentiExpanded) View.VISIBLE else View.GONE
        binding.imgArrowMovimenti.rotation = if (isMovimentiExpanded) 180f else 0f
        
        // Forza layout compatto per card movimenti se chiuso
        val params = binding.cardMovimenti.layoutParams as android.widget.LinearLayout.LayoutParams
        params.height = if (isMovimentiExpanded) 0 else android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        params.weight = if (isMovimentiExpanded) 1f else 0f
        binding.cardMovimenti.layoutParams = params

        headerUpdateJob = viewLifecycleOwner.lifecycleScope.launch {
            val account = viewModel.getAccountById(firstActive.accountId)
            val bank = account?.let { viewModel.getBankById(it.bankId) }
            
            withContext(Dispatchers.Main) {
                // Pulizia atomica nel thread principale prima del popolamento per evitare duplicati
                binding.containerHeaderInfo.removeAllViews()

                if (firstActive.tipo == "Conto Corrente" || firstActive.tipo == "Conto Deposito Libero") {
                    val latest = vincoli.maxByOrNull { it.dataDecorrenza }
                    val saldoAttuale = latest?.importo ?: 0.0
                    var periodicity = latest?.periodoCedolaMesi ?: 12
                    if (periodicity <= 0) periodicity = 12 // Default a 12 se non impostato o errato
                    
                    addInfoRow("Saldo Attuale", currencyFormatter.format(saldoAttuale), 0xFF4CAF50.toInt(), true, 18f)
                    
                    // Calcolo Interessi Maturati basato sulla periodicità
                    val nowTs = System.currentTimeMillis()
                    val startDate = InterestUtils.getStartOfCalculationDate(periodicity)
                    
                    val interessiAnnuati = InterestUtils.calculateCumulativeInterests(vincoli, startDate, nowTs)
                    addInfoRow("Interessi Lordi fino ad oggi", currencyFormatter.format(interessiAnnuati.first))
                    addInfoRow("Interessi Netti fino ad oggi", currencyFormatter.format(interessiAnnuati.second))

                    addInfoRow("Banca", bank?.name ?: "-")
                    addInfoRow("Nome Conto", account?.name ?: "-")
                    addInfoRow("Tipo Conto", firstActive.tipo)
                    val sDetail = firstActive.strumentoDettaglio
                    if (!sDetail.isNullOrBlank()) {
                        addInfoRow("Tipo Strumento", sDetail.replace("Titoli di Stato", "TdS"))
                    }

                    val paymentPeriod = when (periodicity) {
                        1 -> "Mensile"
                        3 -> "Trimestrale"
                        6 -> "Semestrale"
                        12 -> "Annuale"
                        else -> "$periodicity mesi"
                    }
                    addInfoRow("Periodo Pagamento Interessi", paymentPeriod)
                    
                    val tassoLordo = latest?.tassoVincolo ?: 0.0
                    val tassoNetto = tassoLordo * 0.74
                    addInfoRow("Tasso Lordo", "$tassoLordo%")
                    addInfoRow("Tasso Netto (26%)", "${String.format(Locale.ITALY, "%.2f", tassoNetto)}%")
                } else if (isPAC) {
                    val totalQuotes = InstrumentUtils.calculateTotalQuotes(vincoli)
                    val pmc = InstrumentUtils.calculatePMC(vincoli)
                    val lastPrice = vincoli.maxByOrNull { it.dataDecorrenza }?.prezzoAcquisto ?: 0.0
                    val valuation = totalQuotes * lastPrice
                    
                    addInfoRow("Valore Attuale", currencyFormatter.format(valuation), 0xFF4CAF50.toInt(), true, 18f)
                    addInfoRow("Banca", bank?.name ?: "-")
                    addInfoRow("Nome Conto", account?.name ?: "-")
                    addInfoRow("Tipo Conto", firstActive.tipo)
                    val sDetail = firstActive.strumentoDettaglio
                    if (!sDetail.isNullOrBlank()) {
                        addInfoRow("Tipo Strumento", sDetail.replace("Titoli di Stato", "TdS"))
                    }
                    addInfoRow("Quote", String.format(Locale.ITALY, "%.2f", totalQuotes))
                    addInfoRow("PMC", currencyFormatter.format(pmc))
                    addInfoRow("Ultima Quota", currencyFormatter.format(lastPrice))
                } else {
                    // Altri (Fondi, Immobili)
                    val latest = vincoli.maxByOrNull { it.dataDecorrenza }
                    val saldoAttuale = latest?.importo ?: 0.0
                    addInfoRow("Saldo Attuale", currencyFormatter.format(saldoAttuale), 0xFF4CAF50.toInt(), true, 18f)
                    addInfoRow("Banca", bank?.name ?: "-")
                    addInfoRow("Nome Conto", account?.name ?: "-")
                    addInfoRow("Tipo Conto", firstActive.tipo)
                    val sDetail = firstActive.strumentoDettaglio
                    if (!sDetail.isNullOrBlank()) {
                        addInfoRow("Tipo Strumento", sDetail.replace("Titoli di Stato", "TdS"))
                    }

                    if (!firstActive.note.isNullOrBlank()) {
                        addInfoRow("Note", firstActive.note)
                    }
                }
            }
        }
    }

    private fun addInfoRow(label: String, value: String, color: Int = 0xFFFFFFFF.toInt(), bold: Boolean = false, size: Float = 15f) {
        val textView = TextView(requireContext()).apply {
            val textContent = "$label: $value"
            text = textContent
            setTextColor(color)
            textSize = size
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 4, 0, 4)
        }
        binding.containerHeaderInfo.addView(textView)
    }

    private fun setupTrendChart(vincoli: List<Vincolo>) {
        val chart = binding.lineChartTrend
        val first = vincoli.firstOrNull() ?: return
        val isQuoteType = InstrumentUtils.isIncremental(first.tipo, first.strumentoDettaglio)
        
        binding.textHeaderGrafico.text = if (isQuoteType) "Andamento valore quota" else "Andamento valore"

        val points = TrendUtils.getTrendPoints(vincoli, 10)
        if (points.size < 1) {
            binding.cardGraficoAndamento.visibility = View.GONE
            return
        }
        
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("dd/MM", Locale.ITALY)

        points.forEachIndexed { index, ts ->
            val value = TrendUtils.calculateValueAtTimestamp(vincoli, ts)
            entries.add(Entry(index.toFloat(), value.toFloat()))
            labels.add(dateFormat.format(Date(ts)))
        }

        val dataSet = LineDataSet(entries, "Andamento Valore")
        dataSet.apply {
            color = 0xFF448AFF.toInt()
            setCircleColor(0xFF448AFF.toInt())
            lineWidth = 2f
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = 0xFF448AFF.toInt()
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(true)
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = 0xFFBBBBBB.toInt()
                setDrawGridLines(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            axisLeft.apply {
                textColor = 0xFFBBBBBB.toInt()
                setDrawGridLines(true)
                gridColor = 0xFF333333.toInt()
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (isQuoteType) {
                            String.format(Locale.ITALY, "%.3f €", value)
                        } else {
                            currencyFormatter.format(value)
                        }
                    }
                }
            }
            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
                menu.findItem(R.id.action_info)?.isVisible = false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        val bundle = Bundle().apply {
                            putLong("accountId", accountId)
                            putString("assetName", assetName)
                            if (customColor != -1) putInt("customColor", customColor)
                        }
                        findNavController().navigate(R.id.action_storico_to_add_vincolo, bundle)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
