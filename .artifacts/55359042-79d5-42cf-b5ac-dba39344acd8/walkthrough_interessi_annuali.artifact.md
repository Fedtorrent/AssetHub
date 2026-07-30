# Walkthrough: Calcolo Interessi Annuali Maturati

Abbiamo aggiunto una nuova metrica finanziaria nelle schermate di dettaglio per i conti a saldo variabile, permettendoti di monitorare il rendimento reale accumulato dall'inizio dell'anno.

## Modifiche Effettuate

### 1. Nuova Logica di Calcolo Finanziario
Abbiamo creato [InterestUtils.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/InterestUtils.kt) per gestire il calcolo degli interessi giorno per giorno.
- **Ricostruzione Storica**: Il sistema analizza ogni variazione di saldo e di tasso avvenuta dal **1° Gennaio** ad oggi.
- **Calcolo Preciso**: Per ogni periodo tra due variazioni, viene calcolato l'interesse maturato usando la formula: `(Saldo * Tasso * Giorni) / 365`.
- **Ritenuta Fiscale**: Viene applicata automaticamente la tassazione del **26%** per mostrare solo il guadagno effettivo.

### 2. Aggiornamento Interfaccia Dettaglio
- Nella card **"Dati strumento"** dello Storico Asset, è stata aggiunta la voce **"Interessi fino ad oggi (netto 26%)"**.
- Questa riga compare solo per i **Conti Corrente** e i **Conti Deposito Liberi**.
- Il valore si aggiorna istantaneamente ogni volta che aggiungi o modifichi un movimento.

## Risultato
Ora hai una risposta immediata alla domanda: "Quanto ho guadagnato effettivamente da questo conto dall'inizio dell'anno?". L'app fa tutto il lavoro sporco di ricalcolare i periodi per te.

## Verifica
1. Apri lo **Storico** di un Conto Corrente o Conto Deposito Libero.
2. Espandi la sezione **"Dati strumento"**.
3. Verifica la presenza della riga **"Interessi fino ad oggi (netto 26%)"** con il valore calcolato.
4. Prova a inserire un nuovo saldo con un tasso diverso: vedrai il valore aggiornarsi coerentemente.
