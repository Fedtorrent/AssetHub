# Walkthrough: Raffinamento Analitico dei Grafici di Andamento

Abbiamo evoluto i grafici di andamento per renderli uno strumento di analisi reale, eliminando le visualizzazioni superflue e potenziando il dettaglio per gli investimenti.

## Modifiche Effettuate

### 1. Focus sul Valore Quota (PAC/ETF)
- **Cambiamento di Paradigma**: Per strumenti basati su quote (ETF, Azioni, Crypto, ecc.), il grafico non mostra più la valorizzazione totale (che cresce artificialmente a ogni versamento), ma l'andamento del **Prezzo Unitario**.
- **Analisi del Mercato**: Questo ti permette di vedere la vera "curva" del mercato e capire se il titolo sta guadagnando o perdendo valore nel tempo.
- **Alta Precisione**: L'asse Y per questi strumenti ora mostra **3 cifre decimali** (es. `105,425 €`), ideale per monitorare anche piccole variazioni.
- **Titolo Dinamico**: La sezione viene rinominata automaticamente in **"Andamento valore quota"**.

### 2. Pulizia dell'Interfaccia (Rimozione Grafici Inutili)
- Abbiamo rimosso il grafico per tutti gli strumenti a **capitale fisso** (BTP, Titoli di Stato, Obbligazioni, Conti Deposito, BFP).
- **Perché?** In questi strumenti il capitale non cambia nel tempo, quindi il grafico risulterebbe in una linea piatta poco informativa. Ora la card non appare del tutto, lasciando la pagina più pulita.

### 3. Continuità per Conti e Asset
- Per **Conti Correnti**, **Fondi Pensione** e **Immobili**, il grafico continua a tracciare l'andamento del **Saldo Totale** (con 2 decimali), permettendoti di monitorare la tua liquidità e il valore dei tuoi asset nel tempo.

## Risultato
L'app è ora più intelligente: ti mostra i grafici solo dove servono e con i dati più utili per quel tipo di investimento.

## Verifica
1. Apri un **ETF (es. VWCE)**: verifica che il grafico mostri i prezzi unitari con 3 decimali e il titolo "Andamento valore quota".
2. Apri un **Conto Corrente**: verifica che il grafico mostri il saldo con 2 decimali.
3. Apri un **BTP o un BFP**: verifica che la sezione del grafico sia sparita, rendendo la pagina più compatta.
