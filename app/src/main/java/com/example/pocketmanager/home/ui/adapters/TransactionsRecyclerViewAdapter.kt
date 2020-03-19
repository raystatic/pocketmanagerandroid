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
                                      var transactions:ArrayList<Transaction>) :
    RecyclerView.Adapter<TransactionsRecyclerViewAdapter.TransactionsViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.transacations_adapter_layout,parent,false)

        return TransactionsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: TransactionsViewHolder, position: Int) {
        holder.bindView(transactions[position], context)
    }

    class TransactionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAmount = itemView.findViewById<TextView>(R.id.tv_amount_transaction)
        private val tvDate = itemView.findViewById<TextView>(R.id.tv_date_transaction)
      //  private val tvMode = itemView.findViewById<TextView>(R.id.tv_mode_transaction)
      //  private val tvReceiver = itemView.findViewById<TextView>(R.id.tv_receiver_transaction)
       // private val tvSender = itemView.findViewById<TextView>(R.id.tv_sender_transaction)
      //  private val tvType = itemView.findViewById<TextView>(R.id.tv_type_transaction)

        fun bindView(transaction: Transaction, context: Context){
            tvAmount.text =transaction.amount
            tvDate.text = Utility.formatDate(transaction.date)

            if (transaction.debit!!){
                tvAmount.setTextColor(context.resources.getColor(R.color.red))
            }else{
                tvAmount.setTextColor(context.resources.getColor(R.color.green))
            }

           // tvMode.text = transaction.mode
        //    tvReceiver.text = transaction.reciever
         //   tvSender.text = transaction.sender
           // tvType.text = transaction.type
        }

    }

}