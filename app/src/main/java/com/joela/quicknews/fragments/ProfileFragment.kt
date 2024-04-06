/*
Fragment to show user's profile details
 */

package com.joela.quicknews.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import com.joela.quicknews.activities.PreferencesActivity
import com.joela.quicknews.R
import com.joela.quicknews.activities.WelcomeActivity
import com.joela.quicknews.databinding.FragmentProfileBinding
import com.joela.quicknews.models.ProfileViewModel


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    var auth = FirebaseAuth.getInstance()
    private val viewModel: ProfileViewModel by activityViewModels()

    //load layout and initialise fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val email = auth.currentUser?.email
        val userid = auth.currentUser?.uid.toString()
        /*
        Observe the user's phone and username from viewModel and
        update the TextView accordingly
        */
        viewModel.phone.observe(viewLifecycleOwner, Observer{ item->
            binding.phoneTextview.setText(item.toString())
        })
        viewModel.username.observe(viewLifecycleOwner, Observer{ item->
            binding.usernameTextview.setText(item.toString())
        })
        binding.emailTextview.text = email.toString()

        val changeEmail = binding.changeemailButton
        val resetPassword = binding.resetpasswordButton
        val changePhone = binding.changephoneButton
        val logout = binding.logoutButton
        val delete = binding.deleteaccountButton
        val changePreferences = binding.setpreferencesButton

        /*
        Observe viewModel for email verified property and
        activate the reset password button and change phone no. button
        if email is verified
        */
        viewModel.emailverified.observe(viewLifecycleOwner, Observer{ item->
            if(viewModel.emailverified.value == false){
                binding.resetpasswordButton.isActivated = false
                binding.changephoneButton.isActivated = false
            }
            else{
                binding.resetpasswordButton.isActivated = true
                binding.changephoneButton.isActivated = true
            }
        })

        /*
        Change/Verify Email Button is clicked
        for changing user's email to new email and verify the email
        */
        changeEmail.setOnClickListener(){
            val builder = AlertDialog.Builder(activity)
            //show custom alert dialog to enter new email
            val dialogLayout = inflater.inflate(R.layout.change_email_layout, null)
            val editEmail = dialogLayout.findViewById<EditText>(R.id.newemail_edittext)
            //Custom builder for dialog
            with(builder){
                setTitle("Enter the new Email")
                editEmail.setText(email)
                //OK button is clicked
                setPositiveButton("OK"){dialog, which->

                    val user = auth.currentUser
                    //User can enter new email address or keep current email to verify
                    val new_email = editEmail.text.toString()

                    /*
                    Send new/current email to Firebase Authentication service to update/verify
                    Email must be verified before updating
                    */
                    user!!.verifyBeforeUpdateEmail(new_email)
                        .addOnCompleteListener { task ->
                            //Verification email to new email address successfully sent
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "Verification Link has been sent to your new Email", Toast.LENGTH_SHORT).show()
                                auth.signOut()
                                Toast.makeText(activity, "Sign in with new Email after verification", Toast.LENGTH_SHORT).show()
                                activity?.finish()
                            }
                            else{
                                //Verification email to new/current email address failed to be sent
                                try{
                                    throw task.getException()!!
                                }
                                //New/current email is invalid
                                catch(e: FirebaseAuthInvalidUserException){
                                    Toast.makeText(activity, "Enter a valid Email address", Toast.LENGTH_LONG).show()
                                }
                                //User needs to re-login to send verification email
                                catch (e: FirebaseAuthException) {
                                    Toast.makeText(
                                        activity,
                                        "Couldnt send verification email, try signing out and sign in",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }
                        }
                }
                //Cancle button is clicked
                setNegativeButton("Cancle"){dialog, which->
                    Toast.makeText(activity, "Operation Cancled", Toast.LENGTH_SHORT).show()
                }
                setView(dialogLayout)
                show()
            }

        }

        /*
        Reset password button is clicked
        for changing the User's password
        */
        resetPassword.setOnClickListener(){
            //Email must be verified for button to be activated
            if(binding.resetpasswordButton.isActivated == false){
                Toast.makeText(activity, "Verify Email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Send password reset link to email address
            auth.sendPasswordResetEmail(email.toString())
                .addOnCompleteListener{ task ->
                    //Password reset email sent successfully
                    if(task.isSuccessful){
                        Toast.makeText(activity, "Password reset email has been sent", Toast.LENGTH_SHORT).show()
                    }
                    //reset email failed to be sent
                    else {
                        try {
                            throw task.getException()!!
                        }
                        //User needs to re-login to authenticate and send reset email
                        catch (e: FirebaseAuthException) {
                            Toast.makeText(
                                activity,
                                "Couldnt send password reset email, try signing out and sign in",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }

        //Logout button is clicked
        logout.setOnClickListener(){
            auth.signOut()
            //Welcome page is shown
            val intent = Intent(activity, WelcomeActivity::class.java)
            startActivity(intent)
            activity?.finish()
        }

        //Account deletion button is clicked
        delete.setOnClickListener() {
            //Alert dialog is shown for confirmation
            val builder = AlertDialog.Builder(activity)
            with(builder) {
                setTitle("Are you sure?")
                //OK button is clicked
                setPositiveButton("OK") { dialog, which ->

                    val user = auth.currentUser

                    //Delete request is sent to Firebase Authentication service
                    user!!.delete()
                        .addOnCompleteListener { task ->
                            //Delete request is successful
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    activity,
                                    "Your account will be deleted",
                                    Toast.LENGTH_SHORT
                                ).show()
                                //Firebase Database data is deleted
                                FirebaseDatabase.getInstance().getReference("users/" + userid).removeValue()
                                FirebaseDatabase.getInstance().getReference("preferences/" + userid).removeValue()
                                FirebaseDatabase.getInstance().getReference("favourites/" + userid).removeValue()
                                //User is signed out
                                auth.signOut()
                                //Welcome page is shown
                                val intent = Intent(activity, WelcomeActivity::class.java)
                                startActivity(intent)
                                activity?.finish()
                            }
                            //delete request failed
                            else {
                                try {
                                    throw task.getException()!!
                                } catch (e: FirebaseAuthException) {
                                    Toast.makeText(
                                        activity,
                                        "Couldnt delete account, try signing out and sign in",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                            }
                        }
                }
                //Cancle button is clicked
                setNegativeButton("Cancle") { dialog, which ->
                    Toast.makeText(activity, "Operation Cancled", Toast.LENGTH_SHORT).show()
                }
                builder.setMessage("Your account will be permanently deleted")
                show()
            }
        }

        /*
        Change phone button is clicked
         */
        changePhone.setOnClickListener(){
            //Email must be verified for changing phone no.
            if(binding.changephoneButton.isActivated == false){
                Toast.makeText(activity, "Verify Email first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val builder = AlertDialog.Builder(activity)
            val dialogLayout = inflater.inflate(R.layout.change_phone_layout, null)
            //Alert Dialog is shown for entering phone no.
            val editPhone = dialogLayout.findViewById<EditText>(R.id.newphone_edittext)
            with(builder) {
                setTitle("Enter the new Phone Number")
                //OK button clicked
                setPositiveButton("OK") { dialog, which ->

                    val new_phone = editPhone.text.toString()
                    if(new_phone.length<9){
                        Toast.makeText(activity, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        val databaseRef = FirebaseDatabase.getInstance().getReference("users/" + userid)
                        //enter new phone no. in database
                        databaseRef.child("phone").setValue(new_phone)
                            .addOnCompleteListener() { task ->
                                //phone no. changed successfully
                                if(task.isSuccessful){
                                    Toast.makeText(activity, "Phone No. has been changed", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    Toast.makeText(activity, "Phone No. could not be changed", Toast.LENGTH_SHORT).show()
                                    Log.w("Profile Fragment", "Phone No. could not be changed "+task.exception.toString())
                                }
                            }
                    }
                }
                //Cancle button clicked
                setNegativeButton("Cancle") { dialog, which ->
                    Toast.makeText(activity, "Operation Cancled", Toast.LENGTH_SHORT).show()
                }
                setView(dialogLayout)
                show()
            }
        }

        //Change preferences button clicked
        changePreferences.setOnClickListener(){
            viewModel.updateGotoProfile(true)
            //Opens preferences Activity
            val intent = Intent(activity, PreferencesActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    //delete binding if fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}