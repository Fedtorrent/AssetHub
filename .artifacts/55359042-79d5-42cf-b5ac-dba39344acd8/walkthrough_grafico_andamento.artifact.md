# Walkthrough: Grafico di Andamento Storico

Abbiamo introdotto una nuova funzionalità di analisi visiva nelle schermate di dettaglio: un grafico a linee che mostra l'evoluzione del valore del tuo investimento o asset negli ultimi 10 mesi.

## Modifiche Effettuate

### 1. Nuova Utility di Calcolo Temporale
Abbiamo creato [TrendUtils.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/TrendUtils.kt) per gestire la ricostruzione storica.
- Il sistema genera 10 punti corrispondenti agli ultimi 10 fine mese.
- Per ogni punto, l'app "torna indietro nel tempo" e calcola il valore esatto in base ai movimenti registrati fino a quel momento.

### 2. Integrazione nel Layout
Abbiamo aggiunto una nuova sezione collassabile denominata **"Andamento valore"** in:
- **Dettaglio Strumento** (es. BTP, Conti Deposito).
- **Storico Asset** (es. Conti Correnti, PAC, ETF).

### 3. Logica di Visualizzazione Dinamica
Il grafico si adatta automaticamente al tipo di strumento:
- **PAC ed ETF**: Mostra la crescita del valore di mercato (Quote totali * Ultimo prezzo alla data).
- **Conti Correnti**: Mostra l'evoluzione del saldo nel tempo.
- **Strumenti Fissi**: Mostra una linea stabile corrispondente al capitale investito.

## Risultato
Ora puoi monitorare non solo il valore attuale, ma anche come il tuo capitale è cambiato mese dopo mese, con una linea fluida (Cubic Bezier) e una sfumatura celeste in stile Asset Hub.

## Verifica
1. Apri lo **Storico** di un Conto Corrente o di un **ETF**.
2. Clicca sulla sezione **"Andamento valore"**.
3. Verifica che il grafico mostri i punti di fine mese con i relativi valori in Euro.
4. Osserva l'animazione della linea all'apertura della sezione.
