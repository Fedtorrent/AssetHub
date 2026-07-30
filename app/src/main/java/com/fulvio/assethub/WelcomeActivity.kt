package com.fulvio.assethub

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.text.Html
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.fulvio.assethub.databinding.ActivityWelcomeBinding
import com.fulvio.assethub.databinding.ItemWelcomePageBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var adapter: WelcomePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Applicazione Protezione Background (FLAG_SECURE)
        val securityPrefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
        val backgroundProtection = securityPrefs.getBoolean("background_protection", false)
        if (backgroundProtection) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pages = listOf(
            WelcomePage(
                R.mipmap.ic_launcher,
                "<font color='#FFFFFF'>ASSET</font> <font color='#448AFF'>HUB</font>",
                "Il centro di controllo definitivo per il tuo patrimonio. Monitora, pianifica e gestisci ogni tuo asset in un unico luogo sicuro, per avere sempre il quadro completo della tua libertà finanziaria.",
                showVersion = true,
                showTopTitle = true
            ),
            WelcomePage(
                android.R.drawable.ic_menu_today,
                "Pianifica le Entrate",
                "Visualizza nel cruscotto le prossime cedole in arrivo e i vincoli in scadenza per una perfetta pianificazione finanziaria.",
                showVersion = false,
                showTopTitle = false,
                title2 = "Analisi Grafica",
                desc2 = "Monitora la distribuzione del tuo capitale per banca, tipologia di vincolo e durata grazie a grafici interattivi.",
                imageRes2 = android.R.drawable.ic_menu_sort_by_size,
                title3 = "Massima Sicurezza",
                desc3 = "Proteggi i tuoi dati sensibili attivando l'accesso tramite PIN o autenticazione biometrica.",
                imageRes3 = android.R.drawable.ic_lock_lock
            ),
            WelcomePage(
                android.R.drawable.ic_menu_directions,
                "Logica Asset Hub",
                "Segui la gerarchia: crea prima la <b>Banca</b>, poi aggiungi un <b>Conto</b> (es. Corrente o Titoli) e infine inserisci i tuoi <b>Strumenti</b>.",
                showVersion = false,
                showTopTitle = false,
                title2 = "Inserimento Dati",
                desc2 = "Usa sempre la <b>virgola</b> per i decimali. Assicurati che la <b>Data di Decorrenza</b> sia quella reale per calcoli precisi.",
                imageRes2 = android.R.drawable.ic_menu_edit,
                title3 = "Gestione PAC ed ETF",
                desc3 = "Per i PAC, aggiungi ogni versamento come un nuovo movimento: l'app calcolerà automaticamente <b>PMC</b> e <b>valorizzazione</b>.",
                imageRes3 = android.R.drawable.ic_input_add
            ),
            WelcomePage(
                android.R.drawable.ic_menu_help,
                "Guida Rapida",
                "",
                listIcon = R.drawable.ic_bank,
                listContent = "<b>Banche:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;Inserisci i tuoi istituti di credito. Nella scheda di sistema \"Asset Personali\" puoi inserire tutto ciò che vuoi, dagli Immobili ai Contanti dalle Auto ai Gioielli. Dando un valore a questi beni vedrai il tuo reale Patrimonio.",
                listIcon2 = R.drawable.ic_wallet,
                listContent2 = "<b>Conti:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;ogni Banca può avere più Conti ed i Conti possono essere molto diversi per tipologia tra loro. Puoi inserire:<br/>" +
                        "&nbsp;&nbsp;• <b>Conto Corrente</b> - Uso quotidiano, con saldo mensile o movimenti giornalieri.<br/>" +
                        "&nbsp;&nbsp;• <b>Conto Deposito Libero</b> - Come il CC ma con bollo allo 0,2% e senza servizi di pagamento.<br/>" +
                        "&nbsp;&nbsp;• <b>Conto Deposito</b> - Vincolato o Svincolabile.<br/>" +
                        "&nbsp;&nbsp;• <b>Conto Titoli</b> - Contenitore per Azioni, ETF, TdS, ecc.<br/>" +
                        "&nbsp;&nbsp;• <b>Fondo Pensione</b> - Tracciamento saldo e andamento fondo."
            ),
            WelcomePage(
                android.R.drawable.ic_menu_help,
                "Guida Rapida",
                "",
                listIcon = R.drawable.ic_list,
                listContent = "<b>Strumenti:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;Alcuni Conti possono contenere degli Strumenti. Un Conto Deposito contiene i Vincoli, un Conto Titoli contiene TdS, ETF, Fondi, ecc.",
                listIcon2 = R.drawable.ic_dashboard_gauge,
                listContent2 = "<b>Monitoraggio:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;Per ogni strumento puoi consultare il dettaglio con il calcolo di interessi e bolli. Per gli asset volatili (ETF/Azioni), l'app genera un grafico dell'andamento basato sulla cronologia dei prezzi inseriti."
            ),
            WelcomePage(
                android.R.drawable.ic_menu_help,
                "Guida Rapida",
                "",
                listIcon = R.drawable.ic_utility,
                listContent = "<b>Utility:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;Strumenti rapidi per la tua gestione. La Calcolatrice Interessi stima guadagni lordi e netti, mentre il Salto della Staffa ti aiuta a capire se conviene spostare i fondi per evitare il bollo statale.",
                listIcon2 = R.drawable.ic_settings,
                listContent2 = "<b>Setup:</b><br/>&nbsp;&nbsp;&nbsp;&nbsp;Personalizza l'esperienza. Qui puoi gestire le notifiche per cedole e scadenze, attivare la sicurezza (PIN/Biometria) e gestire i backup dei tuoi dati locali."
            ),
            WelcomePage(
                android.R.drawable.ic_dialog_info,
                "TIPS E NOTE",
                "• Ricorda: una Banca tanti Conti; un Conto tanti Strumenti.\n\n• Calcoli Precisi: L'app usa i giorni esatti (base 365) per i Conti Deposito e la base mensile per TdS e Obbligazioni.\n\n• Imposta di Bollo: Lo 0,2% viene calcolato contando quante volte il vincolo 'attraversa' la data del 31 dicembre.\n\n• Titoli Step-Up (es. BTP Valore): Crea un vincolo separato per ogni differente tasso del periodo (il premio finale non è conteggiati).\n\n• Se vuoi solo aggiornare il valore quota di un PAC, Azione o Fondo, aggiungi un movimento con il nuovo valore quota e metti 0 (zero) nei campi Numero Quote e Importo Speso.\n\n• Backup: I dati sono solo sul tuo telefono. Esporta periodicamente un file CSV per sicurezza!",
                showVersion = false,
                showTopTitle = false
            )
        )

        adapter = WelcomePagerAdapter(pages)
        binding.viewPager.adapter = adapter
        
        setupDots(pages.size)
        updateDots(0)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
                if (position == pages.size - 1) {
                    binding.btnNext.text = "INIZIA"
                } else {
                    binding.btnNext.text = "AVANTI"
                }
            }
        })

        binding.btnNext.setOnClickListener {
            if (binding.viewPager.currentItem < pages.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishWelcome()
            }
        }

        binding.btnSkip.setOnClickListener {
            finishWelcome()
        }
    }

    private fun setupDots(size: Int) {
        binding.layoutDots.removeAllViews()
        val dots = arrayOfNulls<ImageView>(size)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in 0 until size) {
            dots[i] = ImageView(this)
            dots[i]?.setImageResource(R.drawable.ic_dot_inactive)
            binding.layoutDots.addView(dots[i], params)
        }
    }

    private fun updateDots(position: Int) {
        val childCount = binding.layoutDots.childCount
        for (i in 0 until childCount) {
            val imageView = binding.layoutDots.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageResource(R.drawable.ic_dot_active)
            } else {
                imageView.setImageResource(R.drawable.ic_dot_inactive)
            }
        }
    }

    private fun finishWelcome() {
        val context = this
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("first_run", false).apply()
        
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(context)
            val count = database.vincoloDao().getAllVincoli().first().size
            
            if (count == 0) {
                androidx.appcompat.app.AlertDialog.Builder(context)
                    .setTitle("Dati di Prova")
                    .setMessage("L\u0027app è vuota. Vuoi caricare alcuni vincoli di esempio per vedere come funzionano i grafici e il cruscotto?")
                    .setPositiveButton("Sì, Carica") { _, _ ->
                        caricaEsempiERichiudi()
                    }
                    .setNegativeButton("No, Grazie") { _, _ ->
                        richiudiActivity()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                richiudiActivity()
            }
        }
    }

    private fun caricaEsempiERichiudi() {
        lifecycleScope.launch {
            caricaDatiEsempioStatico(this@WelcomeActivity)
            richiudiActivity()
        }
    }

    private fun richiudiActivity() {
        if (isTaskRoot) {
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }

    companion object {
        suspend fun caricaDatiEsempioStatico(context: Context) {
            val database = AppDatabase.getDatabase(context)
            val dao = database.vincoloDao()
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val now = Calendar.getInstance()

            // Recupero Categorie
            val catCorrente = dao.getCategoryByType(Category.TYPE_CORRENTE)
            val catDeposito = dao.getCategoryByType(Category.TYPE_DEPOSITO)
            val catTitoli = dao.getCategoryByType(Category.TYPE_TITOLI)
            val catPensione = dao.getCategoryByType(Category.TYPE_PENSIONE)
            val catImmobili = dao.getCategoryByType(Category.TYPE_IMMOBILI)

            var lastCode = dao.getMaxCodiceVincolo() ?: 0

            // 1. INTESA SANPAOLO - Conto Corrente
            val bankIntesaId = dao.insertBank(Bank(name = "Intesa Sanpaolo", color = 0xFF4CAF50.toInt())) // Verde
            val accCorrenteId = dao.insertAccount(Account(
                name = "Conto XME",
                bankId = bankIntesaId,
                categoryId = catCorrente?.id ?: 1,
                frequenzaRendicontazione = "Trimestrale"
            ))
            dao.insertVincolo(Vincolo(
                nome = "Saldo", accountId = accCorrenteId, dataDecorrenza = now.timeInMillis,
                durataMesi = 0, svincolabile = true, importo = 2500.0, tassoVincolo = 0.5,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.26, bolloCaricoBanca = false, tipo = "Conto Corrente",
                codiceVincolo = ++lastCode, frequenzaRendicontazione = "Trimestrale"
            ))

            // 2. FINECO BANK - PAC ed ETF
            val bankFinecoId = dao.insertBank(Bank(name = "Fineco Bank", color = 0xFF0F3ADA.toInt())) // Blu
            val accTitoliId = dao.insertAccount(Account(
                name = "Portafoglio Core",
                bankId = bankFinecoId,
                categoryId = catTitoli?.id ?: 3,
                frequenzaRendicontazione = "Trimestrale"
            ))
            
            // Movimenti PAC VWCE
            val pacCode = ++lastCode
            val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }.timeInMillis
            val twoMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.timeInMillis
            val oneMonthAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
            
            dao.insertVincolo(Vincolo(
                nome = "VWCE (ETF)", accountId = accTitoliId, dataDecorrenza = threeMonthsAgo,
                durataMesi = 0, svincolabile = true, importo = 1000.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.26, bolloCaricoBanca = false, tipo = "Conto Titoli",
                strumentoDettaglio = "ETF", numeroQuote = 10.0, prezzoAcquisto = 100.0,
                quotaVariazione = 1000.0, codiceVincolo = pacCode
            ))
            dao.insertVincolo(Vincolo(
                nome = "VWCE (ETF)", accountId = accTitoliId, dataDecorrenza = twoMonthsAgo,
                durataMesi = 0, svincolabile = true, importo = 1525.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.26, bolloCaricoBanca = false, tipo = "Conto Titoli",
                strumentoDettaglio = "ETF", numeroQuote = 5.0, prezzoAcquisto = 105.0,
                quotaVariazione = 525.0, codiceVincolo = pacCode
            ))
            dao.insertVincolo(Vincolo(
                nome = "VWCE (ETF)", accountId = accTitoliId, dataDecorrenza = oneMonthAgo,
                durataMesi = 0, svincolabile = true, importo = 2075.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.26, bolloCaricoBanca = false, tipo = "Conto Titoli",
                strumentoDettaglio = "ETF", numeroQuote = 5.0, prezzoAcquisto = 110.0,
                quotaVariazione = 550.0, codiceVincolo = pacCode
            ))

            // BTP Valore
            dao.insertVincolo(Vincolo(
                nome = "BTP Valore 2028", accountId = accTitoliId, dataDecorrenza = twoMonthsAgo,
                durataMesi = 60, svincolabile = false, importo = 10000.0, tassoVincolo = 3.5,
                tassoSvincolo = 0.0, periodoCedolaMesi = 6, tassazione = 0.125, bolloCaricoBanca = false,
                tipo = "Conto Titoli", strumentoDettaglio = "Titoli di Stato",
                codiceVincolo = ++lastCode
            ))

            // 3. POSTE ITALIANE - BFP
            val bankPosteId = dao.insertBank(Bank(name = "Poste Italiane", color = 0xFFFFC107.toInt())) // Ambra
            val accLibrettoId = dao.insertAccount(Account(
                name = "Libretto Smart",
                bankId = bankPosteId,
                categoryId = catDeposito?.id ?: 2,
                frequenzaRendicontazione = "Annuale"
            ))
            dao.insertVincolo(Vincolo(
                nome = "Buono Rinnova", accountId = accLibrettoId, dataDecorrenza = oneMonthAgo,
                durataMesi = 72, svincolabile = true, importo = 5000.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.125, bolloCaricoBanca = false, tipo = "Conto Titoli",
                strumentoDettaglio = "BFP", codiceVincolo = ++lastCode
            ))

            // 4. ALLIANZ - Fondo Pensione
            val bankAllianzId = dao.insertBank(Bank(name = "Allianz", color = 0xFFFF8141.toInt())) // Arancione
            val accPensioneId = dao.insertAccount(Account(
                name = "Insieme",
                bankId = bankAllianzId,
                categoryId = catPensione?.id ?: 4,
                frequenzaRendicontazione = "Annuale"
            ))
            dao.insertVincolo(Vincolo(
                nome = "Saldo", accountId = accPensioneId, dataDecorrenza = now.timeInMillis,
                durataMesi = 0, svincolabile = true, importo = 15000.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.26, bolloCaricoBanca = false, tipo = "Fondo Pensione",
                codiceVincolo = ++lastCode
            ))

            // 5. ASSET PERSONALI - Immobili
            val allBanks = dao.getAllBanks().first()
            val existingAssetBank = allBanks.find { it.name == "Asset Personali" }
            val bankAssetId = existingAssetBank?.id ?: dao.insertBank(Bank(name = "Asset Personali", color = 0xFF607D8B.toInt())) // Grigio
            
            val accCasaId = dao.insertAccount(Account(
                name = "Appartamento Milano",
                bankId = bankAssetId,
                categoryId = catImmobili?.id ?: 5,
                frequenzaRendicontazione = "Annuale"
            ))
            dao.insertVincolo(Vincolo(
                nome = "Saldo", accountId = accCasaId, dataDecorrenza = now.timeInMillis,
                durataMesi = 0, svincolabile = true, importo = 250000.0, tassoVincolo = 0.0,
                tassoSvincolo = 0.0, periodoCedolaMesi = 0,
                tassazione = 0.0, bolloCaricoBanca = false, tipo = "Immobili",
                codiceVincolo = ++lastCode
            ))

            prefs.edit().putInt("last_codice_vincolo", lastCode).apply()
        }
    }

    data class WelcomePage(
        val imageRes: Int,
        val title: String,
        val desc: String,
        val showVersion: Boolean = false,
        val showTopTitle: Boolean = false,
        val title2: String? = null,
        val desc2: String? = null,
        val imageRes2: Int = 0,
        val title3: String? = null,
        val desc3: String? = null,
        val imageRes3: Int = 0,
        val listIcon: Int = 0,
        val listContent: String? = null,
        val listIcon2: Int = 0,
        val listContent2: String? = null
    )

    inner class WelcomePagerAdapter(private val pages: List<WelcomePage>) :
        RecyclerView.Adapter<WelcomePagerAdapter.PagerViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
            val b = ItemWelcomePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PagerViewHolder(b)
        }

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            holder.bind(pages[position])
        }

        override fun getItemCount() = pages.size

        inner class PagerViewHolder(private val b: ItemWelcomePageBinding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(page: WelcomePage) {
                b.textWelcomeTopTitle.visibility = if (page.showTopTitle) View.VISIBLE else View.GONE
                
                // Gestione Dimensioni per Pagina 1 (Logo) vs Altre (Dettagli)
                if (page.showTopTitle) {
                    b.imgWelcome.layoutParams.width = 120.dpToPx()
                    b.imgWelcome.layoutParams.height = 120.dpToPx()
                    b.textWelcomeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                    b.imgWelcome.imageTintList = null // No tint per il logo
                } else {
                    b.imgWelcome.layoutParams.width = 60.dpToPx()
                    b.imgWelcome.layoutParams.height = 60.dpToPx()
                    b.textWelcomeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                    b.imgWelcome.imageTintList = android.content.res.ColorStateList.valueOf(0xFFBBBBBB.toInt())
                }

                if (page.imageRes != 0) {
                    b.imgWelcome.visibility = View.VISIBLE
                    b.imgWelcome.setImageResource(page.imageRes)
                } else {
                    b.imgWelcome.visibility = View.GONE
                }
                
                if (page.title.contains("<font") || page.title.contains("<b>")) {
                    b.textWelcomeTitle.text = Html.fromHtml(page.title, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    b.textWelcomeTitle.text = page.title
                }
                
                if (page.desc.contains("<font") || page.desc.contains("<b>")) {
                    b.textWelcomeDesc.text = Html.fromHtml(page.desc, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    b.textWelcomeDesc.text = page.desc
                }
                b.textWelcomeVersion.visibility = if (page.showVersion) View.VISIBLE else View.GONE

                if (page.title2 != null) {
                    b.dividerWelcome.visibility = View.VISIBLE
                    b.textWelcomeTitle2.visibility = View.VISIBLE
                    b.textWelcomeDesc2.visibility = View.VISIBLE
                    
                    if (page.title2.contains("<font") || page.title2.contains("<b>")) {
                        b.textWelcomeTitle2.text = Html.fromHtml(page.title2, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        b.textWelcomeTitle2.text = page.title2
                    }

                    if (page.desc2?.contains("<font") == true || page.desc2?.contains("<b>") == true) {
                        b.textWelcomeDesc2.text = Html.fromHtml(page.desc2, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        b.textWelcomeDesc2.text = page.desc2
                    }
                    
                    if (page.imageRes2 != 0) {
                        b.imgWelcome2.visibility = View.VISIBLE
                        b.imgWelcome2.setImageResource(page.imageRes2)
                    } else {
                        b.imgWelcome2.visibility = View.GONE
                    }
                } else {
                    b.dividerWelcome.visibility = View.GONE
                    b.imgWelcome2.visibility = View.GONE
                    b.textWelcomeTitle2.visibility = View.GONE
                    b.textWelcomeDesc2.visibility = View.GONE
                }

                // Terza parte opzionale (Slide Unite)
                if (page.title3 != null) {
                    b.dividerWelcome2.visibility = View.VISIBLE
                    b.imgWelcome3.visibility = View.VISIBLE
                    b.textWelcomeTitle3.visibility = View.VISIBLE
                    b.textWelcomeDesc3.visibility = View.VISIBLE
                    
                    if (page.title3.contains("<font") || page.title3.contains("<b>")) {
                        b.textWelcomeTitle3.text = Html.fromHtml(page.title3, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        b.textWelcomeTitle3.text = page.title3
                    }

                    if (page.desc3?.contains("<font") == true || page.desc3?.contains("<b>") == true) {
                        b.textWelcomeDesc3.text = Html.fromHtml(page.desc3, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        b.textWelcomeDesc3.text = page.desc3
                    }
                    
                    if (page.imageRes3 != 0) {
                        b.imgWelcome3.setImageResource(page.imageRes3)
                    }
                } else {
                    b.dividerWelcome2.visibility = View.GONE
                    b.imgWelcome3.visibility = View.GONE
                    b.textWelcomeTitle3.visibility = View.GONE
                    b.textWelcomeDesc3.visibility = View.GONE
                }

                // Nuovo blocco: List Item
                if (page.listContent != null) {
                    b.layoutListItem.visibility = View.VISIBLE
                    b.textListContent.text = Html.fromHtml(page.listContent, Html.FROM_HTML_MODE_LEGACY)
                    if (page.listIcon != 0) {
                        b.imgListIcon.visibility = View.VISIBLE
                        b.imgListIcon.setImageResource(page.listIcon)
                    } else {
                        b.imgListIcon.visibility = View.GONE
                    }
                } else {
                    b.layoutListItem.visibility = View.GONE
                }

                // Secondo blocco: List Item
                if (page.listContent2 != null) {
                    b.layoutListItem2.visibility = View.VISIBLE
                    b.textListContent2.text = Html.fromHtml(page.listContent2, Html.FROM_HTML_MODE_LEGACY)
                    if (page.listIcon2 != 0) {
                        b.imgListIcon2.visibility = View.VISIBLE
                        b.imgListIcon2.setImageResource(page.listIcon2)
                    } else {
                        b.imgListIcon2.visibility = View.GONE
                    }
                } else {
                    b.layoutListItem2.visibility = View.GONE
                }
            }
        }
    }

    private fun Int.dpToPx() = (this * resources.displayMetrics.density).toInt()
}
