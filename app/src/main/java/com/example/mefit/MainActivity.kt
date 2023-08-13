package com.example.mefit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mefit.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var currentUser: FirebaseUser? = null

    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth!!.currentUser

        login()
    }

    private fun login() {
        binding.loginButton.setOnClickListener {
            if (binding.inEmail.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter Phone Number", Toast.LENGTH_SHORT).show()
            } else if (binding.inEmail.text.toString().trim { it <= ' ' }.length < 10) {
                Toast.makeText(this@MainActivity, "Invalid Phone Number", Toast.LENGTH_SHORT).show()
            } else {
                otpSend()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        if (currentUser != null) {
            val intent = Intent(this@MainActivity, CalorieGain::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    private fun otpSend() {
        binding.progressBar!!.visibility = View.VISIBLE
        binding.loginButton!!.visibility = View.INVISIBLE
        val callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }
            override fun onVerificationFailed(e: FirebaseException) {
                binding.progressBar!!.visibility = View.GONE
                binding.loginButton!!.visibility = View.VISIBLE
                Log.d("TAG", "onVerificationFailed: " + e.localizedMessage)
                Toast.makeText(this@MainActivity, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: ForceResendingToken) {
                binding.progressBar!!.visibility = View.GONE
                binding.loginButton!!.visibility = View.VISIBLE
                val intent = Intent(this@MainActivity, Verify::class.java)
                intent.putExtra("vfId", verificationId)
                startActivity(intent)
            }
        }
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(binding.inEmail!!.text.toString().trim { it <= ' ' })
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}