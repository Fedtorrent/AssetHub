package com.fulvio.assethub

import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.fulvio.assethub.databinding.FragmentDettaglioVincoloBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DettaglioVincoloFragment : Fragment() {

    private var _binding: FragmentDettaglioVincoloBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    private var currentItem: VincoloWithFullInfo? = null
    private var isGraficoExpanded = false
    private var isDatiExpanded = false
    private var isCedoleExpanded = false
    
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDettaglioVincoloBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vincoloId = arguments?.getLong("vincoloId") ?: -1L
        if (vincoloId != -1L) {
            caricaDati(vincoloId)
        }

        // Sincronizzazione stato iniziale (TUTTO CHIUSO)
        isDatiExpanded = false
        isGraficoExpanded = false
        isCedoleExpanded = false

        binding.contentDati.visibility = View.GONE
        binding.contentGrafico.visibility = View.GONE
        binding.contentCedole.visibility = View.GONE
        
        binding.imgArrowDati.rotation = 0f
        binding.imgArrowGrafico.rotation = 0f
        binding.imgArrowCedole.rotation = 0f

        setupCollapsibleSections()
        setupMenu()
    }

    private fun caricaDati(id: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            val item = viewModel.getWithFullInfoById(id)
            if (item != null) {
                currentItem = item
                item.accountWithBank?.let { accountWithBank ->
                    (activity as? AppCompatActivity)?.supportActionBar?.title = accountWithBank.bank.name
                    mostraDati(item.vincolo, accountWithBank.account, accountWithBank.bank)
                }
                requireActivity().invalidateMenu()
                generaListaCedole(item.vincolo)
                
                if (InstrumentUtils.isHistoryBased(item.vincolo)) {
                    binding.cardGraficoAndamento.visibility = View.VISIBLE
                    // Recuperiamo tutti i movimenti per questo account per mostrare il trend reale dell'asset
                    viewLifecycleOwner.lifecycleScope.launch {
                        val allMovements = viewModel.allVincoli.value?.filter { 
                            it.accountId == item.vincolo.accountId && it.nome == item.vincolo.nome && !it.isDeleted 
                        } ?: listOf(item.vincolo)
                        setupTrendChart(allMovements)
                    }
                } else {
                    binding.cardGraficoAndamento.visibility = View.GONE
                }
            }
        }
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
            color = 0xFF448AFF.toInt() // azure_primary
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
                if (currentItem?.vincolo?.tipo == "Conto Corrente" || currentItem?.vincolo?.tipo == "Conto Deposito Libero") {
                    menuInflater.inflate(R.menu.menu_info_conto, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_info_conto -> {
                        mostraInfoContoLibero()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun mostraInfoContoLibero() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Calcolo Bollo Conto Libero")
            .setMessage("• L'app lo calcola assumendo che il saldo indicato sia la media degli ultimi 3 mesi (con rendicontazione trimestrale) o annuale (con rendicontazione annuale). Non è un calcolo esatto ma non dovrebbe discostarsi di molto dal reale se il conto è utilizzato come un conto deposito e non come conto per la quotidianità.\n\n" +
                    "• L'interesse è calcolato giornalmente dalla data di decorrenza o data ultimo saldo inserito ad oggi.\n\n" +
                    "• Il bollo è calcolato sui trimestri o anni solari (es. 31/03, 30/06...).\n\n" +
                    "• Modificando Saldo o Tasso, gli interessi e i bolli maturati fino a oggi verranno 'congelati' e salvati nello storico del vincolo.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun setupCollapsibleSections() {
        binding.headerDati.setOnClickListener {
            isDatiExpanded = !isDatiExpanded
            binding.contentDati.visibility = if (isDatiExpanded) View.VISIBLE else View.GONE
            binding.imgArrowDati.rotation = if (isDatiExpanded) 180f else 0f
        }

        binding.headerGrafico.setOnClickListener {
            isGraficoExpanded = !isGraficoExpanded
            binding.contentGrafico.visibility = if (isGraficoExpanded) View.VISIBLE else View.GONE
            binding.imgArrowGrafico.rotation = if (isGraficoExpanded) 180f else 0f
            if (isGraficoExpanded) {
                binding.lineChartTrend.animateX(800)
            }
        }

        binding.headerCedole.setOnClickListener {
            isCedoleExpanded = !isCedoleExpanded
            binding.contentCedole.visibility = if (isCedoleExpanded) View.VISIBLE else View.GONE
            binding.imgArrowCedole.rotation = if (isCedoleExpanded) 180f else 0f
        }
    }

    private fun mostraDati(vincolo: Vincolo, account: Account, bank: Bank) {
        val container = binding.contentDati
        container.removeAllViews()

        // Sincronizzazione UI con lo stato delle variabili
        binding.contentDati.visibility = if (isDatiExpanded) View.VISIBLE else View.GONE
        binding.imgArrowDati.rotation = if (isDatiExpanded) 180f else 0f
        
        binding.contentGrafico.visibility = if (isGraficoExpanded) View.VISIBLE else View.GONE
        binding.imgArrowGrafico.rotation = if (isGraficoExpanded) 180f else 0f
        
        binding.contentCedole.visibility = if (isCedoleExpanded) View.VISIBLE else View.GONE
        binding.imgArrowCedole.rotation = if (isCedoleExpanded) 180f else 0f

        val now = Calendar.getInstance()
        val calDecorrenza = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
        val calScadenza = Calendar.getInstance().apply {
            timeInMillis = vincolo.dataDecorrenza
            add(Calendar.MONTH, vincolo.durataMesi)
        }
        
        val bolloText: String
        val bolloAcaricoBanca = account.bolloCaricoBanca
        val isBFP = vincolo.strumentoDettaglio == "BFP"
        
        if (vincolo.tipo == "Conto Corrente") {
            val bolloMaturato = calcolaBolloCC(vincolo, now) + vincolo.bolliConsolidati
            bolloText = if (vincolo.importo > 5000 || vincolo.bolliConsolidati > 0) {
                if (bolloAcaricoBanca) {
                    "${currencyFormatter.format(0.0)} (Assolto da Banca)"
                } else {
                    "${currencyFormatter.format(bolloMaturato)} (Rendicontazione ${vincolo.frequenzaRendicontazione})"
                }
            } else {
                "${currencyFormatter.format(0.0)} (Sotto la soglia dei 5.000€)"
            }
        } else {
            var numBolli = 0
            val calTemp = Calendar.getInstance().apply {
                set(calDecorrenza.get(Calendar.YEAR), Calendar.DECEMBER, 31, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            while (!calTemp.after(calScadenza)) {
                if (!calTemp.before(calDecorrenza)) numBolli++
                calTemp.add(Calendar.YEAR, 1)
            }
            val costoBolloSingolo = vincolo.importo * 0.002
            val importoBolloComplessivo = if (bolloAcaricoBanca) 0.0 else costoBolloSingolo * numBolli
            bolloText = if (bolloAcaricoBanca) {
                "${currencyFormatter.format(0.0)} (n. $numBolli bolli assolti da banca)"
            } else {
                "${currencyFormatter.format(importoBolloComplessivo)} (n. $numBolli bolli da pagare)"
            }
        }

        val dati = mutableListOf(
            "ID Strumento" to String.format(Locale.ITALY, "%02d", vincolo.codiceVincolo),
            "Banca" to bank.name,
            "Nome Conto" to account.name,
            "Tipo Conto" to vincolo.tipo,
            "Nome Strumento" to vincolo.nome,
            "Tipo Strumento" to if (!vincolo.strumentoDettaglio.isNullOrEmpty()) vincolo.strumentoDettaglio else vincolo.tipo,
            "Capitale Investito" to currencyFormatter.format(vincolo.importo)
        )

        if (vincolo.numeroQuote > 0) {
            dati.add("Numero Quote" to String.format(Locale.ITALY, "%.2f", vincolo.numeroQuote))
            dati.add("Prezzo d'Acquisto" to currencyFormatter.format(vincolo.prezzoAcquisto))
        }

        dati.add("Data Inizio" to dateFormatter.format(Date(vincolo.dataDecorrenza)))

        val isHistoryType = vincolo.tipo == "Conto Corrente" || vincolo.tipo == "Conto Deposito Libero"

        if (!isHistoryType && !isBFP) {
            dati.add("Durata" to "${vincolo.durataMesi} mesi")
            dati.add("Tasso Vincolo" to "${vincolo.tassoVincolo}%")
            dati.add("Svincolabile" to if (vincolo.svincolabile) "Sì (Tasso: ${vincolo.tassoSvincolo}%)" else "No")
        } else if (isBFP) {
            dati.add("Durata" to "${vincolo.durataMesi} mesi")
        } else {
            dati.add("Tasso Attuale" to "${vincolo.tassoVincolo}%")
            val pMesi = if (vincolo.periodoCedolaMesi <= 0) 12 else vincolo.periodoCedolaMesi
            val paymentPeriod = when (pMesi) {
                1 -> "Mensile"
                3 -> "Trimestrale"
                6 -> "Semestrale"
                12 -> "Annuale"
                else -> "$pMesi mesi"
            }
            dati.add("Periodo Pagamento Interessi" to paymentPeriod)
        }

        if (!isBFP) {
            dati.add("Tassazione" to if (vincolo.tassazione == 0.125) "12,50%" else "26,00%")
            
            if (!isHistoryType) {
                dati.add("Cedola ogni" to if (vincolo.periodoCedolaMesi > 0) "${vincolo.periodoCedolaMesi} mesi" else "Alla scadenza")
                dati.add("Bollo a carico banca" to if (account.bolloCaricoBanca) "Sì" else "No")
            } else {
                dati.add("Bollo a carico banca" to if (account.bolloCaricoBanca) "Sì" else "No")
            }

            dati.add("Imposta di Bollo" to bolloText)
        }
        
        if (vincolo.interessiMaturatiPrecedenti > 0) {
            dati.add("Interessi Consolidati" to currencyFormatter.format(vincolo.interessiMaturatiPrecedenti))
        }
        
        if (vincolo.bolliConsolidati > 0) {
            dati.add("Bolli Consolidati" to currencyFormatter.format(vincolo.bolliConsolidati))
        }

        dati.add("Note" to (vincolo.note ?: "-"))

        dati.forEach { (label, value) ->
            val textView = TextView(requireContext()).apply {
                val spannable = android.text.SpannableString("$label: $value")
                spannable.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0,
                    label.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text = spannable
                setTextColor(0xFFFFFFFF.toInt())
                setPadding(0, 8, 0, 8)
                textSize = 14f
            }
            container.addView(textView)
        }
    }

    private fun calcolaBolloCC(vincolo: Vincolo, now: Calendar): Double {
        if (vincolo.importo <= 5000) return 0.0
        val calInizio = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
        var bolloTotale = 0.0
        val temp = Calendar.getInstance().apply { timeInMillis = calInizio.timeInMillis }

        if (vincolo.frequenzaRendicontazione == "Trimestrale") {
            while (!temp.after(now)) {
                val m = temp.get(Calendar.MONTH)
                val d = temp.get(Calendar.DAY_OF_MONTH)
                if ((m == Calendar.MARCH && d == 31) || (m == Calendar.JUNE && d == 30) || 
                    (m == Calendar.SEPTEMBER && d == 30) || (m == Calendar.DECEMBER && d == 31)) {
                    bolloTotale += 8.55
                }
                temp.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            while (!temp.after(now)) {
                if (temp.get(Calendar.MONTH) == Calendar.DECEMBER && temp.get(Calendar.DAY_OF_MONTH) == 31) {
                    bolloTotale += 34.20
                }
                temp.add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return bolloTotale
    }

    private fun generaListaCedole(vincolo: Vincolo) {
        val container = binding.containerCedole
        container.removeAllViews()

        if (vincolo.strumentoDettaglio == "BFP") {
            binding.cardCedole.visibility = View.GONE
            return
        }

        val now = Calendar.getInstance()

        if (vincolo.tipo == "Conto Corrente" || vincolo.tipo == "Conto Deposito Libero") {
            // Per il Conto Libero mostriamo un'unica riga con il guadagno maturato ad oggi
            val diffMillis = now.timeInMillis - vincolo.dataDecorrenza
            val gg = (diffMillis / (1000L * 60 * 60 * 24)).toDouble().coerceAtLeast(0.0)
            val lordoAttuale = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * gg) / 365.0
            val nettoAttuale = lordoAttuale * (1.0 - vincolo.tassazione)
            
            // Aggiungiamo eventuali interessi consolidati
            val nettoTotale = nettoAttuale + vincolo.interessiMaturatiPrecedenti
            val lordoTotale = lordoAttuale + (vincolo.interessiMaturatiPrecedenti / (1.0 - vincolo.tassazione))

            // Rigo unico
            val rowContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
            }
            val txtDesc = TextView(requireContext()).apply {
                text = "Interessi ad oggi"
                setTextColor(0xFFFFFFFF.toInt())
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val txtLordo = TextView(requireContext()).apply {
                text = currencyFormatter.format(lordoTotale)
                setTextColor(0xFF4CAF50.toInt())
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            }
            val txtNetto = TextView(requireContext()).apply {
                text = currencyFormatter.format(nettoTotale)
                setTextColor(0xFF4CAF50.toInt())
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            }
            rowContainer.addView(txtDesc)
            rowContainer.addView(txtLordo)
            rowContainer.addView(txtNetto)
            container.addView(rowContainer)

            binding.textTotaleLordo.text = currencyFormatter.format(lordoTotale)
            binding.textTotaleNetto.text = currencyFormatter.format(nettoTotale)
            binding.textResiduoLordo.text = currencyFormatter.format(0.0)
            binding.textResiduoNetto.text = currencyFormatter.format(0.0)
            return
        }

        val calScadenza = Calendar.getInstance().apply {
            timeInMillis = vincolo.dataDecorrenza
            add(Calendar.MONTH, vincolo.durataMesi)
        }
        val dataScadenza = calScadenza.time

        val calCorrente = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
        val calInizio = Calendar.getInstance()

        var sommaLordo = 0.0
        var sommaNetto = 0.0
        var residuoLordo = 0.0
        var residuoNetto = 0.0

        while (calCorrente.before(calScadenza)) {
            calInizio.timeInMillis = calCorrente.timeInMillis
            
            if (vincolo.periodoCedolaMesi > 0) {
                calCorrente.add(Calendar.MONTH, vincolo.periodoCedolaMesi)
            } else {
                calCorrente.time = dataScadenza
            }

            if (calCorrente.after(calScadenza)) {
                calCorrente.time = dataScadenza
            }

            // Calcolo importi basato sulla tipologia
            val lordo: Double
            
            if (vincolo.tipo == "Conto Deposito") {
                val diffMillis = calCorrente.timeInMillis - calInizio.timeInMillis
                val giorni = (diffMillis / (24 * 60 * 60 * 1000)).toDouble()
                lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * giorni) / 365.0
            } else {
                // Titoli di Stato e Obbligazioni
                val mesi = if (vincolo.periodoCedolaMesi > 0) vincolo.periodoCedolaMesi.toDouble() else vincolo.durataMesi.toDouble()
                lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * mesi) / 12.0
            }
            val netto = lordo * (1.0 - vincolo.tassazione)

            sommaLordo += lordo
            sommaNetto += netto
            
            // Se la data della cedola è oggi o futura, aggiungila al residuo
            if (!calCorrente.before(now)) {
                residuoLordo += lordo
                residuoNetto += netto
            }

            // Aggiunta riga alla UI
            val row = LayoutInflater.from(requireContext()).inflate(android.R.layout.simple_list_item_1, null)
            val rowContainer = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 12, 0, 12)
            }
            
            val isFutureOrToday = !calCorrente.before(now)
            val textColor = if (isFutureOrToday) 0xFFFFFFFF.toInt() else 0xFF888888.toInt()
            val amountColor = if (isFutureOrToday) 0xFF4CAF50.toInt() else 0xFF668866.toInt()

            val txtDate = TextView(requireContext()).apply {
                text = dateFormatter.format(calCorrente.time)
                setTextColor(textColor)
                if (isFutureOrToday) {
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            val txtLordo = TextView(requireContext()).apply {
                text = currencyFormatter.format(lordo)
                setTextColor(amountColor)
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            }
            val txtNetto = TextView(requireContext()).apply {
                text = currencyFormatter.format(netto)
                setTextColor(amountColor)
                gravity = android.view.Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f)
            }

            rowContainer.addView(txtDate)
            rowContainer.addView(txtLordo)
            rowContainer.addView(txtNetto)
            container.addView(rowContainer)
            
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(0xFF333333.toInt())
            }
            container.addView(divider)
        }

        // Mostra i totali calcolati
        binding.textTotaleLordo.text = currencyFormatter.format(sommaLordo)
        binding.textTotaleNetto.text = currencyFormatter.format(sommaNetto)
        binding.textResiduoLordo.text = currencyFormatter.format(residuoLordo)
        binding.textResiduoNetto.text = currencyFormatter.format(residuoNetto)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
