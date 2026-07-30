# Walkthrough: Ottimizzazioni Inserimento e Fix UI

Abbiamo apportato una serie di miglioramenti per rendere l'inserimento dei dati più fluido e risolvere alcuni difetti visivi nelle schermate di dettaglio.

## Modifiche Effettuate

### 1. Inserimento Tasso Intelligente
- **Ereditarietà Selettiva**: Nella schermata di aggiunta movimento, se lasci il campo "Tasso" vuoto, l'app recupererà automaticamente l'ultimo tasso inserito per quel conto. Se invece inserisci esplicitamente `0`, il sistema salverà lo zero come nuovo tasso.
- **Supporto Visivo**: Abbiamo aggiunto una nota informativa (helper text) sotto il campo del tasso per spiegare questo comportamento ("Lascia vuoto per mantenere il tasso precedente").

### 2. Risoluzione Duplicati Header
- Abbiamo corretto un bug nello **Storico Asset** che causava la visualizzazione doppia delle informazioni nell'header "Dati strumento" (es. due righe per la banca, due per il saldo). Ora la pulizia della card è atomica e avviene correttamente prima di ogni aggiornamento.

### 3. Fix "Double Tap" e Sincronizzazione Frecce
- Abbiamo risolto il problema che costringeva a premere due volte per aprire le finestre collassabili all'avvio della pagina.
- Ora lo stato interno del codice e quello visivo sono perfettamente sincronizzati: il primo tocco aprirà sempre la sezione e ruoterà la freccia correttamente verso l'alto.

## Risultato
L'interazione con le schede di dettaglio è ora immediata e priva di ridondanze, mentre l'inserimento dei saldi per i conti a tasso variabile è più veloce e assistito.

## Verifica
1. **Tasso**: Crea un nuovo saldo lasciando il tasso vuoto e verifica che erediti quello precedente.
2. **Header**: Apri lo storico di un conto e verifica che le informazioni siano visualizzate una sola volta.
3. **Frecce**: Verifica che un singolo tocco apra istantaneamente le sezioni "Andamento valore" o "Dati strumento".
