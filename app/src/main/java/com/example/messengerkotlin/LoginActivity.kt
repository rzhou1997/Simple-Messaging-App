package com.example.messengerkotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity(){

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.login).setOnClickListener {

            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            if(email.isEmpty()){
                findViewById<EditText>(R.id.editTextEmail).error = "Email cannot be empty!"
                findViewById<EditText>(R.id.editTextEmail).requestFocus()
                return@setOnClickListener
            }
            if(password.isEmpty()){
                findViewById<EditText>(R.id.editTextPassword).error = "Password cannot be empty"
                findViewById<EditText>(R.id.editTextPassword).requestFocus()
                return@setOnClickListener
            }


            //login with firebase
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnSuccessListener {
                    val user = FirebaseAuth.getInstance().currentUser
                    if(user?.isEmailVerified!!) {
                        val intentToMessagesBoardActivity = Intent(this, MessagesBoardActivity::class.java)
                        intentToMessagesBoardActivity.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intentToMessagesBoardActivity)
                    }else{
                        Toast.makeText(this,"Email has not been verified yet.", Toast.LENGTH_LONG).show()
                    }
                }
        }


        findViewById<TextView>(R.id.registerPage).setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

    }
}