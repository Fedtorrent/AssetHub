# Walkthrough: Gestione Dinamica dei Link Utili

Abbiamo trasformato la sezione "Link Utili" in un vero e proprio gestore di segnalibri personali, permettendoti di aggiungere, organizzare ed eliminare i tuoi collegamenti finanziari preferiti direttamente dall'app.

## Modifiche Effettuate

### 1. Evoluzione del Database (v16)
- Abbiamo introdotto una nuova tabella dedicata (`useful_links`) per memorizzare i tuoi collegamenti in modo permanente e sicuro sul dispositivo.
- **Migrazione Automatica**: Al primo avvio, l'app ha salvato nel database tutti gli 8 link che avevamo inserito precedentemente, così li troverai già pronti all'uso.

### 2. Gestione dei Link (Aggiunta ed Eliminazione)
- **Nuovo Tasto Aggiungi**: Nella barra superiore della pagina Link Utili, ora trovi l'icona **"+"**. Cliccandoci si aprirà una maschera per inserire:
    - **Titolo e Descrizione** del sito.
    - **Indirizzo Web (URL)**: con supporto al copia-incolla.
    - **Icona Personalizzata**: Puoi scegliere tra diverse icone (Banca, Portafoglio, Grafico, ecc.) o lasciare il **Globo** predefinito.
- **Eliminazione Rapida**: Ogni scheda nella lista ora dispone di un'icona **cestino** in basso a destra per rimuovere i link che non ti servono più.

### 3. Interfaccia Ottimizzata
- La lista è ora dinamica: si aggiorna istantaneamente ogni volta che aggiungi o elimini un collegamento.
- Abbiamo mantenuto lo stile arancione coerente con la sezione Utility e l'avviso di sicurezza per i siti esterni.

## Risultato
L'app non è più limitata a un elenco fisso: ora sei tu a decidere quali risorse finanziarie avere a portata di mano, costruendo la tua raccolta personalizzata di strumenti web.

## Verifica
1. Vai in **Utility** -> **Link Utili**.
2. Verifica che i link precedenti siano tutti presenti.
3. Prova ad aggiungere un nuovo link cliccando sul **"+"** in alto:
    - Inserisci un titolo e un URL (es. `https://www.google.it`).
    - Scegli un'icona differente dal globo.
    - Salva e verifica la comparsa nella lista.
4. Prova a eliminare un link cliccando sul cestino e confermando l'azione.
