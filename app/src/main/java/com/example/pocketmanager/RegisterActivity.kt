package com.example.pocketmanager

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var email:String = ""
    private var password:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        btn_reg.setOnClickListener {
            email = et_email_reg.text.toString().trim()
            password = et_password_reg.text.toString().trim()

            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                register(email,password)
            }else{
                Toast.makeText(this,"Enter credentials carefully",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun register(email: String, password: String) {
        mAuth?.createUserWithEmailAndPassword(email,password)
            ?.addOnCompleteListener {
                if (it.isSuccessful){
                    val user = mAuth?.currentUser
                    updateUi(user)
                }else{
                    Toast.makeText(this,"Registration failed!",Toast.LENGTH_SHORT).show()
                    updateUi(null)
                }
            }
    }

    private fun updateUi(user: FirebaseUser?) {
        if (user!=null){
            startActivity(Intent(this@RegisterActivity,HomeActivity::class.java))
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
}
