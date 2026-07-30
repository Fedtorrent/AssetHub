package com.fulvio.assethub

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithBankAndVincoliWrapper(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?,
    @Relation(
        parentColumn = "id",
        entityColumn = "accountId"
    )
    val vincoli: List<Vincolo>
)

data class BankWithAccounts(
    @Embedded val bank: Bank,
    @Relation(
        entity = Account::class,
        parentColumn = "id",
        entityColumn = "bankId"
    )
    val accounts: List<AccountWithBankAndVincoliWrapper>
)
