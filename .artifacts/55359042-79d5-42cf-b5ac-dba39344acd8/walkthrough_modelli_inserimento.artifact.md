# Walkthrough: Ristrutturazione Modelli Inserimento e UI Storico

Abbiamo completato la riorganizzazione della pagina di inserimento strumenti e migliorato la leggibilità dello storico movimenti, seguendo le tue specifiche dettagliate.

## 1. Nuova Pagina di Inserimento (Modelli)

### Selezione Intelligente Banca/Conto
- **Navigazione libera**: Se inserisci uno strumento dalla barra inferiore, ora trovi due campi a cascata: scegli prima la **Banca** e poi il **Conto** (filtrato automaticamente).
- **Da Conto specifico**: Se clicchi sul "+" da un conto, i campi Banca e Conto sono pre-compilati e bloccati (sola lettura) per evitare errori.

### Modelli Specifici per Strumento
Abbiamo rimosso la confusione dei campi generici introducendo 3 macro-modelli:
1. **Modello Quote (ETF, Azioni, BTC, ecc.)**:
    - Campi attivi: Nome, Data, N. Quote, Valore Quota, Importo Speso.
    - **Calcolo Bidirezionale**: Se inserisci Quote e Prezzo, l'Importo si calcola da solo. Se inserisci Prezzo e Importo, le Quote si calcolano automaticamente.
2. **Modello Fixed Income (Certificati, Obbligazioni)**:
    - Campi attivi: Durata, Capitale, Tasso, Cedola, Tassazione (26%/12.5%).
3. **Modello Titoli di Stato**:
    - Campi attivi: Durata, Capitale, Tasso, Cedola.
    - **Tassazione Bloccata**: Fissa al 12,5% in sola lettura.

## 2. Miglioramento Storico Movimenti

Le schede dei movimenti nello storico sono state riorganizzate per dare priorità all'operazione:
- **Riga 1**: Data e Importo della variazione (es. +1.000,00 €) in verde.
- **Riga 2**: Quote acquistate e Prezzo relativo (in colore bianco).
- **Riga 3**: Totale progressivo con etichetta **"Investito fino ad ora: "** (in verde).
- **Uniformità**: Tutto il testo ha ora una dimensione di **14sp** per una lettura più riposante.

## 3. Header Scheda Dettaglio
- Aggiunto il campo **Tipo** (es. ETF, Titolo di Stato) nell'header collassabile per una rapida identificazione dello strumento.

## Verifica
- [x] Calcolo automatico Quote <-> Importo funzionante.
- [x] Tassazione Titoli di Stato non modificabile.
- [x] Layout storico movimenti ordinato secondo le nuove specifiche.
