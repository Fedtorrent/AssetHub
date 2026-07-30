# Asset Hub - Sintesi Stato Progetto (v12)

Questo documento riassume tutte le modifiche tecniche e le funzionalità implementate. Può essere caricato in una nuova sessione di chat per fornire immediatamente il contesto necessario.

## 1. Identità Progetto
- **Nome Ufficiale**: Asset Hub
- **Package Name**: `com.fulvio.assethub`
- **Namespace Gradle**: `com.fulvio.assethub`
- **Application ID**: `com.fulvio.assethub`

## 2. Architettura Database (v12)
L'app utilizza Room Database con una struttura a 4 entità:
- **Banks**: Gestione istituti con colori personalizzati e soft-delete.
- **Accounts**: Collegati alle banche. Includono la **Frequenza Rendicontazione** (Trimestrale/Annuale) e il **Saldo Libero**.
- **Vincoli**: I singoli movimenti/investimenti. Supportano:
    - **Logica Incrementale (PAC)**: Memoria della singola quota versata (`quotaVariazione`).
    - **Raggruppamento**: Gli strumenti con lo stesso nome vengono raggruppati automaticamente prendendo l'ultimo saldo.
    - **Filtro Temporale**: Gli strumenti con data futura o scaduti vengono esclusi dai totali attivi.
- **Categories**: Categorie di sistema (CORRENTE, DEPOSITO, TITOLI, PENSIONE, IMMOBILI).

## 3. Funzionalità Chiave Implementate
- **Sincronizzazione Colori**: La Toolbar e i pulsanti di salvataggio ereditano il colore della Banca selezionata durante tutta la navigazione.
- **Duplicazione Intelligente**: Nello storico asset, il tasto copia pre-compila l'importo dell'ultimo versamento (non il saldo totale).
- **Calcolo Patrimonio**: Algoritmo unificato in `InstrumentUtils.calculateAccountStats` per garantire coerenza tra Cruscotto, Liste e Dettagli.
- **Backup & Restore**: Routine v12 completata. Esporta e importa ogni dettaglio (incluse variazioni PAC e stati eliminati).
- **Reset Totale**: Funzione "Tabula Rasa" che elimina fisicamente il file del database e pulisce le SharedPreferences.

## 4. Note Tecniche Importanti
- **InstrumentUtils**: Contiene la logica per distinguere tra strumenti a "Saldo Assoluto" (C/C) e "Incrementali" (ETF/Azioni).
- **MainActivity**: Gestisce il cambio colore dinamico della Toolbar tramite `customColor` passato negli argomenti di navigazione.
- **AddVincoloFragment**: Gestisce titoli e campi diversi in base al contesto (Aggiungi Movimento vs Aggiungi Saldo).

## 5. Stato Backup Corrente
- L'utente ha effettuato un backup della **Versione 12**.
- In caso di reset o cambio cartella, utilizzare il file CSV per il ripristino completo.

---
*Documento generato il 21/07/2026 per la migrazione del progetto Asset Hub.*
