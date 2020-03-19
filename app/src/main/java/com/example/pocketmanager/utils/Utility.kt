package com.example.pocketmanager.utils

import android.content.Context
import android.widget.Toast

class Utility {
    companion object {
        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun formatDate (date : String?): String {
            val dateList = date?.split(" ")
            return "${dateList?.get(0)} ${dateList?.get(1)} ${dateList?.get(2)}"
        }

    }
}