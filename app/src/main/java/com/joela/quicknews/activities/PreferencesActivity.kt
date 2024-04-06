/*
Activity for Users to manage their news category preferences
 */

package com.joela.quicknews.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.ToggleButton
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.joela.quicknews.R
import com.joela.quicknews.databinding.ActivityPreferencesBinding
import com.joela.quicknews.models.ProfileViewModel

class PreferencesActivity : AppCompatActivity() {
    lateinit var binding: ActivityPreferencesBinding
    lateinit var auth: FirebaseAuth
    var selprefCount: Int = 0
    var selpref: ArrayList<String> = ArrayList()
    private val viewModel: ProfileViewModel by viewModels()

    //Load layout and initialise activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()

        var databaseRef = FirebaseDatabase.getInstance().getReference("preferences/"+auth.currentUser?.uid.toString())

        //Get the user's news category preferences
        databaseRef.get().addOnCompleteListener(){
            if(it.result.exists()){
                var prefsel: ArrayList<String> = it.result.value as ArrayList<String>

                /*
                Update the buttons based on which preference is saved on database
                The buttons are highlighted
                */
                for (pref in prefsel){
                    when(pref){
                        binding.businessButton.text ->{
                            checkButton(binding.businessButton as View)
                        }
                        binding.healthButton.text ->{
                            checkButton(binding.healthButton as View)
                        }
                        binding.sportsButton.text ->{
                            checkButton(binding.sportsButton as View)
                        }
                        binding.technologyButton.text ->{
                            checkButton(binding.technologyButton as View)
                        }
                        binding.entertainmentButton.text ->{
                            checkButton(binding.entertainmentButton as View)
                        }
                        binding.scienceButton.text ->{
                            checkButton(binding.scienceButton as View)
                        }
                    }
                }

            }
        }

    }

    //Buttons are checked based on the original preference list
    fun checkButton(view: View){
        val button: ToggleButton = view as ToggleButton
        view.setBackgroundResource(R.drawable.preferences_togglebutton_on)
        button.setTextColor(getColor(R.color.black))
        button.isChecked = true
        selpref.add(button.text.toString())
        selprefCount++
    }

    //Clicked on the save button
    fun saveClick(view: View){
        //Save preferences to database
        var databaseRef = FirebaseDatabase.getInstance().getReference("preferences/"+auth.currentUser?.uid.toString())
        databaseRef.setValue(selpref)

        //If user is from Sign up page go to Home Activity
        //Or else Profile Activity
        if(viewModel.gotoprofile.value == false){
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        else{
            viewModel.updateGotoProfile()
            finish()
        }

        Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show()
        finish()
    }

    //Buttons are checked based on the user's click
    fun preferencesClick(view: View){
        val button: ToggleButton = view as ToggleButton
        if(button.isChecked) {
            view.setBackgroundResource(R.drawable.preferences_togglebutton_on)
            button.setTextColor(getColor(R.color.black))
            selpref.add(button.text.toString())
            selprefCount++
        }
        else {
            view.setBackgroundResource(R.drawable.preferences_togglebutton_off)
            button.setTextColor(getColor(R.color.white))
            selpref.remove(button.text.toString())
            selprefCount--
        }

        //Only 3 categories can be selected
        if(selprefCount>3){
            Toast.makeText(this, "Select only 3", Toast.LENGTH_SHORT).show()
            view.isChecked = false
            view.setBackgroundResource(R.drawable.preferences_togglebutton_off)
            button.setTextColor(getColor(R.color.white))
            selpref.remove(button.text.toString())
            selprefCount--
        }
    }
}