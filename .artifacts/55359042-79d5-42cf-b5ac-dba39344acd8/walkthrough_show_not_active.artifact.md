# Walkthrough: Controllo Visibilità Strumenti Non Attivi

Abbiamo aggiunto una nuova impostazione per gestire la visibilità degli strumenti con data di decorrenza futura e uniformato le etichette nelle Impostazioni.

## Modifiche Effettuate

### 1. Nuova Opzione nelle Impostazioni
- In [fragment_impostazioni.xml](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/res/layout/fragment_impostazioni.xml), nella sezione "Dati e visualizzazione", è stato aggiunto l'interruttore **"VISUAL. STRUMENTI NON ATTIVI"**.
- Tutte le scritte "VISUALIZZA" e "VISUALIZZAZIONE" sono state abbreviate in **"VISUAL."** per rendere l'interfaccia più snella.

### 2. Logica di Filtraggio
- In [ListaVincoliFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/ListaVincoliFragment.kt), abbiamo aggiornato la routine di filtraggio.
- Se l'opzione è disattivata, gli strumenti la cui **data di inizio** è successiva ad oggi (strumenti futuri) verranno nascosti dalla lista.

## Risultato
- Puoi ora decidere se vedere o meno gli strumenti che hai pianificato per il futuro ma che non sono ancora operativi.
- L'interfaccia delle impostazioni è più pulita e coerente grazie alle nuove abbreviazioni.

## Verifica
1. Vai nelle **Impostazioni** -> **Dati e visualizzazione**.
2. Verifica che le etichette siano ora tutte nel formato "VISUAL. ...".
3. Crea uno strumento con data futura e prova a nasconderlo/mostrarlo usando il nuovo switch.
