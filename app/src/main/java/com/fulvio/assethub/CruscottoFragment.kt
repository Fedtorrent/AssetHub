package com.fulvio.assethub

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.fulvio.assethub.databinding.FragmentCruscottoBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.text.NumberFormat
import java.util.*

class CruscottoFragment : Fragment() {

    private var _binding: FragmentCruscottoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)

    private var isTotaleExpanded = false
    private var isAndamentoExpanded = false
    private var isCedoleExpanded = false
    private var isVincoliScadenzaExpanded = false
    private var isGraficoNomiExpanded = false
    private var isGraficoTipoExpanded = false
    private var isGraficoDurataExpanded = false
    private var isGraficoBancaExpanded = false
    private var isGraficoAssetExpanded = false
    private var isGraficoInvestimentiExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCruscottoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCollapsibleSections()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.dashboard_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val prefs = requireContext().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)
                return when (menuItem.itemId) {
                    R.id.action_expand_all -> {
                        toggleAll(true, prefs)
                        true
                    }
                    R.id.action_collapse_all -> {
                        toggleAll(false, prefs)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.allBanksWithAccounts.observe(viewLifecycleOwner) { items ->
            updateUI(items)
        }
    }

    private fun setupCollapsibleSections() {
        val prefs = requireContext().getSharedPreferences("dashboard_prefs", Context.MODE_PRIVATE)

        isTotaleExpanded = prefs.getBoolean("totale_expanded", false)
        isAndamentoExpanded = prefs.getBoolean("andamento_expanded", false)
        isCedoleExpanded = prefs.getBoolean("cedole_expanded", false)
        isVincoliScadenzaExpanded = prefs.getBoolean("vincoli_scadenza_expanded", false)
        isGraficoNomiExpanded = prefs.getBoolean("grafico_nomi_expanded", false)
        isGraficoTipoExpanded = prefs.getBoolean("grafico_tipo_expanded", false)
        isGraficoDurataExpanded = prefs.getBoolean("grafico_durata_expanded", false)
        isGraficoBancaExpanded = prefs.getBoolean("grafico_banca_expanded", false)
        isGraficoAssetExpanded = prefs.getBoolean("grafico_asset_expanded", false)
        isGraficoInvestimentiExpanded = prefs.getBoolean("grafico_investimenti_expanded", false)

        updateCollapsibleView(binding.contentTotale, binding.imgArrowTotale, isTotaleExpanded)
        updateCollapsibleView(binding.contentAndamento, binding.imgArrowAndamento, isAndamentoExpanded)
        updateCollapsibleView(binding.contentCedoleProssime, binding.imgArrowCedoleProssime, isCedoleExpanded)
        updateCollapsibleView(binding.contentVincoliScadenza, binding.imgArrowVincoliScadenza, isVincoliScadenzaExpanded)
        updateCollapsibleView(binding.contentGraficoNomi, binding.imgArrowGraficoNomi, isGraficoNomiExpanded)
        updateCollapsibleView(binding.contentGraficoTipo, binding.imgArrowGraficoTipo, isGraficoTipoExpanded)
        updateCollapsibleView(binding.contentGraficoDurata, binding.imgArrowGraficoDurata, isGraficoDurataExpanded)
        updateCollapsibleView(binding.contentGraficoBanca, binding.imgArrowGraficoBanca, isGraficoBancaExpanded)
        updateCollapsibleView(binding.contentGraficoAsset, binding.imgArrowGraficoAsset, isGraficoAssetExpanded)
        updateCollapsibleView(binding.contentGraficoInvestimenti, binding.imgArrowGraficoInvestimenti, isGraficoInvestimentiExpanded)

        binding.headerTotale.setOnClickListener {
            isTotaleExpanded = !isTotaleExpanded
            updateCollapsibleView(binding.contentTotale, binding.imgArrowTotale, isTotaleExpanded)
            prefs.edit().putBoolean("totale_expanded", isTotaleExpanded).apply()
        }
        binding.headerAndamento.setOnClickListener {
            isAndamentoExpanded = !isAndamentoExpanded
            updateCollapsibleView(binding.contentAndamento, binding.imgArrowAndamento, isAndamentoExpanded)
            prefs.edit().putBoolean("andamento_expanded", isAndamentoExpanded).apply()
        }
        binding.headerCedoleProssime.setOnClickListener {
            isCedoleExpanded = !isCedoleExpanded
            updateCollapsibleView(binding.contentCedoleProssime, binding.imgArrowCedoleProssime, isCedoleExpanded)
            prefs.edit().putBoolean("cedole_expanded", isCedoleExpanded).apply()
        }
        binding.headerVincoliScadenza.setOnClickListener {
            isVincoliScadenzaExpanded = !isVincoliScadenzaExpanded
            updateCollapsibleView(binding.contentVincoliScadenza, binding.imgArrowVincoliScadenza, isVincoliScadenzaExpanded)
            prefs.edit().putBoolean("vincoli_scadenza_expanded", isVincoliScadenzaExpanded).apply()
        }
        binding.headerGraficoNomi.setOnClickListener {
            isGraficoNomiExpanded = !isGraficoNomiExpanded
            updateCollapsibleView(binding.contentGraficoNomi, binding.imgArrowGraficoNomi, isGraficoNomiExpanded)
            prefs.edit().putBoolean("grafico_nomi_expanded", isGraficoNomiExpanded).apply()
        }
        binding.headerGraficoTipo.setOnClickListener {
            isGraficoTipoExpanded = !isGraficoTipoExpanded
            updateCollapsibleView(binding.contentGraficoTipo, binding.imgArrowGraficoTipo, isGraficoTipoExpanded)
            prefs.edit().putBoolean("grafico_tipo_expanded", isGraficoTipoExpanded).apply()
        }
        binding.headerGraficoDurata.setOnClickListener {
            isGraficoDurataExpanded = !isGraficoDurataExpanded
            updateCollapsibleView(binding.contentGraficoDurata, binding.imgArrowGraficoDurata, isGraficoDurataExpanded)
            prefs.edit().putBoolean("grafico_durata_expanded", isGraficoDurataExpanded).apply()
        }
        binding.headerGraficoBanca.setOnClickListener {
            isGraficoBancaExpanded = !isGraficoBancaExpanded
            updateCollapsibleView(binding.contentGraficoBanca, binding.imgArrowGraficoBanca, isGraficoBancaExpanded)
            prefs.edit().putBoolean("grafico_banca_expanded", isGraficoBancaExpanded).apply()
        }
        binding.headerGraficoAsset.setOnClickListener {
            isGraficoAssetExpanded = !isGraficoAssetExpanded
            updateCollapsibleView(binding.contentGraficoAsset, binding.imgArrowGraficoAsset, isGraficoAssetExpanded)
            prefs.edit().putBoolean("grafico_asset_expanded", isGraficoAssetExpanded).apply()
        }
        binding.headerGraficoInvestimenti.setOnClickListener {
            isGraficoInvestimentiExpanded = !isGraficoInvestimentiExpanded
            updateCollapsibleView(binding.contentGraficoInvestimenti, binding.imgArrowGraficoInvestimenti, isGraficoInvestimentiExpanded)
            prefs.edit().putBoolean("grafico_investimenti_expanded", isGraficoInvestimentiExpanded).apply()
        }
    }

    private fun toggleAll(expand: Boolean, prefs: android.content.SharedPreferences) {
        isTotaleExpanded = expand
        isAndamentoExpanded = expand
        isCedoleExpanded = expand
        isVincoliScadenzaExpanded = expand
        isGraficoNomiExpanded = expand
        isGraficoTipoExpanded = expand
        isGraficoDurataExpanded = expand
        isGraficoBancaExpanded = expand
        isGraficoAssetExpanded = expand
        isGraficoInvestimentiExpanded = expand

        updateCollapsibleView(binding.contentTotale, binding.imgArrowTotale, expand)
        updateCollapsibleView(binding.contentAndamento, binding.imgArrowAndamento, expand)
        updateCollapsibleView(binding.contentCedoleProssime, binding.imgArrowCedoleProssime, expand)
        updateCollapsibleView(binding.contentVincoliScadenza, binding.imgArrowVincoliScadenza, expand)
        updateCollapsibleView(binding.contentGraficoNomi, binding.imgArrowGraficoNomi, expand)
        updateCollapsibleView(binding.contentGraficoTipo, binding.imgArrowGraficoTipo, expand)
        updateCollapsibleView(binding.contentGraficoDurata, binding.imgArrowGraficoDurata, expand)
        updateCollapsibleView(binding.contentGraficoBanca, binding.imgArrowGraficoBanca, expand)
        updateCollapsibleView(binding.contentGraficoAsset, binding.imgArrowGraficoAsset, expand)
        updateCollapsibleView(binding.contentGraficoInvestimenti, binding.imgArrowGraficoInvestimenti, expand)

        prefs.edit().apply {
            putBoolean("totale_expanded", expand)
            putBoolean("andamento_expanded", expand)
            putBoolean("cedole_expanded", expand)
            putBoolean("vincoli_scadenza_expanded", expand)
            putBoolean("grafico_nomi_expanded", expand)
            putBoolean("grafico_tipo_expanded", expand)
            putBoolean("grafico_durata_expanded", expand)
            putBoolean("grafico_banca_expanded", expand)
            putBoolean("grafico_asset_expanded", expand)
            putBoolean("grafico_investimenti_expanded", expand)
            apply()
        }
    }

    private fun updateCollapsibleView(view: View, arrow: View, expanded: Boolean) {
        view.visibility = if (expanded) View.VISIBLE else View.GONE
        arrow.rotation = if (expanded) 180f else 0f
    }

    private fun updateUI(items: List<BankWithAccounts>) {
        val now = Calendar.getInstance()
        val activeBanks = items.filter { !it.bank.isDeleted }

        // Variabili per i subtotali
        var totalePatrimonio = 0.0
        var totaleImmobiliare = 0.0
        var totaleMobiliareVincolato = 0.0
        var totaleMobiliareLibero = 0.0
        var totaleAltro = 0.0
        
        // Tutti gli strumenti per le liste (cedole/scadenze) e grafici andamento
        val allInstrumentsForLists = mutableListOf<VincoloWithAccount>()
        val allInstrumentsWithCategory = mutableListOf<InstrumentHistoryItem>()

        activeBanks.forEach { bankWithAccs ->
            bankWithAccs.accounts.filter { !it.account.isDeleted }.forEach { wrapper ->
                val activeVincoli = wrapper.vincoli.filter { !it.isDeleted }
                val systemType = wrapper.category?.systemType ?: Category.TYPE_DEPOSITO
                
                // Calcolo statistiche raggruppate per questo account
                val (balance, _) = InstrumentUtils.calculateAccountStats(
                    systemType,
                    activeVincoli
                )
                
                totalePatrimonio += balance
                
                when (systemType) {
                    Category.TYPE_IMMOBILI -> totaleImmobiliare += balance
                    Category.TYPE_CONTANTI, Category.TYPE_VEICOLI, Category.TYPE_GIOIELLI, Category.TYPE_OGGETTI -> {
                        totaleAltro += balance
                    }
                    Category.TYPE_CORRENTE, Category.TYPE_DEPOSITO_LIBERO -> totaleMobiliareLibero += balance
                    Category.TYPE_TITOLI, Category.TYPE_DEPOSITO, Category.TYPE_PENSIONE -> {
                        totaleMobiliareVincolato += balance
                    }
                }

                // Popoliamo la lista piatta per Cedole e Scadenze
                activeVincoli.forEach { 
                    allInstrumentsForLists.add(VincoloWithAccount(it, wrapper.account))
                    allInstrumentsWithCategory.add(InstrumentHistoryItem(it, systemType, wrapper.account.id))
                }
            }
        }

        val totaleMobiliare = totaleMobiliareVincolato + totaleMobiliareLibero

        // Visualizzazione Dati
        binding.textTotaleCapitale.text = currencyFormatter.format(totalePatrimonio)
        binding.textPatrimonioImmobiliare.text = currencyFormatter.format(totaleImmobiliare)
        binding.textPatrimonioMobiliare.text = currencyFormatter.format(totaleMobiliare)
        binding.textPatrimonioAltro.text = currencyFormatter.format(totaleAltro)
        binding.textMobiliareVincolato.text = currencyFormatter.format(totaleMobiliareVincolato)
        binding.textMobiliareLibero.text = currencyFormatter.format(totaleMobiliareLibero)

        setupAndamentoCharts(allInstrumentsWithCategory)
        setupProssimeCedole(allInstrumentsForLists)
        setupVincoliInScadenza(allInstrumentsForLists)
        setupPatrimonioPerBancaChart(activeBanks)
        
        // Per i grafici di dettaglio, filtriamo solo ciò che è attualmente attivo (non futuro, non scaduto)
        val activeRaw = allInstrumentsForLists.filter { item ->
            val vincolo = item.vincolo
            val calScadenza = Calendar.getInstance().apply {
                timeInMillis = vincolo.dataDecorrenza
                add(Calendar.MONTH, vincolo.durataMesi)
            }
            val isExpired = vincolo.durataMesi > 0 && calScadenza.before(now)
            val isFuture = vincolo.dataDecorrenza > now.timeInMillis
            !isExpired && !isFuture
        }

        // Raggruppiamo gli strumenti per nome e account per avere un conteggio reale (non per movimento)
        val historyGroups = activeRaw.filter { InstrumentUtils.isHistoryBased(it.vincolo) }
            .groupBy { item ->
                // Logica di raggruppamento differenziata:
                if (item.vincolo.tipo == "Conto Titoli") {
                    // Per PAC/ETF: raggruppiamo per Account + Nome (per gestire titoli diversi nello stesso conto)
                    "${item.account.id}_${item.vincolo.nome}"
                } else {
                    // Per CC, CD Libero, FP, Immobili, ecc.: raggruppiamo SOLO per Account
                    // (vogliamo vedere solo l'ultimo valore globale del conto, ignorando se l'etichetta è "Saldo" o altro)
                    "${item.account.id}_STORY_TOTAL"
                }
            }
            .mapValues { entry -> 
                val itemsInGroup = entry.value
                val latest = itemsInGroup.maxBy { it.vincolo.dataDecorrenza }
                
                // Determiniamo un nome leggibile: "Banca - Nome Strumento" (o Banca - Nome Conto per i generici)
                val bank = activeBanks.find { it.bank.id == latest.account.bankId }?.bank
                val bankName = bank?.name ?: ""
                
                val contentName = if (latest.vincolo.nome == "Saldo" || latest.vincolo.nome == "Saldo Iniziale") {
                    latest.account.name
                } else {
                    latest.vincolo.nome
                }
                val displayName = if (bankName.isNotEmpty()) "$contentName - $bankName" else contentName

                if (InstrumentUtils.isIncremental("Conto Titoli", latest.vincolo.strumentoDettaglio)) {
                    // Per PAC: Valorizzazione = Totale Quote * Ultimo Prezzo
                    val totalQuotes = itemsInGroup.sumOf { it.vincolo.numeroQuote }
                    val lastPrice = latest.vincolo.prezzoAcquisto
                    latest.vincolo.copy(importo = totalQuotes * lastPrice, nome = displayName)
                } else {
                    latest.vincolo.copy(nome = displayName)
                }
            }
            .values.toList()

        val singleInstruments = activeRaw.filter { !InstrumentUtils.isHistoryBased(it.vincolo) }
            .map { item ->
                val bank = activeBanks.find { it.bank.id == item.account.bankId }?.bank
                val bankName = bank?.name ?: ""
                
                val contentName = if (item.vincolo.nome == "Saldo" || item.vincolo.nome == "Saldo Iniziale") {
                    item.account.name
                } else {
                    item.vincolo.nome
                }
                val displayName = if (bankName.isNotEmpty()) "$contentName - $bankName" else contentName
                item.vincolo.copy(nome = displayName)
            }
        
        val currentlyActiveVincoli = (historyGroups + singleInstruments)

        setupPieChart(currentlyActiveVincoli)
        setupAssetPieChart(currentlyActiveVincoli)
        setupInvestimentiPieChart(currentlyActiveVincoli)
        setupDurataBarChart(currentlyActiveVincoli)
        setupBarChart(currentlyActiveVincoli)
    }

    private fun setupPatrimonioPerBancaChart(items: List<BankWithAccounts>) {
        val chart = binding.pieChartBanca
        val legendContainer = binding.legendBancaContainer
        legendContainer.removeAllViews()

        // Creiamo una lista di oggetti con Nome, Valore e Colore della Banca
        val dataList = items.map { bankWithAccs ->
            var totaleBanca = 0.0
            bankWithAccs.accounts.filter { !it.account.isDeleted }.forEach { wrapper ->
                val (balance, _) = InstrumentUtils.calculateAccountStats(
                    wrapper.category?.systemType ?: Category.TYPE_DEPOSITO,
                    wrapper.vincoli.filter { !it.isDeleted }
                )
                totaleBanca += balance
            }
            // Salviamo il nome, il totale e il colore scelto dall'utente
            Triple(bankWithAccs.bank.name, totaleBanca, bankWithAccs.bank.color)
        }.filter { it.second > 0 }.sortedByDescending { it.second }

        val entries = dataList.map { PieEntry(it.second.toFloat(), it.first) }
        val colors = dataList.map { it.third } // Estraiamo i colori degli utenti

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 2f
        
        chart.data = PieData(dataSet)
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false)
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }

        val totalePatrimonio = dataList.sumOf { it.second }
        dataList.forEach { data ->
            val percent = if (totalePatrimonio > 0) (data.second / totalePatrimonio) * 100 else 0.0
            addLegendRow(legendContainer, data.first, data.second, percent, data.third)
        }
    }

    private fun addLegendRow(container: LinearLayout, label: String, value: Double, percent: Double, color: Int) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val colorBox = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(12.dpToPx(), 12.dpToPx()).apply { marginEnd = 12.dpToPx() }
            setBackgroundColor(color)
        }
        val txtLabel = TextView(requireContext()).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        val txtValue = TextView(requireContext()).apply {
            text = "${currencyFormatter.format(value)} (${String.format(Locale.ITALY, "%.1f", percent)}%)"
            setTextColor(0xFF4CAF50.toInt())
            gravity = android.view.Gravity.END
        }
        row.addView(colorBox)
        row.addView(txtLabel)
        row.addView(txtValue)
        container.addView(row)
    }

    private fun setupAndamentoCharts(allInstruments: List<InstrumentHistoryItem>) {
        val now = System.currentTimeMillis()
        val points = mutableListOf<Long>()
        
        // 1. Punto 0: Oggi
        points.add(now)
        
        // 2. Altri 9 punti: fine dei mesi precedenti
        val cal = Calendar.getInstance()
        for (i in 1..9) {
            cal.add(Calendar.MONTH, -1)
            val endOfMonth = cal.clone() as Calendar
            endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            endOfMonth.set(Calendar.HOUR_OF_DAY, 23)
            endOfMonth.set(Calendar.MINUTE, 59)
            endOfMonth.set(Calendar.SECOND, 59)
            endOfMonth.set(Calendar.MILLISECOND, 999)
            points.add(endOfMonth.timeInMillis)
        }
        
        // Ordiniamo cronologicamente (dal più vecchio al più recente)
        val sortedPoints = points.sorted()
        
        val entriesTotale = mutableListOf<com.github.mikephil.charting.data.Entry>()
        val entriesMobiliare = mutableListOf<com.github.mikephil.charting.data.Entry>()
        val labels = mutableListOf<String>()
        val df = SimpleDateFormat("dd/MM", Locale.ITALY)
        val todayStr = df.format(Date(now))

        sortedPoints.forEachIndexed { index, ts ->
            val snapshot = calculatePortfolioAtTimestamp(allInstruments, ts)
            entriesTotale.add(com.github.mikephil.charting.data.Entry(index.toFloat(), snapshot.first.toFloat()))
            entriesMobiliare.add(com.github.mikephil.charting.data.Entry(index.toFloat(), snapshot.second.toFloat()))
            
            val dateStr = df.format(Date(ts))
            labels.add(if (dateStr == todayStr) "Oggi" else dateStr)
        }

        renderLineChart(binding.lineChartTotale, entriesTotale, labels, 0xFF4CAF50.toInt()) // Verde per totale
        renderLineChart(binding.lineChartMobiliare, entriesMobiliare, labels, 0xFF448AFF.toInt()) // Azzurro per mobiliare
    }

    private fun calculatePortfolioAtTimestamp(allInstruments: List<InstrumentHistoryItem>, timestamp: Long): Pair<Double, Double> {
        var totale = 0.0
        var mobiliare = 0.0

        // Raggruppiamo per account
        val groupedByAccount = allInstruments.groupBy { it.accountId }

        groupedByAccount.forEach { entry ->
            val instruments = entry.value
            val systemType = instruments.first().systemType
            
            // Filtriamo gli strumenti attivi alla data del timestamp
            val activeAtDate = instruments.filter { item ->
                val v = item.vincolo
                val calScadenza = Calendar.getInstance().apply {
                    timeInMillis = v.dataDecorrenza
                    add(Calendar.MONTH, v.durataMesi)
                }
                val isExpired = v.durataMesi > 0 && calScadenza.timeInMillis < timestamp
                val isFuture = v.dataDecorrenza > timestamp
                !isExpired && !isFuture && !v.isDeleted
            }

            if (activeAtDate.isEmpty()) return@forEach

            val balance: Double
            if (systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO || systemType == Category.TYPE_PENSIONE || 
                systemType == Category.TYPE_IMMOBILI || systemType == Category.TYPE_CONTANTI || systemType == Category.TYPE_VEICOLI || 
                systemType == Category.TYPE_GIOIELLI || systemType == Category.TYPE_OGGETTI) {
                // Ultimo valore inserito nello storico alla data
                balance = activeAtDate.maxBy { it.vincolo.dataDecorrenza }.vincolo.importo
            } else {
                // Deposito e Titoli: raggruppamento per nome
                val historyGroups = activeAtDate.map { it.vincolo }.filter { InstrumentUtils.isHistoryBased(it) }
                    .groupBy { it.nome }
                    .mapValues { entryInner -> 
                        val items = entryInner.value
                        if (InstrumentUtils.isIncremental("Conto Titoli", items.first().strumentoDettaglio)) {
                            val totalQuotes = items.sumOf { it.numeroQuote }
                            val lastPrice = items.maxBy { it.dataDecorrenza }.prezzoAcquisto
                            totalQuotes * lastPrice
                        } else {
                            items.maxBy { it.dataDecorrenza }.importo
                        }
                    }
                val singleTotal = activeAtDate.map { it.vincolo }.filter { !InstrumentUtils.isHistoryBased(it) }.sumOf { it.importo }
                balance = historyGroups.values.sum() + singleTotal
            }

            totale += balance
            if (systemType != Category.TYPE_IMMOBILI && systemType != Category.TYPE_CONTANTI && 
                systemType != Category.TYPE_VEICOLI && systemType != Category.TYPE_GIOIELLI && 
                systemType != Category.TYPE_OGGETTI) {
                mobiliare += balance
            }
        }
        return Pair(totale, mobiliare)
    }

    private fun renderLineChart(chart: com.github.mikephil.charting.charts.LineChart, entries: List<com.github.mikephil.charting.data.Entry>, labels: List<String>, colorRes: Int) {
        val dataSet = com.github.mikephil.charting.data.LineDataSet(entries, "")
        dataSet.apply {
            color = colorRes
            setCircleColor(colorRes)
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = colorRes
            fillAlpha = 30
            mode = com.github.mikephil.charting.data.LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.apply {
            data = com.github.mikephil.charting.data.LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            setScaleEnabled(false)
            setTouchEnabled(true)
            
            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                textColor = 0xFFBBBBBB.toInt()
                setDrawGridLines(false)
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return labels.getOrNull(value.toInt()) ?: ""
                    }
                }
                granularity = 1f
                setLabelCount(labels.size)
            }
            
            axisLeft.apply {
                textColor = 0xFFBBBBBB.toInt()
                setDrawGridLines(true)
                gridColor = 0x22FFFFFF
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value >= 1000) "${(value / 1000).toInt()}k" else value.toInt().toString()
                    }
                }
            }
            axisRight.isEnabled = false
            animateX(800)
            invalidate()
        }
    }

    private fun setupPieChart(vincoli: List<Vincolo>) {
        val chart = binding.pieChartTipo
        val legendContainer = binding.legendTipoContainer
        legendContainer.removeAllViews()
        
        val dataMap = vincoli.groupBy { it.tipo }
            .mapValues { entry -> 
                val somma = entry.value.sumOf { it.importo }
                val conteggio = entry.value.size
                Pair(somma, conteggio)
            }
            .toList()
            .sortedByDescending { it.second.first }

        val entries = dataMap.map { PieEntry(it.second.first.toFloat(), it.first) }
        val totaleSomma = dataMap.sumOf { it.second.first }

        val dataSet = PieDataSet(entries, "")
        val colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 3f
        
        chart.data = PieData(dataSet)
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false) 
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }

        dataMap.forEachIndexed { index, pair ->
            val percentuale = if (totaleSomma > 0) (pair.second.first / totaleSomma) * 100 else 0.0
            addLegendRowWithCount(legendContainer, pair.first, pair.second.first, percentuale, pair.second.second, colors[index % colors.size])
        }
    }

    private fun setupAssetPieChart(vincoli: List<Vincolo>) {
        val chart = binding.pieChartAsset
        val legendContainer = binding.legendAssetContainer
        legendContainer.removeAllViews()
        
        // Raggruppamento MACRO (Asset Class)
        val dataMap = vincoli.groupBy { v -> 
            val detail = v.strumentoDettaglio
            if (!detail.isNullOrEmpty()) {
                "Investimenti" // Tutto ciò che ha un dettaglio va in Investimenti
            } else {
                when (v.tipo) {
                    "Conto Corrente", "Conto Deposito Libero", "Contanti" -> "Liquidità"
                    "Conto Deposito" -> "Depositi Vincolati"
                    "Immobili" -> "Immobiliare"
                    "Fondo Pensione" -> "Previdenza"
                    "Veicoli", "Gioielli", "Oggetti di valore" -> "Beni di valore"
                    else -> "Investimenti"
                }
            }
        }
            .mapValues { entry -> 
                val somma = entry.value.sumOf { it.importo }
                val conteggio = entry.value.size
                Pair(somma, conteggio)
            }
            .toList()
            .sortedByDescending { it.second.first }

        val entries = dataMap.map { PieEntry(it.second.first.toFloat(), it.first) }
        val totaleSomma = dataMap.sumOf { it.second.first }

        val dataSet = PieDataSet(entries, "")
        val colors = ColorTemplate.PASTEL_COLORS.toList() + ColorTemplate.VORDIPLOM_COLORS.toList()
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 3f
        
        chart.data = PieData(dataSet)
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false) 
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }

        dataMap.forEachIndexed { index, pair ->
            val percentuale = if (totaleSomma > 0) (pair.second.first / totaleSomma) * 100 else 0.0
            addLegendRowWithCount(legendContainer, pair.first, pair.second.first, percentuale, pair.second.second, colors[index % colors.size])
        }
    }

    private fun setupInvestimentiPieChart(vincoli: List<Vincolo>) {
        val chart = binding.pieChartInvestimenti
        val legendContainer = binding.legendInvestimentiContainer
        legendContainer.removeAllViews()
        
        // Filtriamo solo gli strumenti con dettaglio (Investimenti reali)
        val investimenti = vincoli.filter { !it.strumentoDettaglio.isNullOrEmpty() }
        if (investimenti.isEmpty()) {
            binding.cardGraficoInvestimenti.visibility = View.GONE
            return
        }
        binding.cardGraficoInvestimenti.visibility = View.VISIBLE

        val dataMap = investimenti.groupBy { v -> 
            v.strumentoDettaglio!!.replace("Titoli di Stato", "TdS")
        }
            .mapValues { entry -> 
                val somma = entry.value.sumOf { it.importo }
                val conteggio = entry.value.size
                Pair(somma, conteggio)
            }
            .toList()
            .sortedByDescending { it.second.first }

        val entries = dataMap.map { PieEntry(it.second.first.toFloat(), it.first) }
        val totaleSomma = dataMap.sumOf { it.second.first }

        val dataSet = PieDataSet(entries, "")
        val colors = ColorTemplate.COLORFUL_COLORS.toList() + ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        dataSet.sliceSpace = 3f
        
        chart.data = PieData(dataSet)
        chart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            setDrawEntryLabels(false) 
            legend.isEnabled = false
            animateY(1000)
            invalidate()
        }

        dataMap.forEachIndexed { index, pair ->
            val percentuale = if (totaleSomma > 0) (pair.second.first / totaleSomma) * 100 else 0.0
            addLegendRowWithCount(legendContainer, pair.first, pair.second.first, percentuale, pair.second.second, colors[index % colors.size])
        }
    }

    private fun addLegendRowWithCount(container: LinearLayout, label: String, value: Double, percent: Double, count: Int, color: Int) {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 8, 0, 8)
        }
        val colorBox = View(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(12.dpToPx(), 12.dpToPx()).apply { marginEnd = 12.dpToPx() }
            setBackgroundColor(color)
        }
        val txtLabel = TextView(requireContext()).apply {
            text = label
            setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        val txtValue = TextView(requireContext()).apply {
            text = "${currencyFormatter.format(value)} (${String.format(Locale.ITALY, "%.1f", percent)}%)"
            setTextColor(0xFF4CAF50.toInt())
            gravity = android.view.Gravity.END
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, -2).apply { marginEnd = 16.dpToPx() }
        }
        val txtCount = TextView(requireContext()).apply {
            text = "$count str."
            setTextColor(0xFFBBBBBB.toInt())
            textSize = 12f
        }
        row.addView(colorBox)
        row.addView(txtLabel)
        row.addView(txtValue)
        row.addView(txtCount)
        container.addView(row)
    }

    private fun setupDurataBarChart(vincoli: List<Vincolo>) {
        val chart = binding.barChartDurata
        val filtered = vincoli.filter { it.tipo != "Conto Corrente" && it.durataMesi > 0 }
        val dataMap = filtered.groupBy { it.durataMesi }
            .mapValues { entry -> entry.value.size }
            .toList()
            .sortedBy { it.first }

        if (dataMap.isEmpty()) {
            chart.clear()
            return
        }

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        dataMap.forEachIndexed { index, pair ->
            entries.add(BarEntry(index.toFloat(), pair.second.toFloat()))
            labels.add("${pair.first}m")
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.colors = ColorTemplate.JOYFUL_COLORS.toList()
        dataSet.valueTextColor = 0xFFFFFFFF.toInt()
        dataSet.valueTextSize = 12f
        dataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float) = value.toInt().toString()
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        chart.data = barData

        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            textColor = 0xFFFFFFFF.toInt()
            granularity = 1f
            setLabelCount(labels.size)
        }
        chart.axisLeft.apply {
            textColor = 0xFFFFFFFF.toInt()
            axisMinimum = 0f
            granularity = 1f
        }
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false
        chart.description.isEnabled = false
        chart.animateXY(1000, 1000)
        chart.invalidate()
    }

    private fun setupBarChart(vincoli: List<Vincolo>) {
        val container = binding.legendImportoContainer
        container.removeAllViews()
        
        val totalAmount = vincoli.sumOf { it.importo }
        // Ordiniamo per valore decrescente
        val sortedVincoli = vincoli.sortedByDescending { it.importo }

        sortedVincoli.forEach { v ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 8, 0, 8)
            }
            val txtName = TextView(requireContext()).apply {
                val displayName = if (v.nome.length > 25) v.nome.take(22) + "..." else v.nome
                text = displayName
                setTextColor(0xFFFFFFFF.toInt())
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            }
            val txtAmount = TextView(requireContext()).apply {
                val percent = if (totalAmount > 0) (v.importo / totalAmount) * 100 else 0.0
                val amountStr = currencyFormatter.format(v.importo)
                val percentStr = String.format(Locale.ITALY, "%.1f", percent)
                
                text = "$amountStr ($percentStr%)"
                setTextColor(0xFF4CAF50.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = android.view.Gravity.END
            }
            row.addView(txtName)
            row.addView(txtAmount)
            container.addView(row)
        }
    }

    private fun setupProssimeCedole(items: List<VincoloWithAccount>) {
        val container = binding.containerCedoleDashboard
        container.removeAllViews()
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val mesi = prefs.getInt("mesi_cedole", 2)
        val now = Calendar.getInstance()
        val limit = Calendar.getInstance().apply { add(Calendar.MONTH, mesi) }

        val lista = mutableListOf<Triple<Long, Double, VincoloWithAccount>>()
        for (item in items) {
            val v = item.vincolo
            val end = Calendar.getInstance().apply { timeInMillis = v.dataDecorrenza; add(Calendar.MONTH, v.durataMesi) }
            val curr = Calendar.getInstance().apply { timeInMillis = v.dataDecorrenza }
            
            while (curr.before(end)) {
                val periodStart = curr.timeInMillis
                if (v.periodoCedolaMesi > 0) curr.add(Calendar.MONTH, v.periodoCedolaMesi) else curr.time = end.time
                if (curr.after(end)) curr.time = end.time
                
                if (!curr.before(now) && (mesi == 999 || !curr.after(limit))) {
                    val lordo = if (v.tipo == "Conto Deposito") (v.importo * (v.tassoVincolo/100.0) * (curr.timeInMillis - periodStart)/(24*60*60*1000.0))/365.0 
                                else (v.importo * (v.tassoVincolo/100.0) * (if (v.periodoCedolaMesi > 0) v.periodoCedolaMesi else v.durataMesi))/12.0
                    lista.add(Triple(curr.timeInMillis, lordo * (1.0 - v.tassazione), item))
                }
            }
        }
        lista.sortBy { it.first }
        lista.forEach { triple ->
            val row = TextView(requireContext()).apply {
                text = "${dateFormatter.format(Date(triple.first))} - ${currencyFormatter.format(triple.second)} (${triple.third.vincolo.nome})"
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 8, 0, 8)
            }
            container.addView(row)
        }
    }

    private fun setupVincoliInScadenza(items: List<VincoloWithAccount>) {
        val container = binding.containerVincoliScadenzaDashboard
        container.removeAllViews()
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val mesi = prefs.getInt("mesi_vincoli", 2)
        val now = Calendar.getInstance()
        val limit = Calendar.getInstance().apply { add(Calendar.MONTH, mesi) }

        val lista = items.filter { it.vincolo.tipo != "Conto Corrente" && it.vincolo.durataMesi > 0 }
            .map { it to Calendar.getInstance().apply { timeInMillis = it.vincolo.dataDecorrenza; add(Calendar.MONTH, it.vincolo.durataMesi) } }
            .filter { !it.second.before(now) && (mesi == 999 || !it.second.after(limit)) }
            .sortedBy { it.second.timeInMillis }

        lista.forEach { (item, cal) ->
            val row = TextView(requireContext()).apply {
                text = "${dateFormatter.format(cal.time)} - ${currencyFormatter.format(item.vincolo.importo)} (${item.vincolo.nome})"
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 8, 0, 8)
            }
            container.addView(row)
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class InstrumentHistoryItem(val vincolo: Vincolo, val systemType: String, val accountId: Long)
