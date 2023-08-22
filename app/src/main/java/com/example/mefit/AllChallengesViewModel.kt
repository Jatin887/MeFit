package com.example.mefit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.model.Challenge
import com.example.mefit.model.UserChallenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.System.currentTimeMillis
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllChallengesViewModel: ViewModel(){


    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var document = db.collection("users").document(user.uid)
    var mainChallengeList = MutableLiveData<List<Challenge>>()

    var mainUserChallengeList = MutableLiveData<List<UserChallenge>>()
    fun loadAllChallenges(){

        document.get()
            .addOnSuccessListener { documentSnapshot ->
                var userChallenges = listOf<UserChallenge>()
                val _userChallenges = documentSnapshot.get("userChallenges")  as? List<Map<String, Any>>
                if(_userChallenges != null) {
                    userChallenges = _userChallenges.map {
                        UserChallenge(
                            it["name"].toString(),
                            it["desc"].toString(),
                            it["id"].toString(),
                            it["duration"].toString().toInt(),
                            it["calories"].toString().toInt(),
                            it["rewards"].toString().toInt(),
                            it["startTime"].toString().toLong(),
                        )
                    }
                }
                mainUserChallengeList.value = userChallenges

            }
            .addOnFailureListener { e ->
                // Handle the error
            }
        db.collection("challenges").get().addOnSuccessListener { result ->
            var _challengess = mutableListOf<Challenge>()
            for (document in result) {
                //check if the challenge is already added by the user
                if(document.data["id"].toString() in mainUserChallengeList.value?.map { it.id }!!){
                    continue
                }
                _challengess.add(Challenge(document.data["name"].toString(), document.data["desc"].toString(),
                    document.data["id"].toString(), document.data["duration"].toString().toInt(),
                    document.data["calories"].toString().toInt(), document.data["rewards"].toString().toInt()))
            }
            mainChallengeList.value = _challengess


        }.addOnFailureListener {
            //Toast.makeText(requireContext(), "Failed to fetch challenges", Toast.LENGTH_SHORT).show()
        }
    }



    fun displayDialog(challenge: Challenge, context: Context){

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(challenge.name)
            .setMessage(challenge.desc)
            .setPositiveButton("Let's Do It") { dialogInterface: DialogInterface, _: Int ->
                onLetsDoItClicked(challenge, context)
                dialogInterface.dismiss()
            }
            .setNegativeButton("Maybe Later") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.show()


    }

    fun onLetsDoItClicked(challenge: Challenge, context: Context) {


        val newChallenge = UserChallenge(
            challenge.name,
            challenge.desc,
            challenge.id,
            challenge.duration,
             challenge.calories,
            challenge.rewards,
            currentTimeMillis(),
        )

        document.get()
            .addOnSuccessListener { documentSnapshot ->
                var userChallenges = listOf<UserChallenge>()
                val _userChallenges = documentSnapshot.get("userChallenges")  as? List<Map<String, Any>>
                if(_userChallenges != null) {
                    userChallenges = _userChallenges.map {
                        UserChallenge(
                            it["name"].toString(),
                            it["desc"].toString(),
                            it["id"].toString(),
                            it["duration"].toString().toInt(),
                            it["calories"].toString().toInt(),
                            it["rewards"].toString().toInt(),
                            it["startTime"].toString().toLong(),
                        )
                    }
                }
                if (userChallenges.isNullOrEmpty()) {
                    document.update("userChallenges", listOf(newChallenge))
                } else if(challenge.id !in userChallenges.map { it.id }) {
                    val updatedList = userChallenges.toMutableList()
                    updatedList.add(newChallenge)
                    document.update("userChallenges", updatedList).addOnCompleteListener {
                        Toast.makeText(
                            context,
                            "Challenge Accepted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    //uodate mainchallenge list and notify adapter
                    mainChallengeList.value = mainChallengeList.value?.filter { it.id != challenge.id }
                }else{
                    Toast.makeText(
                       context,
                        "You have already accepted this challenge",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle the error
            }
    }




    fun displayOngoingDialog(challenge: UserChallenge, context: Context){
        var desc = ""
        desc += "Reward: " + challenge.rewards + "\n"
        val endDateMillis = challenge.startTime + (challenge.duration * 24 * 60 * 60 * 1000)

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(endDateMillis))
        desc += "Ends At: $formattedDate\n\n"
        desc += challenge.desc + "\n\n"

        val alertDialog = AlertDialog.Builder(context)
            .setTitle(challenge.name)
            .setMessage(desc)
            .setPositiveButton("Got it") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .setNegativeButton("Invite Friend") { dialogInterface: DialogInterface, _: Int ->
                Toast.makeText(
                    context,
                    "Invite Sent to all Friends!",
                    Toast.LENGTH_SHORT
                ).show()
                dialogInterface.dismiss()
            }
            .create()

        alertDialog.show()


    }





}

