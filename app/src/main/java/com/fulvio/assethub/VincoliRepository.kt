package com.fulvio.assethub

import kotlinx.coroutines.flow.Flow

class VincoliRepository(private val vincoloDao: VincoloDao) {
    
    // --- VINCOLI ---
    val allVincoli: Flow<List<Vincolo>> = vincoloDao.getAllVincoli()
    val allVincoliWithFullInfo: Flow<List<VincoloWithFullInfo>> = vincoloDao.getAllVincoliWithFullInfo()

    suspend fun insert(vincolo: Vincolo): Long {
        return vincoloDao.insertVincolo(vincolo)
    }

    suspend fun update(vincolo: Vincolo) {
        vincoloDao.updateVincolo(vincolo)
    }

    suspend fun delete(vincolo: Vincolo) {
        vincoloDao.softDeleteVincolo(vincolo.id, true)
    }

    suspend fun restore(vincolo: Vincolo) {
        vincoloDao.softDeleteVincolo(vincolo.id, false)
    }

    suspend fun deletePhysical(vincolo: Vincolo) {
        vincoloDao.deleteVincolo(vincolo)
    }

    suspend fun getById(id: Long): Vincolo? {
        return vincoloDao.getVincoloById(id)
    }

    suspend fun getWithFullInfoById(id: Long): VincoloWithFullInfo? {
        return vincoloDao.getVincoloWithFullInfoById(id)
    }

    suspend fun getMaxCodiceVincolo(): Int {
        return vincoloDao.getMaxCodiceVincolo() ?: 0
    }

    suspend fun getByCodice(codice: Int): VincoloWithAccount? {
        return vincoloDao.getVincoloByCodice(codice)
    }

    suspend fun getLastVincoloByAccountAndName(accountId: Long, nome: String): Vincolo? {
        return vincoloDao.getLastVincoloByAccountAndName(accountId, nome)
    }

    suspend fun getLastVincoloByAccount(accountId: Long): Vincolo? {
        return vincoloDao.getLastVincoloByAccount(accountId)
    }

    suspend fun updateCodiceVincoloPerAsset(accountId: Long, nome: String, nuovoCodice: Int) {
        vincoloDao.updateCodiceVincoloPerAsset(accountId, nome, nuovoCodice)
    }

    suspend fun updateCodiceVincoloById(id: Long, nuovoCodice: Int) {
        vincoloDao.updateCodiceVincoloById(id, nuovoCodice)
    }

    // --- ACCOUNTS ---
    val allAccounts: Flow<List<Account>> = vincoloDao.getAllAccounts()
    val allAccountsWithBankAndVincoli: Flow<List<AccountWithBankAndVincoli>> = vincoloDao.getAllAccountsWithBankAndVincoli()

    suspend fun insertAccount(account: Account): Long {
        return vincoloDao.insertAccount(account)
    }

    suspend fun updateAccount(account: Account) {
        vincoloDao.updateAccount(account)
    }

    suspend fun deleteAccount(account: Account) {
        vincoloDao.softDeleteAccount(account.id, true)
        vincoloDao.softDeleteVincoliByAccount(account.id, true)
    }

    suspend fun restoreAccount(account: Account) {
        vincoloDao.softDeleteAccount(account.id, false)
        vincoloDao.softDeleteVincoliByAccount(account.id, false)
    }

    suspend fun deleteAccountPhysical(account: Account) {
        vincoloDao.deleteAccount(account)
    }

    suspend fun getAccountById(id: Long): Account? {
        return vincoloDao.getAccountById(id)
    }

    // --- BANKS ---
    val allBanks: Flow<List<Bank>> = vincoloDao.getAllBanks()
    val allBanksWithAccounts: Flow<List<BankWithAccounts>> = vincoloDao.getAllBanksWithAccounts()

    suspend fun insertBank(bank: Bank): Long {
        return vincoloDao.insertBank(bank)
    }

    suspend fun updateBank(bank: Bank) {
        vincoloDao.updateBank(bank)
    }

    suspend fun deleteBank(bank: Bank) {
        vincoloDao.softDeleteBank(bank.id, true)
        vincoloDao.softDeleteAccountsByBank(bank.id, true)
        vincoloDao.softDeleteVincoliByBank(bank.id, true)
    }

    suspend fun restoreBank(bank: Bank) {
        vincoloDao.softDeleteBank(bank.id, false)
        vincoloDao.softDeleteAccountsByBank(bank.id, false)
        vincoloDao.softDeleteVincoliByBank(bank.id, false)
    }

    suspend fun deleteBankPhysical(bank: Bank) {
        vincoloDao.deleteBank(bank)
    }

    suspend fun getBankById(id: Long): Bank? {
        return vincoloDao.getBankById(id)
    }

    // --- CATEGORIES ---
    val allCategories: Flow<List<Category>> = vincoloDao.getAllCategories()

    // --- USEFUL LINKS ---
    val allUsefulLinks: Flow<List<UsefulLink>> = vincoloDao.getAllUsefulLinks()

    suspend fun insertUsefulLink(link: UsefulLink): Long {
        return vincoloDao.insertUsefulLink(link)
    }

    suspend fun deleteUsefulLink(link: UsefulLink) {
        vincoloDao.deleteUsefulLink(link)
    }

    suspend fun getUsefulLinksCount(): Int {
        return vincoloDao.getUsefulLinksCount()
    }
}
