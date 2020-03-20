package com.example.pocketmanager.utils

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class Utility {
    companion object {
        fun showToast(context: Context, msg: String) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

        fun formatDate (date : String?): String {
            val dateList = date?.split(" ")
            return "${dateList?.get(0)} ${dateList?.get(1)} ${dateList?.get(2)} ${dateList?.get(5)}"
        }

        fun showSnackBar(view: View,msg: String){
            Snackbar.make(view,msg,Snackbar.LENGTH_SHORT)
        }

    }
}