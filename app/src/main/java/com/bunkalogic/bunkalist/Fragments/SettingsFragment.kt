package com.bunkalogic.bunkalist.Fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bunkalogic.bunkalist.Activities.Login.LoginActivity
import com.bunkalogic.bunkalist.Activities.SettingsActivities.EditProfileActivity
import com.bunkalogic.bunkalist.Activities.SettingsActivities.ModeDayOrNightActivity
import com.bunkalogic.bunkalist.R
import com.bunkalogic.bunkalist.SharedPreferences.preferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_settings.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor

/**
 *  Created by @author Naim Dridi on 25/02/19
 */

class SettingsFragment : Fragment() {

    private lateinit var _view: View



    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _view =  inflater.inflate(R.layout.fragment_settings, container, false)
        clicksListeners()

        return _view
    }



    // function that contain the clickListener of elements of the graphical interface
    private fun clicksListeners(){

        _view.buttonApplyChanges.setOnClickListener { startActivity(intentFor<EditProfileActivity>()) }

        _view.buttonMode.setOnClickListener { startActivity(intentFor<ModeDayOrNightActivity>()) }

        _view.buttonSignOut.setOnClickListener {
            preferences.deleteAll()
            mAuth.signOut()
            startActivity(intentFor<LoginActivity>().newTask().clearTask())
        }
    }


}