package com.example.pocketmanager.home.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.util.*

@IgnoreExtraProperties
class Transaction(var transactionId: String?=null,
                  var amount: String?=null,
                  var note:String?=null,
                  var reciever:String?=null,
                  var sender:String?=null,
                  var date:String?=null,
                  var type:String?=null,
                  var mode:String?=null,
                  var debit:Boolean?=false,
                  var transactDate:Date?=Date()) {


    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "transactionId" to transactionId,
            "amount" to amount,
            "note" to note,
            "reciever" to reciever,
            "sender" to sender,
            "date" to date,
            "type" to type,
            "mode" to mode,
            "debit" to debit,
            "transactDate" to transactDate
        )
    }

}