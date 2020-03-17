package com.example.pocketmanager.home.ui

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.example.pocketmanager.R
import com.example.pocketmanager.home.model.Amount
import com.example.pocketmanager.home.model.Transaction
import com.example.pocketmanager.utils.Constants
import com.example.pocketmanager.utils.PrefManager
import com.example.pocketmanager.utils.Utility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import java.util.*
import kotlin.collections.HashMap


class HomeViewModel: ViewModel(){

    fun showTransactionDialog(context: Context, dbReference: DatabaseReference){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_transaction_layout)

        val etAmount = dialog.findViewById<EditText>(R.id.et_add_transaction_amount)
        val etReceiver = dialog.findViewById<EditText>(R.id.et_add_transaction_receiver)
        val etSender = dialog.findViewById<EditText>(R.id.et_add_transaction_sender)
        val etDate = dialog.findViewById<EditText>(R.id.et_add_transaction_date)
        val etMode = dialog.findViewById<EditText>(R.id.et_add_transaction_mode)
        val etDesc = dialog.findViewById<EditText>(R.id.et_add_transaction_desc)

        val btnAdd = dialog.findViewById<Button>(R.id.btn_add_transaction_confrm)

        etDate.setText(Date().toString())

        etSender.setText("me")

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT)

        btnAdd.setOnClickListener {
            val amount = etAmount.text.toString().trim()
            val receiver = etReceiver.text.toString().trim()
            val sender = etSender.text.toString().trim()
            val date = etDate.text.toString().trim()
            val mode = etMode.text.toString().trim()
            val desc = etDesc.text.toString().trim()

            val transaction = Transaction(amount,desc,receiver,sender,date,mode)

            addTransactionToDb(context,transaction, dbReference, dialog)

        }



        dialog.show()
        dialog.setCanceledOnTouchOutside(true)


    }

    private fun addTransactionToDb(
        context: Context,
        transaction: Transaction,
        dbReference: DatabaseReference,
        dialog: Dialog
    ) {

        val title = dialog.findViewById<TextView>(R.id.tv_add_transaction_title)
        title.text = "Uploading transaction..."

        val key = dbReference.child(Constants.USER).push().key.toString()

        val transactionMap = transaction.toMap()

        val childUpdates = HashMap<String,Any>()

        val user = FirebaseAuth.getInstance().currentUser

        childUpdates["/${user?.displayName}/transactions/$key"] = transactionMap

        dbReference.updateChildren(childUpdates)
            .addOnCompleteListener {
                dialog.cancel()
                if (it.isSuccessful){
                    Utility.showToast(context,"Transaction added")
                }else{
                    Utility.showToast(context,"There was an error in adding transaction")
                }
            }


    }

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

        val prefManager = PrefManager(context)

        var key = ""

        val amountMap = amount.toMap()

        val childUpdates = HashMap<String,Any>()

        val user = FirebaseAuth.getInstance().currentUser

        if (!TextUtils.isEmpty(prefManager.getString(Constants.ROOT_KEY))){
            key = prefManager.getString(Constants.ROOT_KEY).toString()
        }else{
            key = dbReference.child(Constants.USER).push().key.toString()
            prefManager.saveString(Constants.ROOT_KEY,key)
        }

        childUpdates["/${user?.displayName}/$key"] = amountMap

        dbReference.updateChildren(childUpdates)
            .addOnCompleteListener {
                dialog.cancel()
                if (it.isSuccessful){
                    Utility.showToast(context,"Amount added")
                }else{
                    Utility.showToast(context,"There was an error in updating amount")
                }
            }

    }

}