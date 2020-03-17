package com.example.pocketmanager.home.ui

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.pocketmanager.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {

    private var viewModel : HomeViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]

        val dbReference = FirebaseDatabase.getInstance().reference

        btn_add_total_amount.setOnClickListener {
            viewModel!!.showAddAmountDialog(this,dbReference)
        }

        btn_add_transaction.setOnClickListener {
            viewModel!!.showTransactionDialog(this,dbReference)
        }

    }
}
