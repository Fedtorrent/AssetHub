# Walkthrough: Centralizzazione Bollo Statale sul Conto

Abbiamo spostato la gestione del Bollo Statale dal singolo strumento (ETF, Obbligazione, ecc.) all'anagrafica del **Conto**, automatizzando il calcolo in base alle regole fiscali vigenti.

## Modifiche Effettuate

### 1. Database (Migrazione v14)
- La tabella `accounts` ora include la colonna `bolloCaricoBanca`.
- Implementata la migrazione automatica da v13 a v14 per preservare i dati.

### 2. Gestione Conti (Account)
- In [AddAccountFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/AddAccountFragment.kt), è stato aggiunto il campo **"Bollo Statale a carico Banca?"**.
- Questo campo appare per i conti di tipo **Corrente**, **Deposito** e **Titoli**.
- Se impostato su **Sì**, l'app annullerà il calcolo del bollo per tutti gli strumenti collegati a quel conto.

### 3. Semplificazione Inserimento Strumenti
- Rimosso il campo del bollo da [AddVincoloFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/AddVincoloFragment.kt). Ora non dovrai più preoccuparti di specificarlo per ogni singolo acquisto o investimento.

### 4. Logica Fiscale nel Dettaglio
- In [DettaglioVincoloFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/DettaglioVincoloFragment.kt), il calcolo del bollo ora segue queste regole se il bollo **non** è a carico banca:
    - **Conto Corrente**: 34,20€/anno (o 8,55€/trimestre) in base alla rendicontazione scelta.
    - **Titoli/Deposito**: 0,2% pro-rata sul capitale.
- Se il bollo è a carico banca, il valore visualizzato sarà **0,00€** con l'indicazione "Assolto da Banca".

## Backup & Restore
- Aggiornata l'esportazione CSV per includere l'impostazione del bollo nell'entità `ACCOUNT`, garantendo la continuità dei dati durante il ripristino.

## Verifica
1. Modifica un Conto esistente e imposta "Bollo a carico Banca: SÌ".
2. Controlla il dettaglio di un investimento collegato: vedrai il bollo azzerato.
3. Ripristina su "NO" e verifica che il calcolo (0,2% o fisso CC) riappaia correttamente.
