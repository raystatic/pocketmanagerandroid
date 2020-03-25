package com.walletLog.pocketmanager.utils

import java.util.*

class GenerateRandomString {

    companion object{
        const val DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val random = Random()

        fun randomString(len:Int) : String{
            val stringBuilder = StringBuilder(len)
            for (i in 0..len){
                stringBuilder.append(DATA[random.nextInt(DATA.length)])
            }

            return stringBuilder.toString()
        }
    }

}