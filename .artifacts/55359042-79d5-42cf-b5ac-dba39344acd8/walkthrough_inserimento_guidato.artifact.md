# Walkthrough: Inserimento Dati Guidato e Sequenziale

Abbiamo trasformato la pagina di inserimento strumenti in un percorso guidato, dove i campi appaiono progressivamente in base alle scelte effettuate.

## Nuova Logica di Visualizzazione

### 1. Inserimento da Barra di Navigazione
- **Stato Iniziale**: L'utente vede solo il campo "Seleziona Banca". Tutto il resto è nascosto.
- **Passaggio 1 (Banca)**: Scegliendo la banca, appare il campo "Seleziona Conto" (filtrato per quella banca).
- **Passaggio 2 (Conto)**: Scegliendo il conto:
    - Se è un **Conto Titoli**, appare il campo "Tipo Strumento".
    - Se è un altro tipo (es. **Conto Corrente**), appaiono direttamente i campi del modello corrispondente (Data, Saldo, ecc.).
- **Passaggio 3 (Tipo Strumento)**: Scegliendo la tipologia (es. ETF), appaiono i campi finali e il pulsante **Salva**.

### 2. Inserimento Rapido da Conto (Icona +)
- I campi **Banca** e **Conto** appaiono già pre-compilati e sono in **sola visione** (disabilitati) per garantire coerenza.
- Il flusso riprende automaticamente dal **Passaggio 3** (Tipo Strumento) o mostra subito i campi se il modello è già determinato.

## Caratteristiche Tecniche
- **Reset Intelligente**: Cambiando una scelta "a monte" (es. cambiando la Banca dopo aver scelto il Conto), il sistema nasconde e pulisce automaticamente tutti i campi successivi per evitare errori di compilazione.
- **Pulizia UI**: Il pulsante "Salva" è nascosto fino a quando il percorso non è completato, assicurando che tutti i dati necessari siano stati presentati all'utente.

## Verifica Effettuata
- [x] Flusso sequenziale correttamente implementato.
- [x] Gestione sola lettura per inserimento da icona "+".
- [x] Scomparsa e reset dei campi al cambio banca/conto.
