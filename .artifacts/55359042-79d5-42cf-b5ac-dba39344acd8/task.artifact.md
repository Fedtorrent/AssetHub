# Task: Ottimizzazione Privacy durante Backup

- [x] **Gestione Stato (AssetHubApp.kt)**
    - [x] Aggiungere flag statico `ignoreNextForegroundBlock`
    - [x] Implementare controllo del flag nel `LifecycleObserver`
- [x] **Integrazione UI (ImpostazioniFragment.kt)**
    - [x] Impostare flag a `true` prima dell'esportazione
    - [x] Impostare flag a `true` prima dell'importazione
- [x] **Verifica finale**
    - [x] Test rientro da salvataggio file senza blocco PIN
    - [x] Verifica che il blocco funzioni ancora per il background manuale
