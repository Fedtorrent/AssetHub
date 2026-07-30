# Walkthrough: Introduzione del "Conto Deposito Libero"

Abbiamo implementato una nuova tipologia di conto "ibrida" che combina la flessibilità operativa di un Conto Corrente con il regime fiscale di un Conto Deposito.

## Modifiche Effettuate

### 1. Nuova Categoria di Sistema
- Abbiamo aggiunto la categoria **"Conto Deposito Libero"** nel database e nel sistema di seeding.
- Al primo avvio, l'app aggiungerà automaticamente questa voce alla lista delle tipologie selezionabili.
- Il colore predefinito assegnato è il **Ciano** (`#00BCD4`).

### 2. Logica di Calcolo Ibrida
- **Interessi (stile Conto Corrente)**: Il conto viene gestito tramite storico movimenti (versamenti e prelievi). Gli interessi vengono calcolati giorno per giorno sul saldo effettivo presente sul conto.
- **Imposta di Bollo (stile Conto Deposito)**: A differenza dei conti correnti (quota fissa 34,20€), per questo conto l'app calcola automaticamente lo **0,2% sul capitale** presente al 31/12, senza soglie di esenzione.
- **Patrimonio**: Nel Cruscotto, il saldo di questi conti viene sommato alla voce **"Liberi"**, riflettendo la possibilità di movimentare le somme senza vincoli temporali.

### 3. Integrazione nell'Interfaccia (UI)
- **Inserimento**: Le maschere di creazione conto e aggiunta movimento sono state aggiornate per mostrare i campi corretti (Tasso, Rendicontazione, ecc.) per questa nuova categoria.
- **Schede e Dettagli**: Le schede seguono la logica "intelligente" a 3 righe (Nome Conto in evidenza, Banca al terzo rigo) e le pagine di dettaglio mostrano il riepilogo del guadagno netto ad oggi.

## Risultato
Ora puoi tracciare quei prodotti bancari (molto comuni oggi) che offrono tassi d'interesse flessibili su base giornaliera ma che sono legalmente classificati come conti deposito ai fini del bollo statale.

## Verifica
1. Crea un nuovo Account scegliendo **"Conto Deposito Libero"**.
2. Aggiungi un movimento di saldo iniziale.
3. Apri il **Dettaglio Strumento**: verifica che la voce **"Imposta di Bollo"** riporti lo 0,2% del capitale e che compaia la riga **"Interessi ad oggi"**.
4. Verifica nel **Cruscotto** che la cifra contribuisca al totale dei fondi **"Liberi"**.
