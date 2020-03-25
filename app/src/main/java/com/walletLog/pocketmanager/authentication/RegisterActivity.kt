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
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : BaseActivity(),
    LoaderInterface {

    private var mAuth: FirebaseAuth? = null
    private var email:String = ""
    private var password:String = ""
    private var progressDialog: ProgressDialog?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)

        tv_login_reg.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btn_reg.setOnClickListener {
            showLoader()
            email = et_email_reg.text.toString().trim()
            password = et_password_reg.text.toString().trim()

            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                if (password.length>=8){
                    val username = email.replace(".","_")
                    register(email,password,username)
                }else{
                    Toast.makeText(this,"Password should be of at least 8 characters",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Enter credentials carefully",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun register(email: String, password: String, name:String) {
        mAuth?.createUserWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
                if (it.isSuccessful){
                    val user = mAuth?.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name).build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener {it1->
                            hideLoader()
                            if(it1.isSuccessful){
                                updateUi(user)
                            }else{
                                hideLoader()
                                Toast.makeText(this,"Registration Profile Update failed!",Toast.LENGTH_SHORT).show()
                                updateUi(null)
                            }
                        }
                }else{
                    hideLoader()
                    Toast.makeText(this,"Registration failed! ${it.exception?.message}",Toast.LENGTH_SHORT).show()
                    updateUi(null)
                }
            }
    }

    private fun updateUi(user: FirebaseUser?) {
        if (user!=null){
            startActivity(Intent(this@RegisterActivity,
                HomeActivity::class.java))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth?.currentUser
        if(user!=null){
            updateUi(user)
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
