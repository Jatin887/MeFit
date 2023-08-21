package com.example.mefit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mefit.databinding.ActivityVerifyBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Verify : AppCompatActivity() {
    private var verificationId: String? = null
    private lateinit var binding : ActivityVerifyBinding
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val etC1 = binding.etC1
        val etC2 = binding.etC2
        val etC3 = binding.etC3
        val etC4 = binding.etC4
        val etC5 = binding.etC5
        val etC6 = binding.etC6


        val editTexts: List<EditText> by lazy {
            listOf(etC1, etC2, etC3, etC4, etC5, etC6)
        }

        setupEditTextListeners(editTexts)

        verificationId = intent.getStringExtra("vfId")
        editTextInput()
        binding.tvResendBtn.setOnClickListener(View.OnClickListener {
            Toast.makeText(
                this@Verify,
                "OTP sent Successfully",
                Toast.LENGTH_SHORT
            ).show()
        })

        binding.verifyButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                binding.progressBarVerify.visibility = View.VISIBLE
                binding.verifyButton.visibility = View.INVISIBLE
                if ((etC1.text.toString().trim { it <= ' ' }.isEmpty() ||
                            etC2?.getText().toString().trim { it <= ' ' }.isEmpty() ||
                            etC3?.getText().toString().trim { it <= ' ' }.isEmpty() ||
                            etC4?.getText().toString().trim { it <= ' ' }.isEmpty() ||
                            etC5?.getText().toString().trim { it <= ' ' }.isEmpty() ||
                            etC6?.getText().toString().trim { it <= ' ' }.isEmpty())
                ) {
                    Toast.makeText(this@Verify, "OTP is not Valid", Toast.LENGTH_SHORT).show()
                } else if (verificationId != null) {
                    val code = (etC1?.getText().toString().trim { it <= ' ' } +
                            etC2?.getText().toString().trim { it <= ' ' } +
                            etC3?.getText().toString().trim { it <= ' ' } +
                            etC4?.getText().toString().trim { it <= ' ' } +
                            etC5?.getText().toString().trim { it <= ' ' } +
                            etC6?.getText().toString().trim { it <= ' ' })
                    val credential = PhoneAuthProvider.getCredential(
                        verificationId!!, code
                    )
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                binding.progressBarVerify.setVisibility(View.VISIBLE)
                                binding.verifyButton.setVisibility(View.INVISIBLE)
                                //Check if firestore collection user exists and it has a dcoument with the name same as uid
                                db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get()
                                    .addOnSuccessListener { document ->
                                        if (document.exists()) {
                                            //If user exists then go to CalorieGain Activity
                                            val intent = Intent(this@Verify, CalorieGain::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                        } else {
                                            //If user does not exist then go to Profile Activity
                                            val intent = Intent(this@Verify, Profile::class.java)
                                            intent.flags =
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(intent)
                                        }
                                    }
                            } else {
                                binding.progressBarVerify.setVisibility(View.GONE)
                                binding.verifyButton.setVisibility(View.VISIBLE)
                                Toast.makeText(
                                    this@Verify,
                                    "OTP is not Valid",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        })
    }


    private fun setupEditTextListeners(editTexts: List<EditText>) {
        for (i in 0 until editTexts.size - 1) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        editTexts[i + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        // Set last EditText's listener to hide the keyboard
        editTexts[5].setOnEditorActionListener { _, _, _ ->
            editTexts[5].clearFocus()
            // Optionally, you can hide the keyboard here
            true
        }
    }
    private fun editTextInput() {
        // Same logic here... you might want to consider refactoring this for brevity
        // ... (rest of your editTextInput method)
    }
}