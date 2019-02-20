package com.bunkalogic.bunkalist.Activities.LoginActivities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bunkalogic.bunkalist.Activities.MainActivity
import com.bunkalogic.bunkalist.Others.isValidEmail
import com.bunkalogic.bunkalist.Others.isValidPassword
import com.bunkalogic.bunkalist.Others.validate
import com.bunkalogic.bunkalist.R
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast

/**
 *  Created by @author Naim Dridi on 20/02/19
 */

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {


    // Variables
    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mGoogleApiClient: GoogleApiClient by lazy { getGoogleApiClient() }

    private  val RC_GOOGLE_SIGN_IN = 99


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        clickListeners()
    }

    // This function is responsible for checking if the user has confirmed the email
    private fun logInByEmail(email: String, password: String){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if (task.isSuccessful){
                if (mAuth.currentUser!!.isEmailVerified){
                    startActivity(intentFor<MainActivity>().newTask().clearTask())
                }else{
                    toast(R.string.user_confirm_email)
                }
            }else{
                toast(R.string.login_error)
            }

        }

    }


    // Function to collect the Api from Google+
    private fun  getGoogleApiClient(): GoogleApiClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        return GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    // checking that you have a google open account this terminal
    private fun loginByGoogleAccountIntoFirebase(googleAccount: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this){
            if (mGoogleApiClient.isConnected){
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            startActivity(intentFor<MainActivity>().newTask().clearTask())
        }
    }

    // function that contain the clickListener of elements of the graphical interface
    private fun clickListeners(){

        buttonLogIn.setOnClickListener {
            // I collect the values ​​written in the editText and I check without valid
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (isValidEmail(email) && isValidPassword(password)){
                logInByEmail(email, password)
            }else{
                toast("Please make sure all the data is correct")
            }

        }

        editTextEmail.validate {

            editTextEmail.error = if (isValidEmail(it)) null else " Email is not valid "
        }

        editTextPassword.validate {

            editTextPassword.error = if (isValidPassword(it)) null else " Your password should contain 8 characters length at least"
        }


        textViewForgotPassword.setOnClickListener {
            startActivity(intentFor<ForgotPasswordActivity>())
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        buttonLogInGoogle.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        buttonCreateAccount.setOnClickListener {
            startActivity(intentFor<SignUpActivity>())
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }


    // Overwrite this method to collect the result after the Login with Google
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess){
                val account = result.signInAccount
                loginByGoogleAccountIntoFirebase(account!!)
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        toast("connection failed")
    }
}
