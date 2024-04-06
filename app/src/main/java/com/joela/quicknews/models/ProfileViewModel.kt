/*
Models the User Profile details
Can be updated live with listeners and
Can be accessed from any class or file
Can also set the properties to be observed for changes
 */

package com.joela.quicknews.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel: ViewModel() {

    private val mutableUsername = MutableLiveData<String>()
    private val mutablePhone = MutableLiveData<String>()
    private val mutableGotoProfile = MutableLiveData<Boolean>()
    private val mutableEmailVerified = MutableLiveData<Boolean>()
    private val mutablePrefList = MutableLiveData<ArrayList<String>?>()
    private val mutablePrefNotSet = MutableLiveData<Boolean>()
    val username: LiveData<String> get() = mutableUsername
    val phone: LiveData<String> get() = mutablePhone
    val gotoprofile: LiveData<Boolean> get() = mutableGotoProfile
    val emailverified: LiveData<Boolean> get() = mutableEmailVerified
    val preflist: LiveData<ArrayList<String>?> get() = mutablePrefList
    val prefnotset: LiveData<Boolean> get() = mutablePrefNotSet


    fun update(username: String, phone: String) {
        mutableUsername.value = username
        mutablePhone.value = phone
    }

    fun updateGotoProfile(gotoprofile: Boolean = false){
        mutableGotoProfile.value = gotoprofile
    }

    fun updateEmailVerified(verified: Boolean){
        mutableEmailVerified.value = verified
    }

    fun updatePrefList(preflist: ArrayList<String>?){
        mutablePrefList.value = preflist
    }

    fun updatePrefNotSet(prefset: Boolean){
        mutablePrefNotSet.value = prefset
    }
}