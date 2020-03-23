package com.example.pocketmanager.home.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HomeViewModel: ViewModel(), TransactionsRecyclerViewAdapter.TransactionInteractor{

    var EXPENDITURE_TYPE = ""

    val typeList = arrayOf("Self","Other")
    val monthList = arrayOf(1)
    var monthDuration = 1

    fun readBalancefromDB(context: Context,dbReference: DatabaseReference, textView: TextView, tvDaysLeft:TextView){
        readAmountFromDB(context,dbReference,null, textView,tvDaysLeft)
    }


    fun showInfoDialog(context: Context,dbReference: DatabaseReference){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.info_dialog_layout)

        readAmountFromDB(context,dbReference, dialog, null,null)

        val button  = dialog.findViewById<Button>(R.id.btn_ok_summary)

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        button.setOnClickListener {
            dialog.cancel()
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)

    }

    private fun readAmountFromDB(context: Context, dbReference: DatabaseReference, dialog: Dialog?, textView: TextView?,tvDaysLeft:TextView?) {

        val prefManager = PrefManager(context)
        val user = FirebaseAuth.getInstance().currentUser

        val amountListener =   object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Utility.showToast(context,"Unable to refresh right now!")
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (!p0.exists()){
                    prefManager.saveBoolean(Constants.BUDGET_UPDATED,false)
                    Log.d("context_error","Please add amount of this month to get started $context")
                    textView?.text = "--"
                    tvDaysLeft?.text = "--"

                    try{
                        Utility.showDialog(context,"Please add budget of this month to get started")
                    }catch (e:Exception){
                        Log.d("dialog_error",e.message!!)
                        Utility.showToast(context,"Please add budget of this month to get started")
                    }
                }else{
                    val amount = p0.getValue(Amount::class.java)

                    if (dialog!=null && textView == null){
                        val tvTotalAmount = dialog.findViewById<TextView>(R.id.tv_total_amount)
                        val tvBalanceAmount = dialog.findViewById<TextView>(R.id.tv_balance_amount)
                        val tvSpentAmount = dialog.findViewById<TextView>(R.id.tv_spent_amount)
                        val tvDateUpdated = dialog.findViewById<TextView>(R.id.tv_date_updation)
                        val tvEndDate = dialog.findViewById<TextView>(R.id.tv_date_end)

                        tvTotalAmount.text = amount?.amount
                        tvBalanceAmount.text = amount?.balance
                        tvDateUpdated.text = Utility.formatDate(amount?.date)
                        tvSpentAmount.text = amount?.spent
                        tvEndDate.text = Utility.formatDate(amount?.uptoDate.toString())

                    }else if (dialog == null && textView!=null && tvDaysLeft!=null) {
                        val noOfdays = amount?.uptoDate?.let { Utility.noOfDaysBWTwoDates(Date(), it) }
                        if (noOfdays?.toInt()!! <0){
                            deleteExistingData(user,dbReference, context)
                            deleteEarlierTransactions(user,dbReference)
                        }else{
                            textView.text = amount?.balance
                            val uptoDate = amount?.uptoDate?.let { Utility.noOfDaysBWTwoDates(Date(), it) }
                            tvDaysLeft.text = uptoDate
                            prefManager.saveBoolean(Constants.BUDGET_UPDATED,true)
                        }
                    }
                }
            }
        }

        dbReference.child("/${user?.displayName}/budget").addValueEventListener(amountListener)

    }

    private fun deleteExistingData(user: FirebaseUser?, dbReference: DatabaseReference, context: Context) {
        dbReference.child("/${user?.displayName}/budget").setValue(null)
    }

    fun showTransactionDialog(context: Context, dbReference: DatabaseReference){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.add_transaction_layout)

        val etAmount = dialog.findViewById<EditText>(R.id.et_add_transaction_amount)
        val etReceiver = dialog.findViewById<EditText>(R.id.et_add_transaction_receiver)
        val etSender = dialog.findViewById<EditText>(R.id.et_add_transaction_sender)
        val etDate = dialog.findViewById<TextView>(R.id.et_add_transaction_date)
        val etMode = dialog.findViewById<EditText>(R.id.et_add_transaction_mode)
        val etDesc = dialog.findViewById<EditText>(R.id.et_add_transaction_desc)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner_add_transaction)
        val linDate = dialog.findViewById<LinearLayout>(R.id.linDate)

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

        etDate.text = Utility.formatDate(Date().toString())

        var transactionDate = Date()

        val calender = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(context,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate.set(year,month,dayOfMonth)
            //Utility.showToast(context,)
            etDate.text = Utility.formatDate(newDate.time.toString())
            transactionDate = newDate.time
        },calender.get(Calendar.YEAR),calender.get(Calendar.MONTH),calender.get(Calendar.DAY_OF_MONTH))

        linDate.setOnClickListener {
            datePickerDialog.show()
        }

        //etSender.setText("me")

        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        btnAdd.setOnClickListener {

            val prefManager = PrefManager(context)
            if (!prefManager.getBoolean(Constants.BUDGET_UPDATED)!!){
                Utility.showToast(context,"Budget no updated yet!")
            }else{
                val amount = etAmount.text.toString().trim()
                val receiver = etReceiver.text.toString().trim()
                val sender = etSender.text.toString().trim()
                val date = transactionDate.toString()
                val mode = etMode.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val type = EXPENDITURE_TYPE

                if (sender.isEmpty() && receiver.isEmpty()){
                    etSender.error = "One must be filled"
                    etReceiver.error = "One must be filled"
                }else if (sender.isNotEmpty() && receiver.isNotEmpty()){
                    etSender.error = "One must be empty"
                    etReceiver.error = "One must be empty"
                }else{

                    if (sender.isEmpty()){
                        etSender.error = null
                    }

                    if (receiver.isEmpty()){
                        etReceiver.error = null
                    }

                    if (amount.isNotEmpty() && mode.isNotEmpty() && date.isNotEmpty() && desc.isNotEmpty()){
                        var debit = false

                        if (receiver.isNotEmpty()) {
                            debit = true
                        }else if (sender.isNotEmpty()){
                            debit = false
                        }

                        val key = dbReference.child(Constants.USER).push().key.toString()

                        val transaction = Transaction(key,amount,desc,receiver,sender,date,type,mode,debit, transactionDate)

                        addTransactionToDB(context,transaction,dbReference,dialog,key)
                    }else{
                        Utility.showToast(context,"Fill data properly!")
                    }
                }
            }
        }



        dialog.show()
        dialog.setCanceledOnTouchOutside(true)


    }

    private fun addTransactionToDB(
        context: Context,
        transaction: Transaction,
        dbReference: DatabaseReference,
        dialog: Dialog,
        transactionKey: String
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        dbReference.child("/${user?.displayName}/budget").runTransaction(object : com.google.firebase.database.Transaction.Handler{

            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                Log.d("transaction_error","${p0}")
                Utility.showToast(context,"Transaction competed with ${p0?.message}")
            }

            override fun doTransaction(p0: MutableData): com.google.firebase.database.Transaction.Result {
                val amount = p0.getValue(Amount::class.java)
                    ?: return com.google.firebase.database.Transaction.success(p0)

                addTransactionToDb(context,transaction,dbReference,dialog,transactionKey)

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
        dialog: Dialog,
        transactionKey:String
    ) {

        val title = dialog.findViewById<TextView>(R.id.tv_add_transaction_title)
        title.text = "Uploading transaction..."

        val transactionMap = transaction.toMap()

        val childUpdates = HashMap<String,Any>()

        val user = FirebaseAuth.getInstance().currentUser

        childUpdates["/${user?.displayName}/transactions/$transactionKey"] = transactionMap
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

    fun showAddAmountDialog(context:Context, dbReference: DatabaseReference, tvBalanec: TextView, tvDaysLeft: TextView){

        val dialog = Dialog(context)

        dialog.setContentView(R.layout.add_total_amount_dialog)
        dialog.setTitle("Add Total Amount")

        val etTotalAmount = dialog.findViewById<EditText>(R.id.et_total_amount_home)
        val spinner = dialog.findViewById<Spinner>(R.id.spinner_months)
        val tvDuration = dialog.findViewById<TextView>(R.id.tv_duration_preview)

        tvDuration.visibility = View.GONE

        val calendar =  Calendar.getInstance()

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
                val amount = Amount(totalAmount, today, totalAmount,spent.toString(),calendar.time, Date())
                addAmountToDb(amount, dbReference, context, dialog, etTotalAmount,tvBalanec,tvDaysLeft)
            }else{
                etTotalAmount.error = "Cannot be empty"
            }
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }

    private fun addAmountToDb(amount: Amount, dbReference: DatabaseReference, context:Context, dialog: Dialog, editText: EditText,tvBalanec:TextView,tvDaysLeft:TextView) {

        val title = dialog.findViewById<TextView>(R.id.tv_title_add_amount)
        title.text = "Updating... "

        val prefManager = PrefManager(context)

        val amountMap = amount.toMap()

        val childUpdates = HashMap<String,Any>()

        val user = FirebaseAuth.getInstance().currentUser


        childUpdates["/${user?.displayName}/budget"] = amountMap

        dbReference.updateChildren(childUpdates)
            .addOnCompleteListener {
                dialog.cancel()
                if (it.isSuccessful){
                    deleteEarlierTransactions(user,dbReference)
                    Utility.showToast(context,"Amount added")
                    readBalancefromDB(context,dbReference,tvBalanec,tvDaysLeft)
                }else{
                    Utility.showToast(context,"There was an error in updating amount")
                }
            }

    }

    private fun deleteEarlierTransactions(user: FirebaseUser?, dbReference: DatabaseReference) {
        dbReference.child("${user?.displayName}/transactions")
            .setValue(null)
    }

    fun readTransactions(context: Context, dbReference: DatabaseReference, recyclerView: RecyclerView, progressBar: ProgressBar, textView: TextView) {
        val user = FirebaseAuth.getInstance().currentUser

        val transactions = ArrayList<Transaction>()

        dbReference.child("/${user?.displayName}/transactions/")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Utility.showToast(context,"Unable to read transactions!")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    transactions.clear()
                    if (!p0.exists()){
                        if (progressBar.visibility == View.VISIBLE){
                            progressBar.visibility = View.GONE
                        }
                        if (textView.visibility == View.GONE){
                            textView.visibility = View.VISIBLE
                        }
                        if (recyclerView.visibility == View.VISIBLE){
                            recyclerView.visibility = View.GONE
                        }
                    }else{
                        p0.children.forEach {
                            val transaction = it.getValue(Transaction::class.java)
                            transactions.add(transaction!!)
                        }

                        transactions.forEach {
                            Log.d("unsorted_transactions",it.date!!)
                        }

                        val sortedTransaction = Utility.sortTransactions(transactions)

                        if (recyclerView.visibility == View.GONE){
                            recyclerView.visibility = View.VISIBLE
                        }
                        val adapter = TransactionsRecyclerViewAdapter(context, sortedTransaction, this@HomeViewModel)
                        val layoutManager = LinearLayoutManager(context)
                        recyclerView.layoutManager = layoutManager
                        recyclerView.adapter = adapter

                        if (progressBar.visibility == View.VISIBLE){
                            progressBar.visibility = View.GONE
                        }
                        if (textView.visibility == View.VISIBLE){
                            textView.visibility = View.GONE
                        }
                    }
                }
            })

    }

    override fun onTransactionsLoaded(progressBar: ProgressBar, msg:String?, context: Context) {
        if (progressBar.visibility == View.VISIBLE){
            progressBar.visibility = View.GONE
            if (msg!=null){
                Utility.showToast(context,msg)
            }
        }
    }

    override fun onTransactionClicked(transaction: Transaction, context: Context) {
        showTransactionInfoDialog(transaction,context)
    }

    private fun showTransactionInfoDialog(transaction: Transaction,context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.transaction_dialog)

        val button  = dialog.findViewById<Button>(R.id.btn_ok_transaction)
        val tvTransactionId = dialog.findViewById<TextView>(R.id.tv_transaction_ID)
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

        tvTransactionId.text = transaction.transactionId
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

    fun test(context: Context) {
        val calender = Calendar.getInstance()
        DatePickerDialog(context,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate.set(year,month,dayOfMonth)
            Utility.showToast(context,Utility.formatDate(newDate.time.toString()))
        },calender.get(Calendar.YEAR),calender.get(Calendar.MONTH),calender.get(Calendar.DAY_OF_MONTH))
    }
}