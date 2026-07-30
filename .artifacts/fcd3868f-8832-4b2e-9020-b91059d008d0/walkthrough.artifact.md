# Fix Unresolved Reference 'getVincoloByCodice'

I have fixed the unresolved reference error by adding the missing `getVincoloByCodice` method to the `VincoloDao` interface.

## Changes Made

### :app

- **[VincoloDao.kt](file:///F:/04.PersonalApp/I_Miei_Vincoli/app/src/main/java/com/fulvio/imieivincoli/VincoloDao.kt)**: Added `getVincoloByCodice(codice: Int)` with `@Transaction` and `@Query` annotations.

```kotlin
    @Transaction
    @Query("SELECT * FROM vincoli WHERE codiceVincolo = :codice")
    suspend fun getVincoloByCodice(codice: Int): VincoloWithAccount?
```

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` and it passed successfully.
