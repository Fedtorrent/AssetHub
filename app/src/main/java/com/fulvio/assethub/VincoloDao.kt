package com.fulvio.assethub

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VincoloDao {
    
    // --- VINCOLI ---
    @Query("SELECT * FROM vincoli")
    fun getAllVincoli(): Flow<List<Vincolo>>

    @Transaction
    @Query("SELECT * FROM vincoli")
    fun getAllVincoliWithAccount(): Flow<List<VincoloWithAccount>>

    @Transaction
    @Query("SELECT * FROM vincoli")
    fun getAllVincoliWithFullInfo(): Flow<List<VincoloWithFullInfo>>

    @Transaction
    @Query("SELECT * FROM vincoli WHERE id = :id")
    suspend fun getVincoloWithFullInfoById(id: Long): VincoloWithFullInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVincolo(vincolo: Vincolo): Long

    @Update
    suspend fun updateVincolo(vincolo: Vincolo)

    @Delete
    suspend fun deleteVincolo(vincolo: Vincolo)

    @Query("SELECT * FROM vincoli WHERE id = :id")
    suspend fun getVincoloById(id: Long): Vincolo?

    @Transaction
    @Query("SELECT * FROM vincoli WHERE codiceVincolo = :codice")
    suspend fun getVincoloByCodice(codice: Int): VincoloWithAccount?

    @Query("SELECT MAX(codiceVincolo) FROM vincoli")
    suspend fun getMaxCodiceVincolo(): Int?

    @Query("SELECT * FROM vincoli WHERE accountId = :accountId AND nome = :nome AND isDeleted = 0 ORDER BY dataDecorrenza DESC LIMIT 1")
    suspend fun getLastVincoloByAccountAndName(accountId: Long, nome: String): Vincolo?

    @Query("SELECT * FROM vincoli WHERE accountId = :accountId AND isDeleted = 0 ORDER BY dataDecorrenza DESC, id DESC LIMIT 1")
    suspend fun getLastVincoloByAccount(accountId: Long): Vincolo?

    @Query("UPDATE vincoli SET codiceVincolo = :nuovoCodice WHERE accountId = :accountId AND nome = :nome")
    suspend fun updateCodiceVincoloPerAsset(accountId: Long, nome: String, nuovoCodice: Int)

    @Query("UPDATE vincoli SET codiceVincolo = :nuovoCodice WHERE id = :id")
    suspend fun updateCodiceVincoloById(id: Long, nuovoCodice: Int)

    @Query("UPDATE vincoli SET tipo = :nuovoTipo WHERE tipo = :vecchioTipo")
    suspend fun updateTipoMassa(vecchioTipo: String, nuovoTipo: String)

    @Query("UPDATE categories SET name = :nuovoNome WHERE systemType = :systemType")
    suspend fun updateCategoryNameBySystemType(systemType: String, nuovoNome: String)

    // --- ACCOUNTS ---
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Transaction
    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccountsWithBankAndVincoli(): Flow<List<AccountWithBankAndVincoli>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account): Long

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    @Query("UPDATE accounts SET isDeleted = :deleted WHERE id = :id")
    suspend fun softDeleteAccount(id: Long, deleted: Boolean)

    @Query("UPDATE accounts SET isDeleted = :deleted WHERE bankId = :bankId")
    suspend fun softDeleteAccountsByBank(bankId: Long, deleted: Boolean)

    // --- BANKS ---
    @Query("SELECT * FROM banks ORDER BY (name != 'Asset Personali'), name ASC")
    fun getAllBanks(): Flow<List<Bank>>

    @Transaction
    @Query("SELECT * FROM banks ORDER BY (name != 'Asset Personali'), name ASC")
    fun getAllBanksWithAccounts(): Flow<List<BankWithAccounts>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: Bank): Long

    @Update
    suspend fun updateBank(bank: Bank)

    @Delete
    suspend fun deleteBank(bank: Bank)

    @Query("SELECT * FROM banks WHERE id = :id")
    suspend fun getBankById(id: Long): Bank?

    @Query("UPDATE banks SET isDeleted = :deleted WHERE id = :id")
    suspend fun softDeleteBank(id: Long, deleted: Boolean)

    // --- VINCOLI SOFT DELETE ---
    @Query("UPDATE vincoli SET isDeleted = :deleted WHERE id = :id")
    suspend fun softDeleteVincolo(id: Long, deleted: Boolean)

    @Query("UPDATE vincoli SET isDeleted = :deleted WHERE accountId = :accountId")
    suspend fun softDeleteVincoliByAccount(accountId: Long, deleted: Boolean)

    @Query("UPDATE vincoli SET isDeleted = :deleted WHERE accountId IN (SELECT id FROM accounts WHERE bankId = :bankId)")
    suspend fun softDeleteVincoliByBank(bankId: Long, deleted: Boolean)

    // --- CATEGORIES ---
    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE systemType = :type LIMIT 1")
    suspend fun getCategoryByType(type: String): Category?

    @Insert
    suspend fun insertCategory(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    // --- USEFUL LINKS ---
    @Query("SELECT * FROM useful_links ORDER BY id ASC")
    fun getAllUsefulLinks(): Flow<List<UsefulLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsefulLink(link: UsefulLink): Long

    @Delete
    suspend fun deleteUsefulLink(link: UsefulLink)

    @Query("SELECT COUNT(*) FROM useful_links")
    suspend fun getUsefulLinksCount(): Int
}
