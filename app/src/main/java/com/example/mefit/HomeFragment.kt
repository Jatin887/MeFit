package com.example.mefit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mefit.adapter.ChallengeAdapter
import com.example.mefit.databinding.FragmentHomeBinding
import com.example.mefit.model.Challenge
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var onGoingChallenges = arrayListOf<String>()
    private var completedChallenges = arrayListOf<String>()

    private var suggestedChallengesModel = arrayListOf<Challenge>()
    private var onGoingChallengesModel = arrayListOf<Challenge>()
    private var completedChallengesModel = arrayListOf<Challenge>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        db.collection("users").document(user.uid).get().addOnSuccessListener {
            val name = it.get("name").toString()
            val consumed = it.get("consumedCalories").toString()
            val total = it.get("totalCalories").toString()
            val percentageConsumed = 0//consumed.toFloat() / total.toFloat() * 100
            onGoingChallenges = it.get("ongoingChallenged") as ArrayList<String>
            completedChallenges = it.get("completedChallenges") as ArrayList<String>
            if(onGoingChallenges.size > 0){
                binding.textView4.visibility = View.GONE
                binding.suggestedChallengeList.visibility = View.GONE
                binding.textView5.visibility = View.VISIBLE
                binding.onGoingChallengeList.visibility = View.VISIBLE
            }
            else{
                binding.textView4.visibility = View.VISIBLE
                binding.suggestedChallengeList.visibility = View.VISIBLE
                binding.textView5.visibility = View.GONE
                binding.onGoingChallengeList.visibility = View.GONE
            }
            if(completedChallenges.size > 0){
                binding.cardView.visibility = View.VISIBLE
                val lastChallenge = completedChallenges.last()
                binding.lastMilestoneChallenge.text = lastChallenge
            }
            else{
                binding.cardView.visibility = View.GONE
            }
            binding.greetingName.text = "Welcome $name"
            binding.consumedPercentage.text = "$percentageConsumed%"
            binding.calorieGoal.text = "$total"

        }

        //get challenges collection of firebase and fill ongoing and suggested challenges accordingly
        db.collection("challenges").get().addOnSuccessListener {

            for (document in it) {

                val challengeID = document.get("id").toString()
                val challengeName = document.get("name").toString()
                val challengeDescription = document.get("desc").toString()
                val challengeDuration = document.get("duration").toString().toInt()
                val challengeCalories = document.get("calories").toString().toInt()
                val challengeRewards = document.get("rewards").toString().toInt()
                Log.d("HomeFragment", "onViewCreated: ${!onGoingChallenges.contains(challengeID)} => ${!completedChallenges.contains(challengeID)}")
                if (!onGoingChallenges.contains(challengeID) && !completedChallenges.contains(challengeID) ){
                    suggestedChallengesModel.add(Challenge(challengeName, challengeDescription, challengeID, challengeDuration, challengeCalories, challengeRewards))
                    Log.d("here---", "onViewCreated: ${suggestedChallengesModel.size}")
                }
                else if (onGoingChallenges.contains(challengeID)){
                    onGoingChallengesModel.add(Challenge(challengeName, challengeDescription, challengeID, challengeDuration, challengeCalories, challengeRewards))
                }else{
                    completedChallengesModel.add(Challenge(challengeName, challengeDescription, challengeID, challengeDuration, challengeCalories, challengeRewards))
                }
            }

            binding.suggestedChallengeList.layoutManager = LinearLayoutManager(requireContext())
            binding.onGoingChallengeList.layoutManager = LinearLayoutManager(requireContext())
            binding.suggestedChallengeList.adapter = ChallengeAdapter(suggestedChallengesModel, requireContext())
            binding.onGoingChallengeList.adapter = ChallengeAdapter(onGoingChallengesModel, requireContext())
            Log.d("here---", "onViewCreated: ${suggestedChallengesModel.size}")
        }
            .addOnFailureListener{
                Log.d("HomeFragment", "onViewCreated: ${it.message}")
            }


        //logout button
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
}