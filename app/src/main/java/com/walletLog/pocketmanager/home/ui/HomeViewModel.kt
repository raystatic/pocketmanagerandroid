package com.walletLog.pocketmanager.home.ui

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
import com.walletLog.pocketmanager.R
import com.walletLog.pocketmanager.home.model.Amount
import com.walletLog.pocketmanager.home.model.Transaction
import com.walletLog.pocketmanager.home.ui.adapters.TransactionsRecyclerViewAdapter
import com.walletLog.pocketmanager.utils.Constants
import com.walletLog.pocketmanager.utils.PrefManager
import com.walletLog.pocketmanager.utils.Utility
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
                        tvDateUpdated.text = Utility.formatDate(amount?.startDate.toString())
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
        Log.d("delete_error","deleteExistingData")
    }

    fun showTransactionDialog(context: Context, dbReference: DatabaseReference, transaction: Transaction?){
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
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_add_transaction_title)
        val btnAdd = dialog.findViewById<Button>(R.id.btn_add_transaction_confrm)

        var transactionDate = Date()

        if (transaction!=null){
            tvTitle.text = "Update Transaction"
            etAmount.setText(transaction.amount)
            etReceiver.setText(transaction.reciever)
            etSender.setText(transaction.sender)
            etDate.text = Utility.formatDate(transaction.date)
            etMode.setText(transaction.mode)
            etDesc.setText(transaction.note)
            transactionDate = transaction.transactDate!!
            btnAdd.text = "Update"
        }

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

        if (transaction==null){
            etDate.text = Utility.formatDate(Date().toString())
            transactionDate = Date()
        }

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

                    if (amount.isNotEmpty() && mode.isNotEmpty() && date.isNotEmpty()){
                        var debit = false

                        if (receiver.isNotEmpty()) {
                            debit = true
                        }else if (sender.isNotEmpty()){
                            debit = false
                        }

                        if (transaction!=null){
                            removeTransactionFromDB(context,transaction,dbReference,transaction.transactionId!!)
                            val editTransaction = Transaction(transaction.transactionId,amount,desc,receiver,sender,date,type,mode,debit, transactionDate)

                            addTransactionToDB(context,editTransaction,dbReference,dialog,
                                transaction.transactionId!!
                            )
                        }else{
                            val key = dbReference.child(Constants.USER).push().key.toString()

                            val newTransaction = Transaction(key,amount,desc,receiver,sender,date,type,mode,debit, transactionDate)

                            addTransactionToDB(context,newTransaction,dbReference,dialog,key)
                        }

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
                if (p0!=null){
                    Utility.showToast(context,"Transaction competed")
                }else{
                    Log.d("transaction_error","${p0?.message}")
                }
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

    private fun removeTransactionFromDB(
        context: Context,
        transaction: Transaction,
        dbReference: DatabaseReference,
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


                dbReference.child("/${user?.displayName}/transactions/$transactionKey").setValue(null)
                Log.d("delete_error","removeTransactionFromDB")

                val netBalance = amount.balance.toDouble()

                if (transaction.debit!!) {
                    val spent = transaction.amount?.toDouble()

                    val balance = netBalance + spent!!

                    amount.balance = balance.toString()

                    amount.spent = (amount.spent.toDouble() - spent).toString()

                }else{
                    val received = transaction.amount?.toDouble()
                    val balance = netBalance - received!!

                    amount.balance = balance.toString()
                    amount.spent = (amount.spent.toDouble() + received).toString()
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
      //  val spinner = dialog.findViewById<Spinner>(R.id.spinner_months)
        val tvDuration = dialog.findViewById<TextView>(R.id.tv_duration_preview)
        val linStartDate = dialog.findViewById<LinearLayout>(R.id.lin_start_date)
        val tvStartDate = dialog.findViewById<TextView>(R.id.tv_start_date_budget)

//        tvDuration.visibility = View.GONE

        var startDate = Date()

        tvStartDate.text = Utility.formatDate(startDate.toString())

        val calendar =  Calendar.getInstance()

        calendar.add(Calendar.MONTH,monthDuration)
        tvDuration.visibility = View.VISIBLE
        var endDate = calendar.time
        tvDuration.text = "${Utility.formatDate(startDate.toString())} to ${Utility.formatDate(calendar.time.toString())}"

       // val aa = ArrayAdapter(context, android.R.layout.simple_spinner_item, monthList)

       // aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

//        spinner.adapter = aa
//
//        spinner.onItemSelectedListener = object : OnItemSelectedListener{
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                monthDuration = monthList[position]
//                calendar.add(Calendar.MONTH,monthDuration)
//                tvDuration.visibility = View.VISIBLE
//                tvDuration.text = "${Utility.formatDate(Date().toString())} to ${Utility.formatDate(calendar.time.toString())}"
//            }
//
//        }

        val newCalnedar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(context,DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance()
            newDate.set(year,month,dayOfMonth)
            //Utility.showToast(context,)
            //etDate.text = Utility.formatDate(newDate.time.toString())
            //transactionDate = newDate.time

            startDate = newDate.time
            newDate.add(Calendar.MONTH,monthDuration)
            endDate = newDate.time

            tvStartDate.text = Utility.formatDate(startDate.toString())

            tvDuration.text = "${Utility.formatDate(startDate.toString())} to ${Utility.formatDate(endDate.toString())}"

        },newCalnedar.get(Calendar.YEAR),newCalnedar.get(Calendar.MONTH),newCalnedar.get(Calendar.DAY_OF_MONTH))

        linStartDate.setOnClickListener {
            datePickerDialog.show()
        }


        val window = dialog.window
        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT)

        val dialogButton: Button = dialog.findViewById<Button>(R.id.btn_add_confrm)

        dialogButton.setOnClickListener {
            val totalAmount = etTotalAmount.text.toString().trim()
            if (!TextUtils.isEmpty(totalAmount)){
                val today = Date().toString()
                val spent = 0
                val amount = Amount(totalAmount, today, totalAmount,spent.toString(),endDate, startDate)
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
                    //deleteEarlierTransactions(user,dbReference)
                    deleteTransactionsOutofDuration(user,dbReference,amount.startDate, amount.uptoDate)
                    Utility.showToast(context,"Amount added")
                    readBalancefromDB(context,dbReference,tvBalanec,tvDaysLeft)
                }else{
                    Utility.showToast(context,"There was an error in updating amount")
                }
            }

    }

    private fun deleteTransactionsOutofDuration(
        user: FirebaseUser?,
        dbReference: DatabaseReference,
        startDate: Date,
        uptoDate: Date
    ) {
        val deleteableTransactionIds = ArrayList<String>()
        deleteableTransactionIds.clear()
        dbReference.child("/${user?.displayName}/transactions")
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        p0.children.forEach {
                            val transaction = it.getValue(Transaction::class.java)

                            Log.d("delete_trans_error","${transaction?.amount} ${transaction?.transactDate!!.before(startDate)} ${transaction.transactDate!!.after(uptoDate)}")

                            if (Utility.noOfDaysBWTwoDates(startDate, transaction.transactDate!!).toInt()<0){
                                deleteableTransactionIds.add(transaction.transactionId!!)
                            }

                        }
                    }

                    if (deleteableTransactionIds.isNotEmpty()){
                        deleteableTransactionIds.forEach {
                            dbReference.child("/${user?.displayName}/transactions/$it").setValue(null)
                            Log.d("delete_error","deleteTransactionsOutofDuration")
                        }
                    }

                }
            })
    }

    private fun deleteEarlierTransactions(user: FirebaseUser?, dbReference: DatabaseReference) {
        dbReference.child("${user?.displayName}/transactions")
            .setValue(null)
        Log.d("delete_error","deleteEarlierTransactions")
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
                        val adapter = TransactionsRecyclerViewAdapter(context, sortedTransaction, this@HomeViewModel, dbReference)
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

    override fun onTransactionClicked(transaction: Transaction, context: Context, dbreference: DatabaseReference) {
        showTransactionInfoDialog(transaction,context,dbreference)
    }

    private fun showTransactionInfoDialog(transaction: Transaction,context: Context,dbReference: DatabaseReference) {
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
        val editCard = dialog.findViewById<ImageView>(R.id.img_edit_transaction)
        val linDescription = dialog.findViewById<LinearLayout>(R.id.lin_description_transaction)
        val imgDelete = dialog.findViewById<ImageView>(R.id.img_delete_transaction)

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

        if (transaction.note!!.isEmpty()){
            if (linDescription.visibility == View.VISIBLE){
                linDescription.visibility = View.GONE
            }
        }

        button.setOnClickListener {
            dialog.cancel()
        }

        editCard.setOnClickListener {
            if (dialog.isShowing){
                dialog.cancel()
                showTransactionDialog(context,dbReference,transaction)
            }
        }


        imgDelete.setOnClickListener {
            removeTransactionFromDB(context,transaction,dbReference, transaction.transactionId!!)
            if (dialog.isShowing){
                dialog.cancel()
            }
        }

        dialog.show()
        dialog.setCanceledOnTouchOutside(true)
    }
}