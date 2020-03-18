package com.example.pocketmanager.home.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Transaction(var amount: String?=null,
                  var note:String?=null,
                  var reciever:String?=null,
                  var sender:String?=null,
                  var date:String?=null,
                  var type:String?=null,
                  var mode:String?=null
                  ) {


    @Exclude
    fun toMap() : Map<String, Any?>{
        return mapOf(
            "amount" to amount,
            "note" to note,
            "reciever" to reciever,
            "sender" to sender,
            "date" to date,
            "type" to type,
            "mode" to mode
        )
    }

}