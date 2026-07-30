# Walkthrough: Gestione Avanzata ID Strumenti e Sanificazione

Abbiamo implementato una logica differenziata per la gestione degli ID strumenti (Codice Vincolo) per garantire l'unicità dove necessario e la coerenza nei gruppi PAC/ETF. Abbiamo inoltre sanato automaticamente i duplicati esistenti.

## Modifiche Effettuate

### 1. Logica Differenziata (Nuovo/Duplica)
In [AddVincoloFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/AddVincoloFragment.kt), la generazione dell'ID ora segue due percorsi distinti:
- **Strumenti Standalone (BTP, Depositi, Obbligazioni)**:
    - Ogni **nuovo inserimento** o **duplicazione** genera un **nuovo ID univoco** (`MAX + 1`).
    - L'ID originale viene mantenuto solo in caso di **modifica** diretta del record.
- **Strumenti a Storico (PAC, ETF, Azioni, CC)**:
    - La duplicazione o l'aggiunta di un movimento **mantiene l'ID del gruppo**, garantendo che tutti i versamenti appartengano allo stesso asset logico.

### 2. Routine di Sanificazione Automatica
In [MainActivity.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/MainActivity.kt), abbiamo aggiunto un processo che viene eseguito una sola volta all'avvio dell'app:
- **Correzione Duplicati Standalone**: Identifica gli strumenti (es. BTP) che condividevano erroneamente lo stesso ID (i tuoi casi "06") e assegna loro codici univoci corretti.
- **Correzione ID 00**: Converte i record rimasti con ID "00" in ID numerici validi basati sul massimo attuale.

## Risultato
- I tuoi BTP con ID duplicato verranno separati automaticamente al primo avvio.
- Duplicando un BTP, otterrai un nuovo strumento con un nuovo ID.
- Duplicando un movimento di un ETF, rimarrai all'interno dello stesso ID strumento (corretto per il raggruppamento).

## Verifica
1. Avvia l'app e controlla la lista strumenti: i duplicati "06" dovrebbero essere stati corretti.
2. Prova a duplicare un BTP: il nuovo deve avere un ID incrementato.
3. Prova a duplicare un acquisto ETF: l'ID deve restare lo stesso.
