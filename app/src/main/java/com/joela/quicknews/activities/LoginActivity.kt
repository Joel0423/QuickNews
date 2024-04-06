/*
Activity for users to Login
 */

package com.joela.quicknews.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joela.quicknews.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivityLoginBinding

    //Initialise authentication instance and current user
    override fun onStart() {
        super.onStart()

        auth = FirebaseAuth.getInstance()
        val currentUser: FirebaseUser? = auth.currentUser

    }

    //Loading the layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    //login button clicked
    fun loginClick(view: View){

        val email: String = binding.emailEdittextLogin.text.toString()
        val password: String = binding.passwordEdittextLogin.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show()
            binding.emailEdittextLogin.requestFocus()
            return
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show()
            binding.passwordEdittextLogin.requestFocus()
            return
        }

        if(password.length <6){
            Toast.makeText(this, "Enter atleast 6 characters for password", Toast.LENGTH_SHORT).show()
            binding.passwordEdittextLogin.requestFocus()
            return
        }

        //Sending login details to Firebase Authentication service
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Successfully logged in", Toast.LENGTH_SHORT).show()
                    gotoHome()
                    finish()
                } else {
                    //Sign in failed
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
    }

    //Opens Home Activity after login
    fun gotoHome(){
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}