package com.example.pocketmanager.home.ui

import android.app.Dialog
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.example.pocketmanager.R
import com.example.pocketmanager.home.model.Amount
import com.example.pocketmanager.utils.Constants
import com.example.pocketmanager.utils.LoaderInterface
import com.example.pocketmanager.utils.Utility
import com.google.firebase.database.DatabaseReference
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class HomeViewModel: ViewModel(){

    fun showAddAmountDialog(context:Context, dbReference: DatabaseReference){

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.add_total_amount_dialog)
        dialog.setTitle("Add Total Amount")

        val etTotalAmount = dialog.findViewById<EditText>(R.id.et_total_amount_home)

        val dialogButton: Button = dialog.findViewById<Button>(R.id.btn_add_confrm)

        dialogButton.setOnClickListener {
            val totalAmount = etTotalAmount.text.toString().trim()
            if (!TextUtils.isEmpty(totalAmount)){
                val today = Date().toString()
                val balance = 0
                val spent = 0
                val amount = Amount(totalAmount, today, balance.toString(),spent.toString())
                addAmountToDb(amount, dbReference, context, dialog, etTotalAmount)
            }
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun addAmountToDb(amount: Amount, dbReference: DatabaseReference, context:Context, dialog: Dialog, editText: EditText) {

        val title = dialog.findViewById<TextView>(R.id.tv_title_add_amount)
        title.text = "Updating... "

        val progressBar = ProgressBar(context)
        progressBar.visibility = View.VISIBLE

        val key = dbReference.child(Constants.AMOUNT).push().key

        val amountMap = amount.toMap()

        val childUpdates = HashMap<String,Any>()
        childUpdates["/amount/$key"] = amountMap

        dbReference.updateChildren(childUpdates)
            .addOnCompleteListener {
                progressBar.visibility = View.GONE
                dialog.cancel()
                if (it.isSuccessful){
                    Utility.showToast(context,"Amount added")
                }else{
                    Utility.showToast(context,"There was an error in updating amount")
                }
            }

    }

}