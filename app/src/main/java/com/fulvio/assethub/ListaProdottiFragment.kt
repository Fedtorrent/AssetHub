package com.fulvio.assethub

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fulvio.assethub.databinding.FragmentListaProdottiBinding
import kotlinx.coroutines.launch

class ListaProdottiFragment : Fragment() {

    private var _binding: FragmentListaProdottiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()
    private var bankId: Long = -1L
    private var sortByBank = false
    private var lastAccounts: List<AccountWithBankAndVincoli> = emptyList()
    private lateinit var accountAdapter: AccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListaProdottiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bankId = arguments?.getLong("bankId") ?: -1L

        accountAdapter = AccountAdapter(
            onViewClick = { item ->
                val bundle = Bundle().apply {
                    putLong("accountId", item.account.id)
                    putInt("customColor", item.bank.color)
                }
                // Navigazione differenziata basata sulla categoria del conto
                val type = item.category.systemType
                if (type == Category.TYPE_CORRENTE || type == Category.TYPE_DEPOSITO_LIBERO || type == Category.TYPE_PENSIONE || type == Category.TYPE_IMMOBILI) {
                    findNavController().navigate(R.id.action_lista_prodotti_to_storico_asset, bundle)
                } else {
                    findNavController().navigate(R.id.navigation_lista_vincoli_detail, bundle)
                }
            },
            onEditClick = { item ->
                val bundle = Bundle().apply { 
                    putLong("bankId", item.account.bankId)
                    putLong("accountId", item.account.id)
                    putInt("customColor", item.bank.color)
                }
                findNavController().navigate(R.id.action_lista_prodotti_to_add_product, bundle)
            },
            onDeleteClick = { item ->
                if (item.account.isDeleted) {
                    viewModel.restoreAccount(item.account)
                    Toast.makeText(requireContext(), "Conto ripristinato", Toast.LENGTH_SHORT).show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Elimina Conto")
                        .setMessage("Sei sicuro di voler eliminare '${item.account.name}'? Tutti gli strumenti collegati verranno eliminati.")
                        .setPositiveButton("ELIMINA") { _, _ ->
                            viewModel.deleteAccount(item.account)
                        }
                        .setNegativeButton("Annulla", null)
                        .show()
                }
            },
            onPermanentDeleteClick = { item ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminazione Definitiva")
                    .setMessage("ATTENZIONE: Questa operazione eliminerà definitivamente il conto '${item.account.name}' e tutti i suoi strumenti dal database. Non sarà possibile recuperarli. Vuoi procedere?")
                    .setPositiveButton("ELIMINA DEFINITIVAMENTE") { _, _ ->
                        viewModel.deleteAccountPhysical(item.account)
                        Toast.makeText(requireContext(), "Conto eliminato definitivamente", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )

        binding.recyclerViewProdotti.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProdotti.adapter = accountAdapter

        viewModel.allAccountsWithBankAndVincoli.observe(viewLifecycleOwner) { accounts ->
            lastAccounts = accounts
            applyList()
        }

        setupMenu()
    }

    private fun applyList() {
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        val showDeleted = prefs.getBoolean("show_deleted", false)
        val filteredByDelete = if (showDeleted) lastAccounts else lastAccounts.filter { !it.account.isDeleted && !it.bank.isDeleted }
        
        val sorted = if (sortByBank) {
            filteredByDelete.sortedWith(compareBy({ it.bank.name }, { it.account.name }))
        } else {
            filteredByDelete.sortedBy { it.account.name }
        }

        if (bankId == -1L) {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Conti"
            accountAdapter.submitList(sorted)
        } else {
            // Cerchiamo il nome della banca anche se non ci sono ancora conti associati
            viewLifecycleOwner.lifecycleScope.launch {
                val bank = viewModel.getBankById(bankId)
                (activity as? AppCompatActivity)?.supportActionBar?.title = bank?.name ?: "Conti Banca"
            }
            
            val filtered = sorted.filter { it.account.bankId == bankId }
            accountAdapter.submitList(filtered)
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
                menu.findItem(R.id.action_info)?.isVisible = false
                menu.findItem(R.id.action_sort)?.isVisible = true
                updateSortMenuIcon(menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_sort -> {
                        sortByBank = !sortByBank
                        requireActivity().invalidateMenu()
                        applyList()
                        true
                    }
                    R.id.action_add -> {
                        val bundle = Bundle().apply { 
                            putLong("bankId", bankId) 
                            val color = arguments?.getInt("customColor", -1) ?: -1
                            if (color != -1) putInt("customColor", color)
                        }
                        findNavController().navigate(R.id.action_lista_prodotti_to_add_product, bundle)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
