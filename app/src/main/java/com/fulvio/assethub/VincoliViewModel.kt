package com.fulvio.assethub

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.util.Calendar

class VincoliViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VincoliRepository
    val allVincoli: LiveData<List<Vincolo>>
    val allAccounts: LiveData<List<Account>>
    val allCategories: LiveData<List<Category>>
    val allVincoliWithFullInfo: LiveData<List<VincoloWithFullInfo>>
    val allAccountsWithBankAndVincoli: LiveData<List<AccountWithBankAndVincoli>>
    val allBanks: LiveData<List<Bank>>
    val allBanksWithAccounts: LiveData<List<BankWithAccounts>>
    val allUsefulLinks: LiveData<List<UsefulLink>>

    init {
        val vincoloDao = AppDatabase.getDatabase(application, viewModelScope).vincoloDao()
        repository = VincoliRepository(vincoloDao)
        
        allVincoli = repository.allVincoli.asLiveData().map { list ->
            list.sortedBy { vincolo: Vincolo ->
                if (vincolo.tipo == "Conto Corrente") {
                    Long.MAX_VALUE
                } else {
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = vincolo.dataDecorrenza
                    cal.add(Calendar.MONTH, vincolo.durataMesi)
                    cal.timeInMillis
                }
            }
        }
        
        allAccounts = repository.allAccounts.asLiveData()
        allCategories = repository.allCategories.asLiveData()
        allVincoliWithFullInfo = repository.allVincoliWithFullInfo.asLiveData()
        allAccountsWithBankAndVincoli = repository.allAccountsWithBankAndVincoli.asLiveData()
        allBanks = repository.allBanks.asLiveData().map { list ->
            list.sortedWith(compareBy({ it.name != "Asset Personali" }, { it.name }))
        }
        allBanksWithAccounts = repository.allBanksWithAccounts.asLiveData().map { list ->
            list.sortedWith(compareBy({ it.bank.name != "Asset Personali" }, { it.bank.name }))
        }
        allUsefulLinks = repository.allUsefulLinks.asLiveData()
    }

    // --- VINCOLI ---
    suspend fun insert(vincolo: Vincolo): Long {
        return repository.insert(vincolo)
    }

    suspend fun update(vincolo: Vincolo) {
        repository.update(vincolo)
    }

    fun delete(vincolo: Vincolo) = viewModelScope.launch {
        repository.delete(vincolo)
    }

    fun restore(vincolo: Vincolo) = viewModelScope.launch {
        repository.restore(vincolo)
    }

    fun deletePhysical(vincolo: Vincolo) = viewModelScope.launch {
        repository.deletePhysical(vincolo)
    }

    suspend fun getById(id: Long): Vincolo? {
        return repository.getById(id)
    }

    suspend fun getWithFullInfoById(id: Long): VincoloWithFullInfo? {
        return repository.getWithFullInfoById(id)
    }

    suspend fun getMaxCodiceVincolo(): Int {
        return repository.getMaxCodiceVincolo()
    }

    suspend fun getByCodice(codice: Int): VincoloWithAccount? {
        return repository.getByCodice(codice)
    }

    suspend fun getLastVincoloByAccountAndName(accountId: Long, nome: String): Vincolo? {
        return repository.getLastVincoloByAccountAndName(accountId, nome)
    }

    suspend fun getLastVincoloByAccount(accountId: Long): Vincolo? {
        return repository.getLastVincoloByAccount(accountId)
    }

    suspend fun updateCodiceVincoloPerAsset(accountId: Long, nome: String, nuovoCodice: Int) {
        repository.updateCodiceVincoloPerAsset(accountId, nome, nuovoCodice)
    }

    suspend fun updateCodiceVincoloById(id: Long, nuovoCodice: Int) {
        repository.updateCodiceVincoloById(id, nuovoCodice)
    }

    // --- ACCOUNTS ---
    suspend fun insertAccount(account: Account): Long {
        return repository.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) {
        repository.updateAccount(account)
    }

    fun deleteAccount(account: Account) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun restoreAccount(account: Account) = viewModelScope.launch {
        repository.restoreAccount(account)
    }

    fun deleteAccountPhysical(account: Account) = viewModelScope.launch {
        repository.deleteAccountPhysical(account)
    }

    suspend fun getAccountById(id: Long): Account? {
        return repository.getAccountById(id)
    }

    // --- BANKS ---
    suspend fun insertBank(bank: Bank): Long {
        return repository.insertBank(bank)
    }

    suspend fun updateBank(bank: Bank) {
        repository.updateBank(bank)
    }

    fun deleteBank(bank: Bank) = viewModelScope.launch {
        repository.deleteBank(bank)
    }

    fun restoreBank(bank: Bank) = viewModelScope.launch {
        repository.restoreBank(bank)
    }

    fun deleteBankPhysical(bank: Bank) = viewModelScope.launch {
        repository.deleteBankPhysical(bank)
    }

    suspend fun getBankById(id: Long): Bank? {
        return repository.getBankById(id)
    }

    // --- USEFUL LINKS ---
    suspend fun insertUsefulLink(link: UsefulLink): Long {
        return repository.insertUsefulLink(link)
    }

    fun deleteUsefulLink(link: UsefulLink) = viewModelScope.launch {
        repository.deleteUsefulLink(link)
    }

    suspend fun getUsefulLinksCount(): Int {
        return repository.getUsefulLinksCount()
    }
}
