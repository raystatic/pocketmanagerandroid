package com.walletLog.pocketmanager.home.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
data class Amount(var amount: String, var date: String, var balance: String, var spent:String, var uptoDate:Date, var startDate:Date){

    constructor():this("","","","",Date(),Date())

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "amount" to amount,
            "date" to date,
            "balance" to balance,
            "spent" to spent,
            "uptoDate" to uptoDate,
            "startDate" to startDate
        )
    }

}