/*
Activity for Users to create an account
 */

package com.joela.quicknews.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.joela.quicknews.databinding.ActivitySignupBinding
import com.joela.quicknews.models.User

class SignupActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignupBinding

    //Load layout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }

    //Sign up button is clicked
    fun signupClick(view: View){
        auth = FirebaseAuth.getInstance()

        val email: String = binding.emailEdittext.text.toString()
        val password: String = binding.passwordEdittext.text.toString()
        val confpass: String = binding.confirmpasswordEdittext.text.toString()

        val phone = binding.phoneEdittext.text.toString()
        val username = binding.usernameEdittext.text.toString()

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show()
            binding.emailEdittext.requestFocus()
            return
        }
        if(TextUtils.isEmpty(password) or TextUtils.isEmpty(confpass)){
            Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show()
            binding.passwordEdittext.requestFocus()
            return
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Enter a username", Toast.LENGTH_SHORT).show()
            binding.usernameEdittext.requestFocus()
            return
        }
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Enter your phone no.", Toast.LENGTH_SHORT).show()
            binding.phoneEdittext.requestFocus()
            return
        }
        if(!TextUtils.isDigitsOnly(phone) or (phone.length<10)){
            Toast.makeText(this, "Enter valid phone no.", Toast.LENGTH_SHORT).show()
            binding.phoneEdittext.requestFocus()
            return
        }
        if(password != confpass){
            Toast.makeText(this, "The passwords do not match", Toast.LENGTH_SHORT).show()
            binding.confirmpasswordEdittext.requestFocus()
            return
        }
        if(password.length <6){
            Toast.makeText(this, "Enter atleast 6 characters for password", Toast.LENGTH_SHORT).show()
            binding.passwordEdittext.requestFocus()
            return
        }

        //Send new user's details to Firebase Authentication service
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User created and signed-in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                    val createdUserid = task.result.user?.uid.toString()

                    var databaseRef = FirebaseDatabase.getInstance().getReference("users/"+createdUserid)
                    val user = User(createdUserid, phone, username)

                    //Save userID, phone and username on database
                    databaseRef.setValue(user)
                    //Open Preferences Activity
                    val intent = Intent(this, PreferencesActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    //User creation failed
                    val e: FirebaseAuthInvalidCredentialsException
                    try{
                        throw task.getException()!!
                    }
                    //Invalid Email address
                    catch(e: FirebaseAuthInvalidCredentialsException) {
                        binding.emailEdittext.setError("Enter Valid Email")
                        binding.emailEdittext.requestFocus()
                        Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show()
                    }
                    //Email already used by another user
                    catch(e: FirebaseAuthUserCollisionException) {
                        binding.emailEdittext.setError("Enter a different Email")
                        binding.emailEdittext.requestFocus()
                        Toast.makeText(this, "This email is already in use", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}