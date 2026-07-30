# Walkthrough - Fix Resource Naming Issue

I have fixed the build error caused by invalid resource names (containing uppercase letters).

## Changes

### 1. Renamed Drawable Files
Renamed the following files in `app/src/main/res/drawable/` to lowercase:
- `ic_DF.png` ➔ `ic_df.png`
- `ic_CDP.png` ➔ `ic_cdp.png`
- `ic_STI.png` ➔ `ic_sti.png`
- `ic_DBerti.png` ➔ `ic_dberti.png`

### 2. Updated Code References
Updated the resource references in `app/src/main/java/com/fulvio/assethub/LinksUtiliFragment.kt` to match the new lowercase names:
- `R.drawable.ic_STI` ➔ `R.drawable.ic_sti`
- `R.drawable.ic_DBerti` ➔ `R.drawable.ic_dberti`
- `R.drawable.ic_CDP` ➔ `R.drawable.ic_cdp`
- `R.drawable.ic_DF` ➔ `R.drawable.ic_df`

## Verification Results
- **Build Verification**: Ran `./gradlew :app:packageDebugResources` and it finished successfully.
- **Resource Check**: Verified that no other files in the `res/` directory contain uppercase letters.

> [!TIP]
> Always use lowercase letters, numbers, and underscores for Android resource filenames to avoid build errors.
