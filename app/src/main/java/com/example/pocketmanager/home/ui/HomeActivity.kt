package com.example.pocketmanager.home.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProviders
import com.example.pocketmanager.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_home_action_bar_layout.*


class HomeActivity : AppCompatActivity() {

    private var viewModel : HomeViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]

        val dbReference = FirebaseDatabase.getInstance().reference

        update_amount_card_action_bar.setOnClickListener {
            viewModel!!.showAddAmountDialog(this,dbReference)
        }

        add_transaction_card_action_bar.setOnClickListener {
            viewModel!!.showTransactionDialog(this,dbReference)
        }

        info_card_action_bar.setOnClickListener {
            viewModel!!.showInfoDialog(this,dbReference)
        }

        viewModel!!.readBalancefromDB(this,dbReference,tv_balance_action_bar)

        viewModel!!.readTransactions(this,dbReference,rv_transactions)

    }
}
