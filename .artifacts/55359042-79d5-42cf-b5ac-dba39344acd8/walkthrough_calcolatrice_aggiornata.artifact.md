# Walkthrough: Calcolatrice Interessi Aggiornata

Abbiamo potenziato la Calcolatrice Interessi aggiungendo il calcolo del tasso netto e ottimizzando lo spazio sullo schermo.

## Modifiche Effettuate

### 1. Nuovo Calcolo: Tasso Annuo Netto
- Ora, oltre al guadagno monetario (lordo e netto), la calcolatrice mostra anche il **Tasso Annuo Netto (%)**.
- Questo valore permette di capire immediatamente l'impatto reale della tassazione sul rendimento lordo proposto.
- Il calcolo segue la formula: `Tasso Lordo * (1 - Tassazione)`.

### 2. Ottimizzazione del Layout
- Abbiamo ridotto i margini verticali tra i campi di inserimento (Capitale, Tasso, Durata).
- È stato ridotto anche lo spazio sopra il pulsante "Calcola".
- Queste modifiche permettono di visualizzare l'intero modulo e i risultati senza dover scorrere la pagina sulla maggior parte dei dispositivi.

## Risultato
L'utility è ora più informativa e compatta, rispondendo alle domande più comuni degli utenti sul rendimento reale degli investimenti.

## Verifica
1. Vai nella sezione **Utility** -> **Calcolatrice Interessi**.
2. Inserisci i dati e premi **Calcola**.
3. Verifica la comparsa della riga **Tasso Annuo Netto** sotto il Guadagno Netto.
4. Controlla che i campi siano più vicini tra loro e che non sia necessario scorrere per vedere i risultati.
