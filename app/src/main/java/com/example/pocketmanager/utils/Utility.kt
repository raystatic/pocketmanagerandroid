package com.example.pocketmanager.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

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

        fun noOfDaysBWTwoDates(date1:Date, date2:Date): String{
            val diff = date2.time - date1.time
            return TimeUnit.DAYS.convert(diff,TimeUnit.MILLISECONDS).toString()
        }

        fun stringToDate(strdate: String?):Date{
            val format =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//            try {
//                val date = format.parse(strdate!!)
//                return date
//            } catch (e: ParseException) {
//                e.printStackTrace()
//            }

            val date = format.parse(strdate!!)
            return date

        }

        fun showDialog(context: Context, s: String) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(s)
            builder.setPositiveButton("OK"
            ) { dialog, which ->
                dialog.cancel()
            }

            val dialog = builder.create()
            dialog.show()
        }

    }
}