/*
Starting Activity of the Application
 */

package com.joela.quicknews.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.joela.quicknews.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth

    //Initialise Firebase Auth instance
    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()

        //If user is logged in go straight to Home Activity
        val currentUser: FirebaseUser? = auth.currentUser
        if(currentUser != null){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    //Load layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    //Opens sign-up Activity
    fun gotoSignup(view: View){
        val intent = Intent(this, SignupActivity::class.java)
        startActivity(intent)
    }

    //Opens Login Activity
    fun gotoLogin(view: View){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}