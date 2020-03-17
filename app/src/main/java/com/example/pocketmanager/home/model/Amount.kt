package com.example.pocketmanager.home.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Amount(var amount: String){

    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "amount" to amount
        )
    }

}