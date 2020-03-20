package com.example.pocketmanager.home.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProviders
import com.example.pocketmanager.R
import com.example.pocketmanager.utils.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_home_action_bar_layout.*


class HomeActivity : BaseActivity() {

    private var viewModel : HomeViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]

        val dbReference = FirebaseDatabase.getInstance().reference

        home_loader.visibility = View.VISIBLE

        update_amount_card_action_bar.setOnClickListener {
            viewModel!!.showAddAmountDialog(this,dbReference)
        }

        add_transaction_card_action_bar.setOnClickListener {
            viewModel!!.showTransactionDialog(this,dbReference)
        }

        info_card_action_bar.setOnClickListener {
            viewModel!!.showInfoDialog(this,dbReference)
        }

        logout_card_action_bar.setOnClickListener {
            signout(this)
        }

        viewModel!!.readBalancefromDB(this,dbReference,tv_balance_action_bar, tv_days_left_action_bar)

        viewModel!!.readTransactions(this,dbReference,rv_transactions, home_loader)

    }

    fun signout(context: Context){
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sign out")
        builder.setMessage("Are you sure want to sign out?")
        builder.setPositiveButton("Yes") { dialog, which ->
            FirebaseAuth.getInstance().signOut()
            finish()
        }
        builder.setNegativeButton("No"){ dialog, which ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()

    }
}
