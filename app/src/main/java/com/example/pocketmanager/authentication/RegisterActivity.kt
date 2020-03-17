package com.example.pocketmanager.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmanager.HomeActivity
import com.example.pocketmanager.utils.LoaderInterface
import com.example.pocketmanager.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity(),
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
                    register(email,password)
                }else{
                    Toast.makeText(this,"Password should be of at least 8 characters",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Enter credentials carefully",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun register(email: String, password: String) {
        mAuth?.createUserWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
                if (it.isSuccessful){
                    hideLoader()
                    val user = mAuth?.currentUser
                    updateUi(user)
                }else{
                    hideLoader()
                    Toast.makeText(this,"Registration failed!",Toast.LENGTH_SHORT).show()
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
