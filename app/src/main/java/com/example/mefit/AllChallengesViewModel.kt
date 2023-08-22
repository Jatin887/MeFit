package com.example.mefit

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.adapter.ChallengeAdapter
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

    var isTrue = MutableLiveData<Boolean>().apply { value = true }

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
                mainUserChallengeList.postValue(userChallenges)

                db.collection("challenges").get().addOnSuccessListener { result ->
                    var _challengess = mutableListOf<Challenge>()
                    for (document in result) {
                        //check if the challenge is already added by the user
                        if(mainUserChallengeList.value?.map { it.id }
                                ?.contains(document.data["id"].toString()) == true){
                            continue
                        }
                        _challengess.add(Challenge(document.data["name"].toString(), document.data["desc"].toString(),
                            document.data["id"].toString(), document.data["duration"].toString().toInt(),
                            document.data["calories"].toString().toInt(), document.data["rewards"].toString().toInt()))
                    }
                    mainChallengeList.postValue(_challengess)


                }.addOnFailureListener {
                    //Toast.makeText(requireContext(), "Failed to fetch challenges", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener { e ->
                // Handle the error
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
                    Toast.makeText(
                        context,
                        "Challenge Accepted",
                        Toast.LENGTH_SHORT
                    ).show()
                    mainChallengeList.postValue(mainChallengeList.value?.filter { it.id != challenge.id })
                    mainHomeSuggestedChallengeList.postValue(mainHomeSuggestedChallengeList.value?.filter { it.id != challenge.id })
                    mainHomeOngoingChallengeList.postValue(listOf(newChallenge))
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
                    mainChallengeList.postValue(mainChallengeList.value?.filter { it.id != challenge.id })
                    mainHomeSuggestedChallengeList.postValue(mainHomeSuggestedChallengeList.value?.filter { it.id != challenge.id })
                    mainHomeOngoingChallengeList.postValue(updatedList)
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





    //home screen
    var mainHomeOngoingChallengeList = MutableLiveData<List<UserChallenge>>()
    var mainHomeCompletedChallengeList = MutableLiveData<List<UserChallenge>>()
    var mainHomeSuggestedChallengeList = MutableLiveData<List<Challenge>>()






    var homeUserChallenges =  arrayListOf<UserChallenge>()
    var homeOnGoingChallenges = arrayListOf<UserChallenge>()
    var homeCompletedChallenges = arrayListOf<UserChallenge>()
    var homeSuggestedChallenges = arrayListOf<Challenge>()
    fun loadHomeChallenges() {

        db.collection("users").document(user.uid).get().addOnSuccessListener {

            val _userChallenges = it.get("userChallenges") as? List<HashMap<String, Any>>

            if (_userChallenges != null) {
                homeUserChallenges = _userChallenges.map { map ->
                    UserChallenge(
                        map["name"].toString(),
                        map["desc"].toString(),
                        map["id"].toString(),
                        map["duration"].toString().toInt(),
                        map["calories"].toString().toInt(),
                        map["rewards"].toString().toInt(),
                        map["startTime"].toString().toLong(),
                    )
                } as ArrayList<UserChallenge>
            }


            db.collection("challenges").get().addOnSuccessListener { it ->

                homeSuggestedChallenges = it.map { document ->
                    Challenge(
                        document.get("name").toString(),
                        document.get("desc").toString(),
                        document.get("id").toString(),
                        document.get("duration").toString().toInt(),
                        document.get("calories").toString().toInt(),
                        document.get("rewards").toString().toInt(),
                    )
                } as ArrayList<Challenge>

                for (document in it) {

                    val challengeID = document.get("id").toString()

                    //check if challenge is ongoing by checking if it is in userChallenges and that the current time is less than the timestamp of the challenge start time + duration
                    homeUserChallenges.forEach { userChallenge ->
                        Log.d(
                            "HEREALL---",
                            userChallenge.name + (userChallenge.id !in homeOnGoingChallenges.map { it.id }) + (System.currentTimeMillis() < (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24))
                        )
                        if (userChallenge.id !in homeOnGoingChallenges.map { it.id } && System.currentTimeMillis() < (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)) {
                            homeOnGoingChallenges.add(userChallenge)
                            Log.d("HERE---", userChallenge.name)
                        }
                    }

                    //check if challenge is completed by checking if it is in userChallenges and that the current time is greater than the timestamp of the challenge start time + duration
                    homeUserChallenges.forEach { userChallenge ->
                        Log.d(
                            "TAG",
                            ("ongoingchallenge  " + System.currentTimeMillis() > ((userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24).toString())).toString()
                        )

                        if (userChallenge.id == challengeID && System.currentTimeMillis() > (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)) {
                            homeCompletedChallenges.add(userChallenge)
                        }
                    }

                }

                val filteredSuggestedChallenges = arrayListOf<Challenge>()
                for (i in homeSuggestedChallenges) {
                    var isPresent = false
                    for (j in homeOnGoingChallenges) {
                        if (i.id == j.id) {
                            isPresent = true
                            break
                        }
                    }
                    for (j in homeCompletedChallenges) {
                        if (i.id == j.id) {
                            isPresent = true
                            break
                        }
                    }
                    if (!isPresent) {
                        filteredSuggestedChallenges.add(i)
                    }
                }
                homeSuggestedChallenges = filteredSuggestedChallenges
                mainHomeSuggestedChallengeList.postValue(homeSuggestedChallenges)
                mainHomeCompletedChallengeList.postValue(homeCompletedChallenges)
                mainHomeOngoingChallengeList.postValue(homeOnGoingChallenges)

                /*
                if(homeOnGoingChallenges.size>0){
                    binding.progressBarAddFood.visibility = View.GONE
                }else{

                    Toast.makeText(requireContext(), "No ongoing challenges", Toast.LENGTH_SHORT).show()
                    //binding.textView5.visibility = View.VISIBLE
                    //binding.onGoingChallengeList.visibility = View.GONE
                }

                if(homeSuggestedChallenges.size>0) {
                    binding.textView4.visibility = View.VISIBLE
                    binding.suggestedChallengeList.visibility = View.VISIBLE
                }else{
                    binding.textView4.visibility = View.GONE
                    binding.suggestedChallengeList.visibility = View.GONE
                }


                if( homeCompletedChallenges.size > 0){
                    binding.cardView.visibility = View.VISIBLE
                    val lastChallenge = homeCompletedChallenges.last()
                    var lastPassed = getLastPassedStatus(lastChallenge)
                    binding.textView7.text = "Total Rewards: ${getTotalRewards(completedChallenges)}"
                    if(lastPassed) {
                        binding.lastMilestoneChallenge.text = lastChallenge.name + ": ${lastChallenge.rewards} rewards earned"
                    }else{
                        binding.lastMilestoneChallenge.text = lastChallenge.name + ": 0 rewards earned (Failed)"
                    }
                }
                else{
                    binding.cardView.visibility = View.GONE
                }

            }
                .addOnFailureListener{
                    Toast.makeText(requireContext(), "Error getting challenges", Toast.LENGTH_SHORT).show()
                }*/


            }

        }





    }
}


