package com.bunkalogic.bunkalist.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bunkalogic.bunkalist.Activities.LoginActivities.LoginActivity
import com.bunkalogic.bunkalist.R
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class MainEmptyActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    // This activity is responsible for when the App is opened, for the transition to the LoginActivity or MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (mAuth.currentUser == null){
            startActivity(intentFor<LoginActivity>().newTask().clearTask())
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }else{
            startActivity(intentFor<MainActivity>().newTask().clearTask())
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }
}
