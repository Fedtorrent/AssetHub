package com.fulvio.assethub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fulvio.assethub.databinding.ItemAccountBinding
import java.text.NumberFormat
import java.util.*

class AccountAdapter(
    private val onViewClick: (AccountWithBankAndVincoli) -> Unit = {},
    private val onEditClick: (AccountWithBankAndVincoli) -> Unit = {},
    private val onDeleteClick: (AccountWithBankAndVincoli) -> Unit = {},
    private val onPermanentDeleteClick: (AccountWithBankAndVincoli) -> Unit = {},
    private val readOnly: Boolean = false
) : ListAdapter<AccountWithBankAndVincoli, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AccountViewHolder(private val binding: ItemAccountBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AccountWithBankAndVincoli) {
            val account = item.account
            val bank = item.bank
            val vincoli = item.vincoli
            
            binding.textBankName.visibility = View.VISIBLE
            binding.textBankName.text = bank.name
            binding.textAccountName.text = account.name
            binding.textAccountName.setTextColor(bank.color)
            binding.textAccountCategory.text = item.category?.name ?: ""
            binding.viewColorIndicator.setBackgroundColor(bank.color)
            
            val activeVincoli = vincoli.filter { !it.isDeleted }
            val systemType = item.category?.systemType ?: Category.TYPE_DEPOSITO
            
            val (totalBalance, effectiveCount) = InstrumentUtils.calculateAccountStats(
                systemType,
                activeVincoli
            )

            if (systemType == Category.TYPE_CORRENTE || systemType == Category.TYPE_DEPOSITO_LIBERO || systemType == Category.TYPE_PENSIONE || systemType == Category.TYPE_IMMOBILI) {
                binding.textVincoliCount.text = if (effectiveCount > 1) "$effectiveCount variazioni" else ""
            } else {
                binding.textVincoliCount.text = "$effectiveCount strumenti"
            }
            
            binding.textTotalBalance.text = currencyFormatter.format(totalBalance)
            binding.textAccountInfo.visibility = View.GONE

            // Feedback visivo per eliminati
            binding.root.alpha = if (account.isDeleted || bank.isDeleted) 0.5f else 1.0f

            // Configurazione Barra Azioni
            if (readOnly) {
                binding.dividerActions.visibility = View.GONE
                binding.layoutActions.visibility = View.GONE
            } else {
                binding.dividerActions.visibility = View.VISIBLE
                binding.layoutActions.visibility = View.VISIBLE
                binding.btnView.visibility = View.VISIBLE
                binding.btnEdit.visibility = View.GONE
                binding.btnDelete.setImageResource(if (account.isDeleted) android.R.drawable.ic_menu_revert else R.drawable.ic_delete_outline)
                binding.btnDelete.contentDescription = if (account.isDeleted) "Ripristina" else "Elimina"
                
                binding.btnHardDelete.visibility = if (account.isDeleted) View.VISIBLE else View.GONE
                binding.btnHardDelete.setOnClickListener { onPermanentDeleteClick(item) }

                binding.btnAddSubItem.visibility = View.GONE
                
                binding.btnView.setOnClickListener { onViewClick(item) }
                binding.btnEdit.setOnClickListener { onEditClick(item) }
                binding.btnDelete.setOnClickListener { onDeleteClick(item) }
            }
        }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<AccountWithBankAndVincoli>() {
        override fun areItemsTheSame(oldItem: AccountWithBankAndVincoli, newItem: AccountWithBankAndVincoli): Boolean {
            return oldItem.account.id == newItem.account.id
        }

        override fun areContentsTheSame(oldItem: AccountWithBankAndVincoli, newItem: AccountWithBankAndVincoli): Boolean {
            return oldItem == newItem
        }
    }
}
