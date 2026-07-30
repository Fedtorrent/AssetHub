package com.fulvio.assethub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fulvio.assethub.databinding.ItemBankBinding
import java.text.NumberFormat
import java.util.*

class BankAdapter(
    private val onViewClick: (BankWithAccounts) -> Unit = {},
    private val onEditClick: (BankWithAccounts) -> Unit = {},
    private val onDeleteClick: (BankWithAccounts) -> Unit = {},
    private val onPermanentDeleteClick: (BankWithAccounts) -> Unit = {}
) : ListAdapter<BankWithAccounts, BankAdapter.BankViewHolder>(BankDiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val binding = ItemBankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BankViewHolder(private val binding: ItemBankBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BankWithAccounts) {
            val bank = item.bank
            val accounts = item.accounts
            
            binding.textBankName.text = bank.name
            binding.textBankName.setTextColor(bank.color)
            binding.viewColorIndicator.setBackgroundColor(bank.color)
            
            var totalPatrimonio = 0.0
            var activeInstrumentsCount = 0
            var activeAccountsCount = 0
            
            accounts.forEach { wrapper ->
                if (!wrapper.account.isDeleted) {
                    activeAccountsCount++
                    val activeVincoli = wrapper.vincoli.filter { !it.isDeleted }
                    val type = wrapper.category?.systemType ?: Category.TYPE_DEPOSITO
                    
                    val (accBalance, accCount) = InstrumentUtils.calculateAccountStats(
                        type,
                        activeVincoli
                    )
                    
                    totalPatrimonio += accBalance
                    // Per il conteggio totale banca, sommiamo solo se sono strumenti (non variazioni)
                    if (type == Category.TYPE_DEPOSITO || type == Category.TYPE_TITOLI) {
                        activeInstrumentsCount += accCount
                    }
                }
            }
            
            binding.textTotalBalance.text = currencyFormatter.format(totalPatrimonio)
            binding.textAccountsCount.text = "$activeAccountsCount conti"
            binding.textInstrumentsCount.text = "$activeInstrumentsCount strumenti"

            // Feedback visivo per eliminati
            binding.root.alpha = if (bank.isDeleted) 0.5f else 1.0f

            // Configurazione Barra Azioni
            val isSystemBank = bank.name == "Asset Personali"
            binding.btnView.visibility = View.VISIBLE
            binding.btnEdit.visibility = if (bank.isDeleted || isSystemBank) View.GONE else View.VISIBLE
            
            if (isSystemBank) {
                binding.btnDelete.visibility = View.GONE
                binding.btnHardDelete.visibility = View.GONE
            } else {
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnDelete.setImageResource(if (bank.isDeleted) android.R.drawable.ic_menu_revert else R.drawable.ic_delete_outline)
                binding.btnDelete.contentDescription = if (bank.isDeleted) "Ripristina" else "Elimina"
                
                binding.btnHardDelete.visibility = if (bank.isDeleted) View.VISIBLE else View.GONE
                binding.btnHardDelete.setOnClickListener { onPermanentDeleteClick(item) }
            }

            binding.btnView.setOnClickListener { onViewClick(item) }
            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    class BankDiffCallback : DiffUtil.ItemCallback<BankWithAccounts>() {
        override fun areItemsTheSame(oldItem: BankWithAccounts, newItem: BankWithAccounts): Boolean {
            return oldItem.bank.id == newItem.bank.id
        }

        override fun areContentsTheSame(oldItem: BankWithAccounts, newItem: BankWithAccounts): Boolean {
            return oldItem == newItem
        }
    }
}
