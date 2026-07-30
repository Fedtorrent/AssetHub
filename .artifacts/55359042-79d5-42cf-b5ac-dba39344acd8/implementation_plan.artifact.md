# Implementation Plan: Rename "Salto della Quaglia" to "Salto della Staffa"

L'obiettivo è rinominare la funzionalità "Salto della Quaglia" in "Salto della Staffa" in tutta l'applicazione, sia a livello di interfaccia utente (UI) che a livello di codice sorgente, per garantire coerenza e riflettere il nuovo nome scelto.

## Proposti Cambiamenti

### 1. Rinominazione File e Classi
- **File**: `SaltoQuagliaFragment.kt` ➔ `SaltoStaffaFragment.kt`
- **File**: `fragment_salto_quaglia.xml` ➔ `fragment_salto_staffa.xml`
- **Classe**: `SaltoQuagliaFragment` ➔ `SaltoStaffaFragment`
- **Binding**: `FragmentSaltoQuagliaBinding` ➔ `FragmentSaltoStaffaBinding`

### 2. Aggiornamento Interfaccia Utente (UI)
#### [MODIFY] [fragment_utility.xml](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/res/layout/fragment_utility.xml)
- Titolo della card: "Salto della Quaglia" ➔ "Salto della Staffa"
- ID della card: `card_salto_quaglia` ➔ `card_salto_staffa`

#### [MODIFY] [UtilityFragment.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/UtilityFragment.kt)
- Titolo del dialogo informativo: "Il Salto della Quaglia" ➔ "Il Salto della Staffa"
- Testo descrittivo all'interno di `mostraInfoSalto()` per riflettere il nuovo nome.

#### [MODIFY] [WelcomeActivity.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/WelcomeActivity.kt)
- Aggiornamento della descrizione della guida nella sezione Utility.

### 3. Navigazione e Logica
#### [MODIFY] [nav_graph.xml](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/res/navigation/nav_graph.xml)
- ID destinazione: `navigation_salto_quaglia` ➔ `navigation_salto_staffa`
- ID azione: `action_utility_to_salto_quaglia` ➔ `action_utility_to_salto_staffa`
- Label: "Salto della Quaglia" ➔ "Salto della Staffa"

#### [MODIFY] [MainActivity.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/MainActivity.kt)
- Aggiornamento dei riferimenti agli ID di navigazione per il colore della Toolbar e la sincronizzazione del menu.

---

## Dove modificare le info del Salto della Staffa
Come richiesto, ecco dove puoi trovare e modificare il testo informativo:
- **Dialogo Informativo**: Si trova in `UtilityFragment.kt` nel metodo `mostraInfoSalto()`.
- **Descrizione Card**: Si trova in `fragment_utility.xml` sotto la card con il titolo.
- **Guida Iniziale**: Si trova in `WelcomeActivity.kt` nella lista delle `WelcomePage`.

---

## Piano di Verifica

### Verifica Tecnica
- Compilazione del progetto per assicurarsi che tutti i riferimenti siano stati aggiornati correttamente.
- Test della navigazione dalla sezione Utility alla nuova pagina "Salto della Staffa".

### Verifica Manuale
1. Aprire la sezione **Utility**.
2. Verificare che il titolo della card sia **"Salto della Staffa"**.
3. Cliccare sull'icona info e verificare che il titolo del dialogo sia **"Il Salto della Staffa"**.
4. Aprire la **Guida** dalle impostazioni e verificare che nella slide Utility il nome sia aggiornato.
