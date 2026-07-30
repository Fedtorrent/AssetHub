package com.fulvio.assethub

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithVincoli(
    @Embedded val account: Account,
    @Relation(
        parentColumn = "id",
        entityColumn = "accountId"
    )
    val vincoli: List<Vincolo>
)
