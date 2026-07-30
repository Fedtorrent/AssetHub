package com.fulvio.assethub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fulvio.assethub.databinding.FragmentLinksUtiliBinding
import com.fulvio.assethub.databinding.ItemLinkUtileBinding

class LinksUtiliFragment : Fragment() {

    private var _binding: FragmentLinksUtiliBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VincoliViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinksUtiliBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.title = "Link Utili"

        val adapter = LinksAdapter(
            onItemClick = { link ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Impossibile aprire il link", Toast.LENGTH_SHORT).show()
                }
            },
            onDeleteClick = { link ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Elimina Link")
                    .setMessage("Sei sicuro di voler eliminare il collegamento a '${link.title}'?")
                    .setPositiveButton("ELIMINA") { _, _ ->
                        viewModel.deleteUsefulLink(link)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )

        binding.recyclerViewLinks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewLinks.adapter = adapter

        viewModel.allUsefulLinks.observe(viewLifecycleOwner) { links ->
            adapter.submitList(links)
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
                        findNavController().navigate(R.id.action_links_utili_to_add_link)
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

    class LinksAdapter(
        private val onItemClick: (UsefulLink) -> Unit,
        private val onDeleteClick: (UsefulLink) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<UsefulLink, LinksAdapter.ViewHolder>(DiffCallback()) {

        class ViewHolder(val b: ItemLinkUtileBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val b = ItemLinkUtileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(b)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            holder.b.textLinkTitolo.text = item.title
            holder.b.textLinkDesc.text = item.description
            holder.b.textLinkUrl.text = item.url
            holder.b.imgLinkIcon.setImageResource(item.iconResId)
            
            holder.b.btnDeleteLink.visibility = View.VISIBLE
            holder.b.btnDeleteLink.setOnClickListener { onDeleteClick(item) }
            holder.b.root.setOnClickListener { onItemClick(item) }
        }

        class DiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<UsefulLink>() {
            override fun areItemsTheSame(oldItem: UsefulLink, newItem: UsefulLink) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: UsefulLink, newItem: UsefulLink) = oldItem == newItem
        }
    }
}
