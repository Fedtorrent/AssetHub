package com.fulvio.assethub

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithBank(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "bankId",
        entityColumn = "id"
    )
    val bank: Bank
)

data class VincoloWithFullInfo(
    @Embedded val vincolo: Vincolo,
    @Relation(
        entity = Account::class,
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val accountWithBank: AccountWithBank?
)
