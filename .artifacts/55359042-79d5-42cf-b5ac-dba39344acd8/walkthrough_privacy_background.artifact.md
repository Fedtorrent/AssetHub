# Walkthrough: Protezione Privacy e Sicurezza Background

Abbiamo implementato una nuova funzione avanzata di privacy per proteggere i tuoi dati sensibili quando non stai usando attivamente l'applicazione.

## Modifiche Effettuate

### 1. Oscuramento Anteprima nei "Recenti"
Abbiamo attivato il supporto al flag di sicurezza di Android (`FLAG_SECURE`).
- **Come funziona**: Quando la protezione è attiva, se metti l'app in background (per passare ad un'altra app), il sistema Android **oscurerà completamente l'anteprima** della schermata nell'elenco delle app recenti.
- **Vantaggio**: Nessuno potrà sbirciare i tuoi saldi o i tuoi investimenti semplicemente guardando il multitasking del tuo telefono.

### 2. Blocco Automatico al Rientro
Abbiamo introdotto un osservatore del ciclo di vita dell'applicazione ([AssetHubApp.kt](file:///F:/04.PersonalApp/Asset_Hub/app/src/main/java/com/fulvio/assethub/AssetHubApp.kt)).
- **Comportamento**: Se l'app viene messa in background e poi riaperta, il sistema rileva il rientro in primo piano e **richiede immediatamente l'autenticazione** (PIN o Biometria) prima di mostrare qualsiasi dato.
- **Continuità**: La sessione viene "bloccata" istantaneamente, garantendo che i dati rimangano privati anche se lasci il telefono sbloccato con l'app aperta.

### 3. Nuova Opzione nelle Impostazioni
Nella sezione "Sicurezza" delle **Impostazioni**, troverai ora lo switch **"Protezione Background"**.
- Puoi attivare o disattivare questa funzione in qualsiasi momento.
- La protezione richiede che sia già impostato un PIN o la biometria per funzionare.

## Risultato
Asset Hub è ora protetto non solo all'avvio, ma durante tutto l'utilizzo quotidiano. La tua privacy è garantita anche contro sguardi indiscreti durante il passaggio tra un'app e l'altra.

## Verifica
1. Vai in **Impostazioni** -> **Sicurezza**.
2. Attiva lo switch **"Protezione Background"**.
3. Torna nel **Cruscotto** e premi il tasto "Home" (o fai lo swipe) per mettere l'app in background.
4. Apri l'elenco delle **app recenti**: verifica che il riquadro di Asset Hub sia **completamente oscurato**.
5. Riapri l'app e verifica che ti venga chiesto il **PIN** o la **Biometria** per entrare.
