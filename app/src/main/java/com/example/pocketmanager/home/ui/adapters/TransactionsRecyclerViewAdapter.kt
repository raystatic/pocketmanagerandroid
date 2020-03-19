package com.example.pocketmanager.home.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketmanager.R
import com.example.pocketmanager.home.model.Transaction
import com.example.pocketmanager.utils.Utility


class TransactionsRecyclerViewAdapter(var context:Context,
                                      var transactions:ArrayList<Transaction>,
                                      var listener: TransactionInteractor) :
    RecyclerView.Adapter<TransactionsRecyclerViewAdapter.TransactionsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transacations_adapter_layout,parent,false)

        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.bindView(transactions[position], context, listener)
    }

    class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount = itemView.findViewById<TextView>(R.id.tv_amount_transaction)
        private val tvDate = itemView.findViewById<TextView>(R.id.tv_date_transaction)

        fun bindView(transaction: Transaction, context: Context, listener: TransactionInteractor){
            tvAmount.text =transaction.amount
            tvDate.text = Utility.formatDate(transaction.date)

            if (transaction.debit!!){
                tvAmount.setTextColor(context.resources.getColor(R.color.red))
            }else{
                tvAmount.setTextColor(context.resources.getColor(R.color.green))
            }

            itemView.setOnClickListener {
                listener.onTransactionClicked(transaction,context)
            }

        }

    }

    interface TransactionInteractor{
        fun onTransactionClicked(transaction: Transaction, context: Context)
    }

}