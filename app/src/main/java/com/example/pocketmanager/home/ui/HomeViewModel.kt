package com.example.pocketmanager.home.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.icu.util.LocaleData
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanager.R
import com.example.pocketmanager.home.model.Amount
import com.example.pocketmanager.home.model.Transaction
import com.example.pocketmanager.home.ui.adapters.TransactionsRecyclerViewAdapter
import com.example.pocketmanager.utils.Constants
import com.example.pocketmanager.utils.PrefManager
import com.example.pocketmanager.utils.Utility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeViewModel: ViewModel(), TransactionsRecyclerViewAdapter.TransactionInteractor{

    var EXPENDITURE_TYPE = ""

    val typeList = arrayOf("Self","Home")
    val monthList = arrayOf(1,2,3,4,5)
    var monthDuration = 1

    fun readBalancefromDB(context: Context,dbReference: DatabaseReference, textView: TextView){
        readAmountFromDB(context,dbReference,null, textView)
    }


    fun showInfoDialog(context: Context,dbReference: DatabaseReference){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.info_dialog_layout)

        readAmountFromDB(context,dbReference, dialog, null)

        val button  = dialog.findViewById<Button>(R.id.btn_ok_summary)

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        button.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)

    }

    private fun readAmountFromDB(context: Context, dbReference: DatabaseReference, dialog: Dialog?, textView: TextView?) {

        val prefManager = PrefManager(context)
        val rootKey = prefManager.getString(Constants.ROOT_KEY)
        val user = FirebaseAuth.getInstance().currentUser

        val amountListener =   object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Utility.showToast(context,"Unable to refresh right now!")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val amount = p0.getValue(Amount::class.java)

                if (dialog!=null && textView == null){
                    val tvTotalAmount = dialog.findViewById<TextView>(R.id.tv_total_amount)
                    val tvBalanceAmount = dialog.findViewById<TextView>(R.id.tv_balance_amount)
                    val tvSpentAmount = dialog.findViewById<TextView>(R.id.tv_spent_amount)
                    val tvDateUpdated = dialog.findViewById<TextView>(R.id.tv_date_updation)

                    tvTotalAmount.text = amount?.amount
                    tvBalanceAmount.text = amount?.balance
                    tvDateUpdated.text = Utility.formatDate(amount?.date)
                    tvSpentAmount.text = amount?.spent

                }else if (dialog == null && textView!=null) {
                    textView.text = amount?.balance
                }
            }
        }

        dbReference.child("/${user?.displayName}/$rootKey").addValueEventListener(amountListener)

    }

    fun showTransactionDialog(context: Context, dbReference: DatabaseReference){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_transaction_layout)

        val etAmount = dialog.findViewById<EditText>(R.id.et_add_transaction_amount)
        val etReceiver = dialog.findViewById<EditText>(R.id.et_add_transaction_receiver)
        val etSender = dialog.findViewById<EditText>(R.id.et_add_transaction_sender)
        val etDate = dialog.findViewById<EditText>(R.id.et_add_transaction_date)
        val etMode = dialog.findViewById<EditText>(R.id.et_add_transaction_mode)
        val etDesc = dialog.findViewById<EditText>(R.id.et_add_transaction_desc)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner_add_transaction)

        spinner.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                EXPENDITURE_TYPE = typeList[position]
            }
        }

        val aa = ArrayAdapter(context, android.R.layout.simple_spinner_item, typeList)

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = aa

        val btnAdd = dialog.findViewById<Button>(R.id.btn_add_transaction_confrm)

        etDate.setText(Utility.formatDate(Date().toString()))

        //etSender.setText("me")

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        btnAdd.setOnClickListener {
            val amount = etAmount.text.toString().trim()
            val receiver = etReceiver.text.toString().trim()
            val sender = etSender.text.toString().trim()
            val date = Date().toString()
            val mode = etMode.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val type = EXPENDITURE_TYPE

            if (sender.isNotEmpty() && receiver.isNotEmpty()){
                etSender.error = "One must be empty"
                etReceiver.error = "One must be empty"
            }else{
                var debit = false

                if (receiver.isNotEmpty()) {
                    debit = true
                }else if (sender.isNotEmpty()){
                    debit = false
                }

                val transaction = Transaction(amount,desc,receiver,sender,date,type,mode,debit)

                addTransactionToDB(context,transaction,dbReference,dialog)
            }

        }



        dialog.show()
        dialog.setCanceledOnTouchOutside(true)


    }

    private fun addTransactionToDB(
        context: Context,
        transaction: Transaction,
        dbReference: DatabaseReference,
        dialog: Dialog
    ) {
        val prefManager = PrefManager(context)
        val rootKey = prefManager.getString(Constants.ROOT_KEY)
        val user = FirebaseAuth.getInstance().currentUser
        dbReference.child("/${user?.displayName}/$rootKey").runTransaction(object : com.google.firebase.database.Transaction.Handler{

            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                Log.d("transaction_error","${p0}")
                Utility.showToast(context,"Transaction competed with ${p0?.message}")
            }

            override fun doTransaction(p0: MutableData): com.google.firebase.database.Transaction.Result {
                val amount = p0.getValue(Amount::class.java)
                    ?: return com.google.firebase.database.Transaction.success(p0)

                addTransactionToDb(context,transaction,dbReference,dialog)

                val reciever = transaction.reciever
                val sender = transaction.sender

                val netBalance = amount.balance.toDouble()

                if (transaction.debit!!) {
                    val spent = transaction.amount?.toDouble()

                    val balance = netBalance - spent!!

                    amount.balance = balance.toString()

                    amount.spent = (amount.spent.toDouble() + spent).toString()

                }else{
                    val received = transaction.amount?.toDouble()
                    val balance = netBalance + received!!

                    amount.balance = balance.toString()
                    amount.spent = (amount.spent.toDouble() - received).toString()
                }

                p0.value = amount

                return com.google.firebase.database.Transaction.success(p0)
            }
        })
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
//
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
        val spinner = dialog.findViewById<Spinner>(R.id.spinner_months)
        val tvDuration = dialog.findViewById<TextView>(R.id.tv_duration_preview)

        tvDuration.visibility = View.GONE

        val aa = ArrayAdapter(context, android.R.layout.simple_spinner_item, monthList)

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = aa

        spinner.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                monthDuration = monthList[position]
                val calendar =  Calendar.getInstance()
                calendar.add(Calendar.MONTH,monthDuration)
                tvDuration.visibility = View.VISIBLE
                tvDuration.text = "${Utility.formatDate(Date().toString())} to ${Utility.formatDate(calendar.time.toString())}"
            }

        }

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        val dialogButton: Button = dialog.findViewById<Button>(R.id.btn_add_confrm)

        dialogButton.setOnClickListener {
            val totalAmount = etTotalAmount.text.toString().trim()
            if (!TextUtils.isEmpty(totalAmount)){
                val today = Date().toString()
                val spent = 0
                val amount = Amount(totalAmount, today, totalAmount,spent.toString())
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

    fun readTransactions(context: Context, dbReference: DatabaseReference, recyclerView: RecyclerView, progressBar: ProgressBar) {
        val user = FirebaseAuth.getInstance().currentUser
        val adapter = TransactionsRecyclerViewAdapter(context, this@HomeViewModel, dbReference.child("/${user?.displayName}/transactions/"), progressBar)
        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

    }

    override fun onTransactionsLoaded(progressBar: ProgressBar) {
        if (progressBar.visibility == View.VISIBLE){
            progressBar.visibility = View.GONE
        }
    }

    override fun onTransactionClicked(transaction: Transaction, context: Context) {
        showTransactionInfoDialog(transaction,context)
    }

    private fun showTransactionInfoDialog(transaction: Transaction,context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.transaction_dialog)

        val button  = dialog.findViewById<Button>(R.id.btn_ok_transaction)
        val tvAmount = dialog.findViewById<TextView>(R.id.tv_transaction_amount)
        val tvDebit = dialog.findViewById<TextView>(R.id.tv_transaction_debit)
        val tvDate = dialog.findViewById<TextView>(R.id.tv_transaction_date)
        val tvMode = dialog.findViewById<TextView>(R.id.tv_transaction_mode)
        val tvReceiver = dialog.findViewById<TextView>(R.id.tv_transaction_receiver)
        val tvSender = dialog.findViewById<TextView>(R.id.tv_transaction_sender)
        val tvDescription = dialog.findViewById<TextView>(R.id.tv_transaction_desc)
        val linSender = dialog.findViewById<LinearLayout>(R.id.lin_sender_transaction)
        val linReceiver = dialog.findViewById<LinearLayout>(R.id.lin_transaction_receiver)
        val tvType = dialog.findViewById<TextView>(R.id.tv_transaction_type)

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        tvAmount.text = transaction.amount
        tvDebit.text = transaction.debit.toString()
        tvDate.text = Utility.formatDate(transaction.date)
        tvMode.text = transaction.mode
        tvDescription.text = transaction.note
        tvReceiver.text = transaction.reciever
        tvSender.text = transaction.sender
        tvType.text = transaction.type

        if (transaction.debit!!){
            linSender.visibility = View.GONE
            linReceiver.visibility = View.VISIBLE
        }else{
            linSender.visibility = View.VISIBLE
            linReceiver.visibility = View.GONE
        }

        button.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }
}