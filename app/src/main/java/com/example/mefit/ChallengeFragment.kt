package com.example.mefit

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContentProviderCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.adapter.ChallengeAdapter
import com.example.mefit.databinding.FragmentChallengeBinding
import com.example.mefit.model.Challenge
import com.example.mefit.model.UserChallenge
import com.google.android.play.integrity.internal.c
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ChallengeFragment : Fragment() {

    private lateinit var binding: FragmentChallengeBinding
    private var db = Firebase.firestore
    var user = FirebaseAuth.getInstance().currentUser!!
    private var challengesViewModel = AllChallengesViewModel()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        challengesViewModel = ViewModelProvider(this)[AllChallengesViewModel::class.java]
        challengesViewModel.loadAllChallenges()
        val dialogView = LayoutInflater.from(context).inflate(R.layout.new_challenge_dialog, null)

        binding.fab.setOnClickListener {
            //show a dialog to add challange
            val alertDialog = AlertDialog.Builder(context)
                .setTitle("Add a new challenge")
                .setView(dialogView)
                .setPositiveButton("Add New Challenge") { dialogInterface: DialogInterface, _: Int ->
                    //validate fields and push to firebase

                    val editTextId = dialogView.findViewById<EditText>(R.id.id)
                    val editTextName = dialogView.findViewById<EditText>(R.id.name)
                    val editTextDesc = dialogView.findViewById<EditText>(R.id.desc)
                    val editTextCalories = dialogView.findViewById<EditText>(R.id.editCalories)
                    val editTextDuration = dialogView.findViewById<EditText>(R.id.editDuration)
                    val editTextRewards = dialogView.findViewById<EditText>(R.id.rewards)

                    val id = editTextId.text.toString().trim()
                    val name = editTextName.text.toString().trim()
                    val desc = editTextDesc.text.toString().trim()
                    val caloriesText = editTextCalories.text.toString().trim()
                    val durationText = editTextDuration.text.toString().trim()
                    val rewards = editTextRewards.text.toString().trim()

                    if (name.isEmpty() || id.isEmpty() || caloriesText.isEmpty() || durationText.isEmpty() || rewards.isEmpty()|| desc.isEmpty()) {
                        Toast.makeText(context, "Please fill all the fields", Toast.LENGTH_SHORT).show()
                    } else {
                        val calories = caloriesText.toInt()
                        val duration = durationText.toInt()
                        val rewards = rewards.toInt()

                        // Push the data to Firebase
                        val newChallenge = UserChallenge(
                            name,
                            desc,
                            id,
                            duration,
                            calories,
                            rewards,
                            System.currentTimeMillis()
                        )

                        // Push the data to Firebase

                        val document = db.collection("users").document(user.uid)
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
                                } else if(id !in userChallenges.map { it.id }) {
                                    val updatedList = userChallenges.toMutableList()
                                    updatedList.add(newChallenge)
                                    document.update("userChallenges", updatedList).addOnCompleteListener {
                                        Toast.makeText(
                                            context,
                                            "New Challenge Added and Accepted!\nYou can view it in the home page only since this challenge is specific to you",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }else{
                                    Toast.makeText(
                                        context,
                                        "You have already accepted challenge with same ID",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to add challenge", Toast.LENGTH_SHORT).show()
                            }
                    }







                    dialogInterface.dismiss()
                }
                .setNegativeButton("Maybe Later") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.dismiss()
                }
                .create()

            alertDialog.show()

        }

        challengesViewModel.mainChallengeList.observe(viewLifecycleOwner) {
            Log.d("ChallengeFragment----", "onViewCreated: $it")
            binding.challengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.challengeRecyclerView.adapter = AllChallengeAdapter(it, requireContext(), challengesViewModel)
        }


    }


}