# Fixed Suspend Function Call in AddAccountFragment

Fixed a compilation error where a suspend function was being called from a non-suspend context in `AddAccountFragment.kt`.

## Changes Made

### [AddAccountFragment.kt](file:///F:/04.PersonalApp/I_Miei_Vincoli/app/src/main/java/com/fulvio/imieivincoli/AddAccountFragment.kt)

Wrapped the call to `viewModel.insertAccount(account)` and subsequent UI operations within `viewLifecycleOwner.lifecycleScope.launch`. This ensures the suspend function is called from a coroutine context, as required by Kotlin coroutines.

```diff
-        viewModel.insertAccount(account)
-        Toast.makeText(requireContext(), "Conto salvato!", Toast.LENGTH_SHORT).show()
-        findNavController().popBackStack()
+        viewLifecycleOwner.lifecycleScope.launch {
+            viewModel.insertAccount(account)
+            Toast.makeText(requireContext(), "Conto salvato!", Toast.LENGTH_SHORT).show()
+            findNavController().popBackStack()
+        }
```

## Verification Results

### Automated Tests
- Ran `:app:compileDebugKotlin` which now finishes successfully.

> [!NOTE]
> Other similar calls in the project (e.g., in `AddVincoloFragment`, `DettaglioVincoloFragment`, `SaltoQuagliaFragment`) were already correctly wrapped in coroutine scopes.
