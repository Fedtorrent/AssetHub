package com.fulvio.assethub

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fulvio.assethub.databinding.FragmentContiBinding

class ContiFragment : Fragment() {

    private var _binding: FragmentContiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = BankAdapter(
            onViewClick = { item ->
                val bundle = Bundle().apply { 
                    putLong("bankId", item.bank.id)
                    putInt("customColor", item.bank.color)
                }
                // Navighiamo verso la versione DETAIL per avere la freccia indietro
                findNavController().navigate(R.id.action_conti_to_lista_prodotti_detail, bundle)
            },
            onEditClick = { item ->
                val bundle = Bundle().apply { putLong("bankId", item.bank.id) }
                findNavController().navigate(R.id.action_conti_to_add_bank, bundle)
            },
            onDeleteClick = { item ->
                if (item.bank.isDeleted) {
                    viewModel.restoreBank(item.bank)
                    Toast.makeText(requireContext(), "Banca ripristinata", Toast.LENGTH_SHORT).show()
                } else {
                    androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Elimina Banca")
                        .setMessage("Sei sicuro di voler eliminare '${item.bank.name}'? Tutti i conti e strumenti collegati verranno nascosti.")
                        .setPositiveButton("ELIMINA") { _, _ ->
                            viewModel.deleteBank(item.bank)
                        }
                        .setNegativeButton("Annulla", null)
                        .show()
                }
            },
            onPermanentDeleteClick = { item ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminazione Definitiva")
                    .setMessage("ATTENZIONE: Questa operazione eliminerà definitivamente la banca '${item.bank.name}' e tutti i dati associati dal database. Non sarà possibile recuperarli. Vuoi procedere?")
                    .setPositiveButton("ELIMINA DEFINITIVAMENTE") { _, _ ->
                        viewModel.deleteBankPhysical(item.bank)
                        Toast.makeText(requireContext(), "Banca eliminata definitivamente", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )

        binding.recyclerViewConti.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewConti.adapter = adapter

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        viewModel.allBanksWithAccounts.observe(viewLifecycleOwner) { items ->
            val showDeleted = prefs.getBoolean("show_deleted", false)
            val filtered = if (showDeleted) items else items.filter { !it.bank.isDeleted }
            adapter.submitList(filtered)
        }

        setupMenu()
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
                        findNavController().navigate(R.id.action_conti_to_add_bank)
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
