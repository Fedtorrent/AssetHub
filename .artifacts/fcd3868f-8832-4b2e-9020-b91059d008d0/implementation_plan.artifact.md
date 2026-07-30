# Fix unresolved reference 'getVincoloByCodice'

The build is failing because `VincoliRepository` calls `vincoloDao.getVincoloByCodice(codice)`, but this method is not defined in the `VincoloDao` interface.

## Proposed Changes

### :app

#### [MODIFY] [VincoloDao.kt](file:///F:/04.PersonalApp/I_Miei_Vincoli/app/src/main/java/com/fulvio/imieivincoli/VincoloDao.kt)

- Add `getVincoloByCodice` method with `@Transaction` and `@Query("SELECT * FROM vincoli WHERE codiceVincolo = :codice")`.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:compileDebugKotlin` to verify the build succeeds.
