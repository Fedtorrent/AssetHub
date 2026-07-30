# Walkthrough: Restyling Schede Strumenti a Tre Righe

Abbiamo riorganizzato completamente il layout delle schede nella lista strumenti per migliorarne la leggibilità e dare maggiore risalto ai nomi degli asset e dei conti.

## Modifiche Effettuate

### 1. Nuova Struttura Gerarchica (Layout)
Le informazioni sono ora distribuite su tre righe distinte invece di due:
- **Riga 1**: Nome primario (Strumento o Conto) a sinistra e Importo totale a destra. Entrambi in grassetto e con dimensioni maggiori (16sp).
- **Riga 2**: Tipo di strumento (es. ETF, Conto Corrente) a sinistra e Performance % a destra.
- **Riga 3**: Riferimento bancario (Banca o Banca - Conto) in un colore grigio discreto.

### 2. Logica Dinamica Intelligente
L'app ora adatta il contenuto delle righe in base alla tipologia di dato:

- **Per gli Strumenti (es. VWCE, BTP)**:
    - **Riga 1**: "VWCE" (Nome Strumento)
    - **Riga 2**: "ETF" (Tipo, senza parentesi)
    - **Riga 3**: "Fineco - Portafoglio Core" (Banca - Conto)

- **Per i Conti Generici (es. Saldo)**:
    - **Riga 1**: "Conto XME" (Nome del Conto)
    - **Riga 2**: "Conto Corrente" (Tipo)
    - **Riga 3**: "Intesa Sanpaolo" (Banca)

## Risultato
Le schede risultano molto più ordinate e professionali. Il nome dell'asset (che è l'informazione più importante) è immediatamente visibile in alto, mentre i dettagli tecnici e bancari sono separati e facili da consultare.

## Verifica
1. Apri la **Lista Strumenti**.
2. Verifica che per un **ETF** appaia il nome (es. VWCE) in grassetto al primo rigo e il dettaglio della banca al terzo.
3. Verifica che per un **Conto Corrente** appaia il nome del conto (es. Conto XME) al primo rigo e la banca al terzo.
4. Controlla che la **Performance %** sia allineata a destra nel secondo rigo.
