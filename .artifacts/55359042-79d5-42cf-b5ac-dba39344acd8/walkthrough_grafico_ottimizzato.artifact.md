# Walkthrough: Ottimizzazione Temporale dei Grafici

Abbiamo perfezionato la logica temporale dei grafici di andamento per renderli ancora più pertinenti alla storia reale dei tuoi investimenti.

## Modifiche Effettuate

### 1. Scala Temporale Dinamica
- **Taglio Automatico**: Il grafico non parte più genericamente da 10 mesi fa, ma si adatta alla data del tuo **primo inserimento**.
- **Esempio**: Se hai aperto un conto a Gennaio 2026, il grafico inizierà da Gennaio, eliminando i mesi precedenti "vuoti" che rendevano la linea piatta e meno leggibile.
- **Limite Massimo**: Il grafico continua a mostrare al massimo gli ultimi 10 mesi per mantenere una visione recente e dettagliata.

### 2. Gestione Intelligente della Visibilità
- **Requisito Minimo**: Abbiamo impostato che il grafico appaia solo se sono presenti **almeno 2 punti** (ovvero se la storia dell'asset attraversa almeno due fine mese).
- **Perché?** Se hai appena aperto un conto o acquistato un ETF questo mese, un grafico con un solo punto sarebbe solo un puntino isolato nello spazio. Ora l'app attende che ci sia una "storia" da raccontare prima di mostrare la sezione.

## Risultato
I grafici sono ora molto più puliti e "zoommati" sulla reale attività dei tuoi asset, evitando spazi vuoti inutili all'inizio della linea.

## Verifica
1. Apri un **Conto Corrente** aperto da pochi mesi: verifica che il grafico inizi esattamente dal mese di apertura.
2. Apri uno strumento inserito **questo mese**: verifica che la card "Andamento valore" sia nascosta (riapparirà al prossimo fine mese).
3. Verifica che per i conti con storia lunga vengano mostrati comunque gli ultimi 10 mesi (limite massimo).
