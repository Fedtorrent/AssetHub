package com.fulvio.assethub

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithBankAndVincoli(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "bankId",
        entityColumn = "id"
    )
    val bank: Bank,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category,
    @Relation(
        parentColumn = "id",
        entityColumn = "accountId"
    )
    val vincoli: List<Vincolo>
)
