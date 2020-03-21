package com.example.pocketmanager.home.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanager.R
import com.example.pocketmanager.home.model.Transaction
import com.example.pocketmanager.utils.Utility
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import java.util.*
import kotlin.collections.ArrayList


class TransactionsRecyclerViewAdapter(var context:Context,
                                      var transactions:ArrayList<Transaction>,
                                      var listener:TransactionInteractor) :
    RecyclerView.Adapter<TransactionsRecyclerViewAdapter.TransactionsViewHolder>() {


//    private val childEventListener: ChildEventListener?

    init {
//        val childEventListener  = object : ChildEventListener {
//            override fun onCancelled(p0: DatabaseError) {
//                Utility.showToast(context,"Unable to read transactions!")
//            }
//
//            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
//
//            }
//
//            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
//
//            }
//
//            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
//                val transaction = p0.getValue(Transaction::class.java)
//                if (Date().after(transaction?.transactDate)){
//                    transactions.add(transaction!!)
//                    notifyItemInserted(transactions.size - 1)
//                }
//
//            }
//
//            override fun onChildRemoved(p0: DataSnapshot) {
//                notifyDataSetChanged()
//            }
//        }
//
//        transactionsReference.addChildEventListener(childEventListener)
//        this.childEventListener = childEventListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transacations_adapter_layout,parent,false)

        return TransactionsViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.bindView(transactions[position], context,listener)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount = itemView.findViewById<TextView>(R.id.tv_amount_transaction)
        private val tvDate = itemView.findViewById<TextView>(R.id.tv_date_transaction)
        private val debitView = itemView.findViewById<View>(R.id.view_debit)
        private val creditView = itemView.findViewById<ImageView>(R.id.view_credit)

        fun bindView(transaction: Transaction, context: Context, listener: TransactionInteractor){
            tvDate.text = Utility.formatDate(transaction.date)

            if (transaction.debit!!){
                tvAmount.text ="- ${transaction.amount}"
                tvAmount.setTextColor(context.resources.getColor(R.color.red))
//                debitView.visibility = View.VISIBLE
//                creditView.visibility = View.GONE

            }else{
                tvAmount.text ="+ ${transaction.amount}"
                tvAmount.setTextColor(context.resources.getColor(R.color.green))
//                debitView.visibility = View.GONE
//                creditView.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                listener.onTransactionClicked(transaction,context)
            }

        }

    }

    interface TransactionInteractor{
        fun onTransactionClicked(transaction: Transaction, context: Context)
        fun onTransactionsLoaded(progressBar: ProgressBar, msg:String?, context: Context)
    }

}