package com.fulvio.assethub

import androidx.room.Embedded
import androidx.room.Relation

data class VincoloWithAccount(
    @Embedded val vincolo: Vincolo,
    @Relation(
        parentColumn = "accountId",
        entityColumn = "id"
    )
    val account: Account
)
