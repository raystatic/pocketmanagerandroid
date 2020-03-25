package com.walletLog.pocketmanager.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.walletLog.pocketmanager.home.ui.HomeActivity
import com.walletLog.pocketmanager.utils.LoaderInterface
import com.walletLog.pocketmanager.R
import com.walletLog.pocketmanager.utils.BaseActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(),
    LoaderInterface {

    private var mAuth: FirebaseAuth?=null
    private var email:String = ""
    private var password:String = ""
    private var progressDialog : ProgressDialog?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)

        tv_reg_login.setOnClickListener {
            startActivity(Intent(this,
                RegisterActivity::class.java))
            finish()
        }

        btn_login.setOnClickListener {
            showLoader()
            email = et_email_login.text.toString().trim()
            password = et_password_login.text.toString().trim()

            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                login(email,password)
            }else{
                Toast.makeText(this,"Please fill credentials carefully",Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun login(email: String, password: String) {
        mAuth?.signInWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
                if(it.isSuccessful){
                    hideLoader()
                    val user = mAuth?.currentUser
                    updateUI(user)
                }else{
                    hideLoader()
                    Toast.makeText(this,"Login failed",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user!=null){
            startActivity(Intent(this@LoginActivity,
                HomeActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth?.currentUser
        if(user!=null){
            updateUI(user)
        }
    }

    override fun showLoader() {
        progressDialog?.setMessage("Logging In...")
        progressDialog?.show()
    }

    override fun hideLoader() {
        if(progressDialog!=null && progressDialog?.isShowing!!){
            progressDialog?.hide()
        }
    }
}
