package com.example.messengerkotlin
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.parcel.Parcelize
import java.util.*


@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {


    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        findViewById<Button>(R.id.register).setOnClickListener{

            val name = findViewById<EditText>(R.id.editTextUserName).text.toString()
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()

            //validation
            if(name.isEmpty()){
                findViewById<EditText>(R.id.editTextUserName).error = "Name cannot be empty"
                findViewById<EditText>(R.id.editTextUserName).requestFocus()
                return@setOnClickListener
            }

            if(email.isEmpty()){
                findViewById<EditText>(R.id.editTextEmail).error = "Email cannot be empty!"
                findViewById<EditText>(R.id.editTextEmail).requestFocus()
                return@setOnClickListener
            }

            if(password.isEmpty()){
                findViewById<EditText>(R.id.editTextPassword).error = "Password cannot be empty!"
                findViewById<EditText>(R.id.editTextPassword).requestFocus()
                return@setOnClickListener
            }else if(password.length < 6){
                findViewById<EditText>(R.id.editTextPassword).error = "Password must have 6 or more characters"
                findViewById<EditText>(R.id.editTextPassword).requestFocus()
                return@setOnClickListener
            }
            // Firebase authentication for registration of user.
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    //checking if the actual creation was successful on this line
                    if(!it.isSuccessful) {
                        return@addOnCompleteListener
                    }else if(it.isSuccessful){
                        //saves users to database
                        val uidValue = FirebaseAuth.getInstance().uid ?: ""
                        val referenceUser = FirebaseDatabase.getInstance().getReference("/users/$uidValue")
                        referenceUser.setValue(User(uidValue, name, email, password))
                        //once it's successfully created we will send the user to their messages board
                        val intent = Intent(this, MessagesBoardActivity::class.java)
                        /*clearing all previous activities that way when they press to go back on their
                        device it will send them to their home screen phone page rather than the registeractivity page.*/
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                }//if creation fails
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        findViewById<TextView>(R.id.accountExist).setOnClickListener{
            startActivity(Intent(this,LoginActivity::class.java))
        }


    }

}
/*A Parcelable is the Android implementation of the Java Serializable. ...
This way a Parcelable can be processed relatively fast, compared to the standard Java serialization.
 To allow your custom object to be parsed to another component they need to implement the android.
 With this we are able to pass our names from our users and be able to put their info in the chat
 logs we can also get their picture and display it in our chat activity*/
@Parcelize
class User(val uid:String, val name: String, val email: String, val password: String): Parcelable {
    constructor():this("","","","")
}