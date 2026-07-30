package com.fulvio.assethub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fulvio.assethub.databinding.ItemVincoloBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class VincoloAdapter(
    private val onViewClick: (VincoloWithFullInfo) -> Unit = {},
    private val onEditClick: (VincoloWithFullInfo) -> Unit = {},
    private val onDuplicateClick: (VincoloWithFullInfo) -> Unit = {},
    private val onDeleteClick: (VincoloWithFullInfo) -> Unit = {},
    private val onPermanentDeleteClick: (VincoloWithFullInfo) -> Unit = {}
) : ListAdapter<VincoloWithFullInfo, VincoloAdapter.VincoloViewHolder>(VincoloDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.ITALY)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.ITALY)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VincoloViewHolder {
        val binding = ItemVincoloBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VincoloViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VincoloViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VincoloViewHolder(private val binding: ItemVincoloBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: VincoloWithFullInfo) {
            val vincolo = item.vincolo
            val accountWithBank = item.accountWithBank ?: return
            val account = accountWithBank.account
            val bank = accountWithBank.bank
            val now = Calendar.getInstance()

            // Calcolo scadenza finale
            val calScadenza = Calendar.getInstance()
            calScadenza.timeInMillis = vincolo.dataDecorrenza
            calScadenza.add(Calendar.MONTH, vincolo.durataMesi)
            val dataScadenza = calScadenza.time

            val isContoLibero = vincolo.tipo == "Conto Corrente" || vincolo.tipo == "Conto Deposito Libero"
            val isScaduto = vincolo.durataMesi > 0 && calScadenza.before(now)
            val isEffettivamenteScaduto = isScaduto
            val isFuturo = vincolo.dataDecorrenza > now.timeInMillis

            // Reset colori e visibilità default
            val colorWhite = 0xFFFFFFFF.toInt()
            val colorGray = 0xFF888888.toInt()
            val colorGreen = 0xFF4CAF50.toInt()

            binding.textBanca.setTextColor(if (isEffettivamenteScaduto) colorGray else colorGray) // Riga 3 sempre grigia
            binding.textNome.setTextColor(if (isEffettivamenteScaduto) colorGray else bank.color)
            binding.textTipoStrumento.setTextColor(if (isEffettivamenteScaduto) colorGray else 0xFFBBBBBB.toInt())
            binding.textImporto.setTextColor(if (isEffettivamenteScaduto) colorGray else colorGreen)
            binding.textScadenza.setTextColor(if (isEffettivamenteScaduto) colorGray else colorWhite)
            binding.textDataCedola.setTextColor(if (isEffettivamenteScaduto) colorGray else colorWhite)
            binding.textImportoCedola.setTextColor(if (isEffettivamenteScaduto) colorGray else colorGreen)

            val isGenericName = vincolo.nome == "Saldo Iniziale" || 
                                vincolo.nome == "Saldo" && (vincolo.tipo == "Conto Corrente" || 
                                                            vincolo.tipo == "Conto Deposito Libero" ||
                                                            vincolo.tipo == "Fondo Pensione" || 
                                                            vincolo.tipo == "Immobili")
            
            if (isGenericName) {
                // Caso B: Conto con nome generico
                binding.textNome.text = account.name
                binding.textTipoStrumento.text = vincolo.tipo
                binding.textBanca.text = bank.name
            } else {
                // Caso A: Strumento con nome specifico
                binding.textNome.text = vincolo.nome
                binding.textTipoStrumento.text = if (!vincolo.strumentoDettaglio.isNullOrEmpty()) {
                    vincolo.strumentoDettaglio.replace("Titoli di Stato", "TdS")
                } else {
                    vincolo.tipo.replace("Titoli di Stato", "TdS")
                }
                binding.textBanca.text = "${bank.name} - ${account.name}"
            }
            
            binding.textImporto.text = currencyFormatter.format(vincolo.importo)
            
            // Gestione riga extra "Investito fino ad ora" per PAC e % Performance
            if (InstrumentUtils.isIncremental(vincolo.tipo, vincolo.strumentoDettaglio) && vincolo.quotaVariazione != 0.0) {
                binding.textInvestitoTotale.visibility = View.VISIBLE
                binding.textInvestitoTotale.text = "Investito fino ad ora: ${currencyFormatter.format(vincolo.quotaVariazione)}"
                
                // Calcolo % Performance
                val diff = vincolo.importo - vincolo.quotaVariazione
                val percent = (diff / vincolo.quotaVariazione) * 100
                binding.textPerformance.visibility = View.VISIBLE
                val sign = if (percent >= 0) "+" else ""
                binding.textPerformance.text = String.format(Locale.ITALY, "( $sign%.2f%% )", percent)
                binding.textPerformance.setTextColor(if (percent >= 0) 0xFF4CAF50.toInt() else 0xFFF44336.toInt())
            } else {
                binding.textInvestitoTotale.visibility = View.GONE
                binding.textPerformance.visibility = View.GONE
            }

            // Impostazione colore banca sulla banda verticale
            binding.viewColorIndicator.setBackgroundColor(bank.color)
            
            if (isContoLibero || vincolo.durataMesi <= 0) {
                binding.textScadenza.visibility = View.GONE
            } else {
                binding.textScadenza.visibility = View.VISIBLE
                binding.textScadenza.text = "Scadenza: ${dateFormatter.format(dataScadenza)}"
            }
            
            binding.textCodiceVincolo.text = String.format(Locale.ITALY, "%02d", vincolo.codiceVincolo)

            // Gestione visibilità rigo Cedole
            val isBFP = vincolo.strumentoDettaglio == "BFP"
            binding.layoutCedolaInfo.visibility = if ((vincolo.periodoCedolaMesi > 0 || isContoLibero) && !isBFP) View.VISIBLE else View.GONE

            // Feedback visivo per eliminati
            binding.root.alpha = if (vincolo.isDeleted || account.isDeleted || bank.isDeleted) 0.5f else 1.0f

            if (isEffettivamenteScaduto) {
                binding.textStatus.text = "VINCOLO SCADUTO"
                binding.textStatus.setTextColor(0xFFF44336.toInt()) // Rosso
                binding.textDataCedola.text = "Cedole terminate"
                binding.textImportoCedola.text = "Ced. Netta: ${currencyFormatter.format(0.0)}"
            } else if (isFuturo) {
                binding.textStatus.text = "STRUMENTO NON ATTIVO"
                binding.textStatus.setTextColor(0xFFFFEB3B.toInt()) // Giallo
                calcolaValoriScheda(vincolo, now, calScadenza, dataScadenza)
            } else {
                binding.textStatus.text = ""
                calcolaValoriScheda(vincolo, now, calScadenza, dataScadenza)
            }

            binding.btnView.setOnClickListener { onViewClick(item) }
            binding.btnEdit.visibility = if (vincolo.isDeleted) View.GONE else View.VISIBLE
            binding.btnEdit.setOnClickListener { onEditClick(item) }
            binding.btnDuplicate.visibility = if (vincolo.isDeleted) View.GONE else View.VISIBLE
            binding.btnDuplicate.setOnClickListener { onDuplicateClick(item) }
            
            binding.btnDelete.setImageResource(if (vincolo.isDeleted) android.R.drawable.ic_menu_revert else R.drawable.ic_delete_outline)
            binding.btnDelete.setOnClickListener { onDeleteClick(item) }

            binding.btnHardDelete.visibility = if (vincolo.isDeleted || isEffettivamenteScaduto) View.VISIBLE else View.GONE
            binding.btnHardDelete.setOnClickListener { onPermanentDeleteClick(item) }
        }

        private fun calcolaValoriScheda(vincolo: Vincolo, now: Calendar, calScadenza: Calendar, dataScadenza: Date) {
            val calInizioPeriodo = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
            val calFinePeriodo = Calendar.getInstance().apply { timeInMillis = vincolo.dataDecorrenza }
            
            if (vincolo.tipo == "Conto Corrente" || vincolo.tipo == "Conto Deposito Libero") {
                // Per i conti liberi usiamo il valore pre-calcolato passato nel campo interessiMaturatiPrecedenti
                val interessiPeriodo = vincolo.interessiMaturatiPrecedenti

                binding.textDataCedola.text = "Tasso Interesse: ${vincolo.tassoVincolo}%"
                binding.textImportoCedola.text = "Interessi Netti: ${currencyFormatter.format(interessiPeriodo)}"
                return
            }

            if (vincolo.periodoCedolaMesi > 0) {
                calFinePeriodo.add(Calendar.MONTH, vincolo.periodoCedolaMesi)
                while (!calFinePeriodo.after(now) && calFinePeriodo.before(calScadenza)) {
                    calInizioPeriodo.timeInMillis = calFinePeriodo.timeInMillis
                    calFinePeriodo.add(Calendar.MONTH, vincolo.periodoCedolaMesi)
                }
                if (calFinePeriodo.after(calScadenza)) {
                    calFinePeriodo.time = dataScadenza
                }
            } else {
                calFinePeriodo.time = dataScadenza
            }

            val cedolaNetta = if (vincolo.tipo == "Conto Deposito") {
                val diffMillis = calFinePeriodo.timeInMillis - calInizioPeriodo.timeInMillis
                val gg = (diffMillis / (24 * 60 * 60 * 1000)).toDouble()
                val lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * gg) / 365.0
                lordo * (1.0 - vincolo.tassazione)
            } else {
                // Titoli di Stato, Obbligazioni e altri strumenti a cedola fissa
                val mesiPeriodo = if (vincolo.periodoCedolaMesi > 0) vincolo.periodoCedolaMesi.toDouble() else vincolo.durataMesi.toDouble()
                val lordo = (vincolo.importo * (vincolo.tassoVincolo / 100.0) * mesiPeriodo) / 12.0
                lordo * (1.0 - vincolo.tassazione)
            }

            binding.textDataCedola.text = "Prox. Cedola: ${dateFormatter.format(calFinePeriodo.time)}"
            binding.textImportoCedola.text = "Ced. Netta: ${currencyFormatter.format(cedolaNetta)}"
        }
    }

    class VincoloDiffCallback : DiffUtil.ItemCallback<VincoloWithFullInfo>() {
        override fun areItemsTheSame(oldItem: VincoloWithFullInfo, newItem: VincoloWithFullInfo): Boolean {
            return oldItem.vincolo.id == newItem.vincolo.id
        }

        override fun areContentsTheSame(oldItem: VincoloWithFullInfo, newItem: VincoloWithFullInfo): Boolean {
            return oldItem == newItem
        }
    }
}
