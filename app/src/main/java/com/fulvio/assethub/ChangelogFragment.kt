package com.fulvio.assethub

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fulvio.assethub.databinding.FragmentChangelogBinding

class ChangelogFragment : Fragment() {

    private var _binding: FragmentChangelogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangelogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logText = """
            <font color='#448AFF'><b>VERSIONE 1.3.1</b></font><br/>
            • <b>Backup Avanzato</b>: Il sistema di salvataggio ora include anche le tue personalizzazioni delle Impostazioni (mesi visualizzazione, filtri, privacy).<br/>
            • <b>Stabilità Icone</b>: Risolto un problema tecnico che causava la visualizzazione di icone errate nei Link Utili dopo l'aggiornamento dell'app.<br/>            
            • <b>Data Ultimo Aggiornamento</b>: Inserita la data "Ultimo Agg. gg/mm/aaaa" sotto gli importi nelle schede Banche, Conti e Prodotti (escludendo automaticamente le date future).<br/>
            • <b>Gestione Quotazioni Strumenti</b>: Migliorata la visualizzazione dei movimenti per ETF, ETC ed ETN mostrando quote e prezzo anche quando si effettuano aggiornamenti di sola quotazione (0 quote).<br/>
            • <b>Pulizia Lista Prodotti</b>: Rimossi i conti di base (Conti Correnti, Deposito, Fondi Pensione) dalla lista dei prodotti finanziari per evitare voci duplicate.<br/>
            • <b>Ordinamento per Banca</b>: Aggiunto il pulsante di cambio ordinamento (da alfabetico a per banca e viceversa) con icona dedicata nella barra superiore delle pagine Conti e Strumenti.<br/>
            <br/>
            <font color='#448AFF'><b>VERSIONE 1.3</b></font><br/>
            • <b>Trend Patrimonio</b>: Nuova sezione con grafici storici del patrimonio Totale e Mobiliare, con aggiornamento in tempo reale e storico degli ultimi 9 mesi.<br/>
            • <b>Raggruppamento Intelligente</b>: Risolto un problema che causava la duplicazione di alcuni conti nel Cruscotto in presenza di etichette storiche differenti.<br/>
            • <b>Salto della Staffa</b>: Rinominata la funzionalità "Salto della Quaglia" in "Salto della Staffa" con una descrizione più dettagliata e tecnica del funzionamento.<br/>
            • <b>Ottimizzazione Sicurezza</b>: La protezione privacy non viene più attivata erroneamente durante le operazioni di backup e ripristino, rendendo il salvataggio dei dati più fluido.<br/>
            • <b>Miglioramenti UI</b>: Perfezionata la terminologia nell'elenco movimenti per una maggiore chiarezza tra saldi e investimenti.<br/>
            <br/>
            <font color='#448AFF'><b>VERSIONE 1.2</b></font><br/>
            • <b>Doppia Analisi Asset</b>: Introdotti due nuovi grafici nel Cruscotto per distinguere la distribuzione macro (Asset Class: Liquidità, Immobiliare, ecc.) dal dettaglio specifico degli Investimenti (ETF, Azioni, BTP).<br/>
            • <b>Ottimizzazione Liste</b>: Perfezionata la visualizzazione della distribuzione per strumento con indicazione delle percentuali e testi più ordinati.<br/>
            • <b>Privacy Totale</b>: Disabilitato il backup automatico sul cloud di Google per garantire che i tuoi dati finanziari rimangano esclusivamente all'interno del dispositivo.<br/>
            • <b>Backup Completo</b>: Il sistema di salvataggio ora include i Link Utili personalizzati e garantisce il ripristino automatico dei link predefiniti dai vecchi backup.<br/>
            • <b>Tema Scuro Predefinito</b>: Forzata la modalità scura per l'intera app per garantire la massima leggibilità di tutti i campi, indipendentemente dalle impostazioni di sistema.<br/>
            • Risolti bug minori.<br/>
            <br/>
            <font color='#448AFF'><b>VERSIONE 1.1</b></font><br/>
            • <b>Link Utili Dinamici</b>: Ora puoi aggiungere, personalizzare ed eliminare i tuoi collegamenti finanziari preferiti direttamente dal database.<br/>
            • <b>Protezione Privacy Background</b>: Nuova opzione per oscurare l'anteprima dell'app nell'elenco delle app recenti e richiedere lo sblocco al rientro.<br/>
            • <b>Espansione Asset Personali</b>: Aggiunte nuove categorie per mappare con precisione Contanti, Veicoli, Gioielli e altri oggetti di valore.<br/>
            • <b>Ordinamento Prioritario</b>: La banca "Asset Personali" è ora ancorata stabilmente in cima alla lista per un accesso immediato.<br/>
            • <b>Importazione Robusta</b>: Migliorato il sistema di ripristino backup per gestire correttamente file modificati manualmente (es. con Excel).<br/>
            • <b>Test Avvisi Istantaneo</b>: Ottimizzato il pulsante di test nelle impostazioni per fornire un feedback immediato e verificare la corretta configurazione del sistema.<br/>
            • <b>Miglioramenti Grafici</b>: Perfezionato l'ordine delle voci nel Cruscotto e aggiornate le icone ufficiali per i link finanziari.<br/>
            <br/>
            <font color='#448AFF'><b>VERSIONE 1.0</b></font><br/>
            • Rilascio iniziale dell'applicazione.<br/>
            • Gestione completa di Banche, Conti e Strumenti.<br/>
            • Cruscotto riepilogativo con grafici interattivi.<br/>
            • Calcolo automatico interessi, bolli e valorizzazione PAC.<br/>
            • Sistema di sicurezza tramite PIN e Biometria.
        """.trimIndent()

        binding.textChangelogContent.text = Html.fromHtml(logText, Html.FROM_HTML_MODE_LEGACY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
