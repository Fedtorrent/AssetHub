package com.fulvio.assethub

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar
import java.util.Date

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.addTextChangedListener
import com.fulvio.assethub.databinding.FragmentListaVincoliBinding

class ListaVincoliFragment : Fragment() {
    
    private var _binding: FragmentListaVincoliBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VincoliViewModel by viewModels()
    private var fullList: List<VincoloWithFullInfo> = emptyList()
    private var accountId: Long = -1L
    private var sortByBank = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaVincoliBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        accountId = arguments?.getLong("accountId") ?: -1L

        val adapter = VincoloAdapter(
            onViewClick = { item ->
                val bundle = Bundle().apply {
                    putLong("accountId", item.accountWithBank?.account?.id ?: -1L)
                    putInt("customColor", item.accountWithBank?.bank?.color ?: -1)
                }
                
                if (InstrumentUtils.isHistoryBased(item.vincolo)) {
                    bundle.putString("assetName", item.vincolo.nome)
                    findNavController().navigate(R.id.action_lista_vincoli_to_storico_asset, bundle)
                } else {
                    bundle.putLong("vincoloId", item.vincolo.id)
                    findNavController().navigate(R.id.action_lista_vincoli_to_dettaglio_vincolo, bundle)
                }
            },
            onEditClick = { item ->
                val bundle = Bundle().apply {
                    putLong("vincoloId", item.vincolo.id)
                    putBoolean("isDuplicate", false)
                    putInt("customColor", item.accountWithBank?.bank?.color ?: -1)
                }
                findNavController().navigate(R.id.action_lista_vincoli_to_add_vincolo, bundle)
            },
            onDuplicateClick = { item ->
                val bundle = Bundle().apply {
                    putLong("vincoloId", item.vincolo.id)
                    putBoolean("isDuplicate", true)
                    putInt("customColor", item.accountWithBank?.bank?.color ?: -1)
                }
                findNavController().navigate(R.id.action_lista_vincoli_to_add_vincolo, bundle)
            },
            onDeleteClick = { item ->
                if (item.vincolo.isDeleted) {
                    viewModel.restore(item.vincolo)
                    Toast.makeText(requireContext(), "Strumento ripristinato", Toast.LENGTH_SHORT).show()
                } else {
                    val vincolo = item.vincolo
                    val accountName = item.accountWithBank?.account?.name ?: ""
                    
                    val isGeneric = vincolo.nome == "Saldo Iniziale" || 
                                    vincolo.nome == "Saldo" && (vincolo.tipo == "Conto Corrente" || 
                                                                vincolo.tipo == "Conto Deposito Libero" ||
                                                                vincolo.tipo == "Fondo Pensione" || 
                                                                vincolo.tipo == "Immobili")

                    val nameToShow = if (isGeneric) accountName else vincolo.nome

                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Elimina Strumento")
                        .setMessage("Sei sicuro di voler eliminare lo strumento '$nameToShow'? Verrà eliminato.")
                        .setPositiveButton("ELIMINA") { _, _ ->
                            viewModel.delete(item.vincolo)
                        }
                        .setNegativeButton("Annulla", null)
                        .show()
                }
            },
            onPermanentDeleteClick = { item ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminazione Definitiva")
                    .setMessage("ATTENZIONE: Questa operazione eliminerà definitivamente lo strumento '${item.vincolo.nome}' dal database. Non sarà possibile recuperarlo. Vuoi procedere?")
                    .setPositiveButton("ELIMINA DEFINITIVAMENTE") { _, _ ->
                        viewModel.deletePhysical(item.vincolo)
                        Toast.makeText(requireContext(), "Strumento eliminato definitivamente", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )
        binding.recyclerViewVincoli.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewVincoli.adapter = adapter

        val now = Calendar.getInstance()

        viewModel.allVincoliWithFullInfo.observe(viewLifecycleOwner) { items ->
            fullList = items
            applyFilters(now)
        }

        binding.editSearch.addTextChangedListener {
            applyFilters(now)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
                menu.findItem(R.id.action_sort)?.isVisible = true
                updateSortMenuIcon(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort -> {
                        sortByBank = !sortByBank
                        requireActivity().invalidateMenu()
                        applyFilters(now)
                        true
                    }
                    R.id.action_add -> {
                        val bundle = Bundle().apply {
                            putLong("accountId", accountId)
                            val color = arguments?.getInt("customColor", -1) ?: -1
                            if (color != -1) putInt("customColor", color)
                        }
                        findNavController().navigate(R.id.action_lista_vincoli_to_add_vincolo, bundle)
                        true
                    }
                    R.id.action_info -> {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Visualizzazione Lista")
                            .setMessage("Puoi visualizzare gli strumenti già scaduti o eliminati attivando le rispettive opzioni nelle impostazioni di visualizzazione nelle impostazioni dell'app.")
                            .setPositiveButton("Capito", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateSortMenuIcon(menu: Menu) {
        val item = menu.findItem(R.id.action_sort) ?: return
        if (sortByBank) {
            item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_sort_alpha)
            item.title = "Ordina Alfabeticamente"
        } else {
            item.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_bank)
            item.title = "Ordina per Banca"
        }
    }

    private fun applyFilters(now: Calendar) {
        val query = binding.editSearch.text.toString().lowercase().trim()
        val adapter = binding.recyclerViewVincoli.adapter as? VincoloAdapter
        
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val showDeleted = prefs.getBoolean("show_deleted", false)
        val showExpired = prefs.getBoolean("show_expired", false)
        val showNotActive = prefs.getBoolean("show_not_active", true)

        val baseFiltered = fullList.filter { item ->
            val vincolo = item.vincolo
            val accountWithBank = item.accountWithBank ?: return@filter false
            val account = accountWithBank.account
            val bank = accountWithBank.bank
            
            val tipo = vincolo.tipo
            if (tipo == "Conto Corrente" || tipo == "Conto Deposito" || tipo == "Conto Deposito Libero" || tipo == "Fondo Pensione" ||
                tipo == "Immobili" || tipo == "Contanti" || tipo == "Veicoli" || tipo == "Gioielli" || tipo == "Oggetti di valore") {
                return@filter false
            }
            
            if (bank.name == "Asset Personali" && accountId == -1L) return@filter false
            if (!showDeleted && (vincolo.isDeleted || account.isDeleted || bank.isDeleted)) return@filter false
            if (accountId != -1L && account.id != accountId) return@filter false

            val calScadenza = Calendar.getInstance().apply {
                timeInMillis = vincolo.dataDecorrenza
                add(Calendar.MONTH, vincolo.durataMesi)
            }
            val isExpired = vincolo.durataMesi > 0 && calScadenza.before(now)
            if (!showExpired && isExpired) return@filter false

            val isFuture = vincolo.dataDecorrenza > now.timeInMillis
            if (!showNotActive && isFuture) return@filter false

            if (query.isNotEmpty()) {
                val match = vincolo.nome.lowercase().contains(query) ||
                            account.name.lowercase().contains(query) ||
                            bank.name.lowercase().contains(query) ||
                            vincolo.importo.toString().contains(query) ||
                            vincolo.tipo.lowercase().contains(query) ||
                            (vincolo.strumentoDettaglio?.lowercase()?.contains(query) ?: false) ||
                            (vincolo.note?.lowercase()?.contains(query) ?: false)
                if (!match) return@filter false
            }

            true
        }

        // Raggruppamento per PAC/History
        val historyGroups = baseFiltered.filter { InstrumentUtils.isHistoryBased(it.vincolo) }
            .groupBy { "${it.accountWithBank?.account?.id ?: -1L}_${it.vincolo.nome}" }
            .mapValues { entry -> 
                val items = entry.value
                val latest = items.maxBy { it.vincolo.dataDecorrenza }
                val vincoliList = items.map { it.vincolo }
                
                if (InstrumentUtils.isIncremental("Conto Titoli", latest.vincolo.strumentoDettaglio)) {
                    // Per PAC: 
                    // importo = Valorizzazione (Quote * Ultimo Prezzo)
                    // quotaVariazione = Totale Investito (Somma versamenti)
                    val totalQuotes = items.sumOf { it.vincolo.numeroQuote }
                    val lastPrice = latest.vincolo.prezzoAcquisto
                    val valuation = totalQuotes * lastPrice
                    val totalInvested = items.sumOf { it.vincolo.quotaVariazione }
                    
                    latest.copy(vincolo = latest.vincolo.copy(
                        importo = valuation,
                        quotaVariazione = totalInvested
                    ))
                } else if (latest.vincolo.tipo == "Conto Corrente" || latest.vincolo.tipo == "Conto Deposito Libero") {
                    // Per CC e CD Libero: Calcoliamo gli interessi del periodo corrente
                    val periodicity = latest.vincolo.periodoCedolaMesi
                    val startDate = InterestUtils.getStartOfCalculationDate(periodicity)
                    val nowTs = System.currentTimeMillis()
                    val interests = InterestUtils.calculateCumulativeInterests(vincoliList, startDate, nowTs)
                    
                    // Usiamo interessiMaturatiPrecedenti come campo temporaneo per trasportare il calcolo all'adapter
                    latest.copy(vincolo = latest.vincolo.copy(
                        interessiMaturatiPrecedenti = interests.second
                    ))
                } else {
                    latest
                }
            }
            .values.toList()

        val singleInstruments = baseFiltered.filter { !InstrumentUtils.isHistoryBased(it.vincolo) }

        val rawResult = historyGroups + singleInstruments
        val finalResult = if (sortByBank) {
            rawResult.sortedWith(compareBy({ it.accountWithBank?.bank?.name ?: "" }, { it.vincolo.nome }))
        } else {
            rawResult.sortedBy { it.vincolo.nome }
        }
        
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Strumenti"

        val isAccountView = accountId != -1L
        if (finalResult.isEmpty() && isAccountView) {
            binding.textEmptyState.visibility = View.VISIBLE
            binding.recyclerViewVincoli.visibility = View.GONE
        } else {
            binding.textEmptyState.visibility = View.GONE
            binding.recyclerViewVincoli.visibility = View.VISIBLE
        }

        adapter?.submitList(finalResult)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
