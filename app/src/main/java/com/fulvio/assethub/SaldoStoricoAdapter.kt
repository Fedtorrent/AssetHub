package com.fulvio.assethub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fulvio.assethub.databinding.ItemSaldoStoricoBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SaldoStoricoAdapter(
    private val onDeleteClick: (Vincolo) -> Unit,
    private val onPermanentDeleteClick: (Vincolo) -> Unit,
    private val onDuplicateClick: (Vincolo) -> Unit,
    private val onEditClick: (Vincolo) -> Unit
) : ListAdapter<Vincolo, SaldoStoricoAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaldoStoricoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSaldoStoricoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Vincolo) {
            binding.textData.text = dateFormatter.format(Date(item.dataDecorrenza))
            binding.textSaldo.text = currencyFormatter.format(item.importo)
            
            val isIncremental = InstrumentUtils.isIncremental(item.tipo, item.strumentoDettaglio)
            
            // 2° riga: Quote e Prezzo (Bianco)
            if ((isIncremental && item.prezzoAcquisto > 0) || item.numeroQuote > 0) {
                binding.textQuotesInfo.visibility = View.VISIBLE
                val qStr = String.format(Locale.ITALY, "%.2f", item.numeroQuote)
                val pStr = currencyFormatter.format(item.prezzoAcquisto)
                binding.textQuotesInfo.text = "Quote: $qStr | Prezzo: $pStr"
            } else {
                binding.textQuotesInfo.visibility = View.GONE
            }

            // 1° riga: Importo speso (Variazione)
            if (item.quotaVariazione != 0.0) {
                val prefix = if (item.quotaVariazione > 0) "+" else ""
                binding.textDelta.visibility = View.VISIBLE
                binding.textDelta.text = "$prefix${currencyFormatter.format(item.quotaVariazione)}"
            } else if (item.tipo == "Conto Corrente") {
                // Per CC mostriamo il saldo direttamente nella prima riga se non c'è variazione?
                // No, seguiamo il modello: Data e Importo speso.
                binding.textDelta.visibility = View.GONE
            } else {
                binding.textDelta.visibility = View.GONE
            }

            // 3° riga: Mostriamo sempre il totale se è un PAC o CC
            binding.layoutInvestedTotal.visibility = View.VISIBLE
            
            if (isIncremental) {
                binding.labelInvested.text = "Investito fino ad ora: "
            } else {
                binding.labelInvested.text = "Saldo: "
            }
            if (item.tipo != "Conto Corrente" && item.tassoVincolo > 0 && !isIncremental) {
                binding.textTasso.visibility = View.VISIBLE
                binding.textTasso.text = "Tasso: ${item.tassoVincolo}%"
            } else {
                binding.textTasso.visibility = View.GONE
            }

            // Calcolo differenza rispetto al movimento precedente (per Conto Corrente, Conto Deposito Libero, Fondo Pensione)
            val shouldShowDiff = item.tipo == "Conto Corrente" || item.tipo == "Conto Deposito Libero" || item.tipo == "Fondo Pensione"
            if (shouldShowDiff) {
                val position = bindingAdapterPosition
                val prevItem = if (position != RecyclerView.NO_POSITION && position + 1 < currentList.size) {
                    currentList[position + 1]
                } else {
                    null
                }

                if (prevItem != null) {
                    val diff = item.importo - prevItem.importo
                    val prevImporto = prevItem.importo
                    val percent = if (prevImporto != 0.0) (diff / prevImporto) * 100.0 else 0.0

                    val sign = if (diff >= 0) "+" else ""
                    val formattedDiff = "$sign${currencyFormatter.format(diff)}"
                    val percentStr = String.format(Locale.ITALY, "%s%.1f%%", sign, percent).replace(",0%", "%")

                    binding.textDiff.visibility = View.VISIBLE
                    binding.textDiff.text = "($formattedDiff $percentStr)"
                    binding.textDiff.setTextColor(if (diff >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
                } else {
                    binding.textDiff.visibility = View.GONE
                }
            } else {
                binding.textDiff.visibility = View.GONE
            }
            
            val isDel = item.isDeleted
            binding.root.setCardBackgroundColor(if (isDel) android.graphics.Color.parseColor("#121212") else android.graphics.Color.parseColor("#212121"))
            val targetAlpha = if (isDel) 0.6f else 1.0f
            binding.layoutContent.alpha = targetAlpha

            binding.btnEdit.visibility = if (item.isDeleted) View.GONE else View.VISIBLE
            binding.btnEdit.setOnClickListener { onEditClick(item) }

            binding.btnDuplicate.visibility = if (item.isDeleted) View.GONE else View.VISIBLE
            binding.btnDuplicate.setOnClickListener { onDuplicateClick(item) }

            binding.btnDelete.setImageResource(if (item.isDeleted) android.R.drawable.ic_menu_revert else R.drawable.ic_delete_outline)
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }
            
            binding.btnHardDelete.visibility = if (item.isDeleted) View.VISIBLE else View.GONE
            binding.btnHardDelete.setOnClickListener { onPermanentDeleteClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Vincolo>() {
        override fun areItemsTheSame(oldItem: Vincolo, newItem: Vincolo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Vincolo, newItem: Vincolo): Boolean {
            return oldItem.isDeleted == newItem.isDeleted &&
                   oldItem.importo == newItem.importo &&
                   oldItem == newItem
        }
    }
}
