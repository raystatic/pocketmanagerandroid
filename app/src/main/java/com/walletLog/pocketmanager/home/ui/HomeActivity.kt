package com.walletLog.pocketmanager.home.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.walletLog.pocketmanager.R
import com.walletLog.pocketmanager.userflow.UserFlowActivity
import com.walletLog.pocketmanager.utils.BaseActivity
import com.walletLog.pocketmanager.utils.Constants
import com.walletLog.pocketmanager.utils.GenerateRandomString
import com.walletLog.pocketmanager.utils.PrefManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.custom_home_action_bar_layout.*
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig


class HomeActivity : BaseActivity() {

    private var viewModel : HomeViewModel?=null

    val dbReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefManager = PrefManager(this)
        if(!prefManager.getBoolean(Constants.NOTFIRSTRUN)!!){

            if(!prefManager.getBoolean(Constants.INTRODUCTION_DONE)!!){
                startActivity(Intent(this,UserFlowActivity::class.java))
                finish()
            }else{
                runTutorial()
                prefManager.saveBoolean(Constants.NOTFIRSTRUN,true)
            }
        }

        viewModel = ViewModelProviders.of(this)[HomeViewModel::class.java]

        home_loader.visibility = View.VISIBLE


        update_amount_card_action_bar.setOnClickListener {
            viewModel!!.showAddAmountDialog(this,dbReference, tv_balance_action_bar,tv_days_left_action_bar)
        }

        add_transaction_card_action_bar.setOnClickListener {
            viewModel!!.showTransactionDialog(this,dbReference, null)
            //viewModel!!.test(this)
        }

        info_card_action_bar.setOnClickListener {
            viewModel!!.showInfoDialog(this,dbReference)
        }

        logout_card_action_bar.setOnClickListener {
            signout(this)
        }

        viewModel!!.readBalancefromDB(this,dbReference,tv_balance_action_bar, tv_days_left_action_bar)

        viewModel!!.readTransactions(this,dbReference,rv_transactions, home_loader, tv_no_transaction)

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

    override fun onResume() {
        super.onResume()

    }

    private fun runTutorial() {
        val config = ShowcaseConfig()
        config.delay = 500 // half second between each showcase view

        val showcaseID = GenerateRandomString.randomString(30)
        val sequence = MaterialShowcaseSequence(this, showcaseID)

        sequence.setConfig(config)

        sequence.addSequenceItem(
            tv_balance_title,
            "This will show your balance", "GOT IT"
        )

        sequence.addSequenceItem(
            tv_days_left_title,
            "No of days left will be shown here", "GOT IT"
        )

        sequence.addSequenceItem(
            update_amount_card_action_bar,
            "Click this to add or update budget of the month", "GOT IT"
        )

        sequence.addSequenceItem(
            add_transaction_card_action_bar,
            "Click this to add a new transaction", "GOT IT"
        )

        sequence.addSequenceItem(
            info_card_action_bar,
            "Click this to a view summary of month", "GOT IT"
        )

        sequence.addSequenceItem(
            logout_card_action_bar,
            "Click this to sign out of the app", "GOT IT"
        )

        sequence.start()
    }

}
