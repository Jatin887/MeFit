package com.example.mefit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.adapter.ChallengeAdapter
import com.example.mefit.databinding.FragmentChallengeBinding
import com.example.mefit.model.Challenge
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

        db.collection("challenges").get().addOnSuccessListener { result ->
            val challenge = mutableListOf<Challenge>()
            for (document in result) {
                challenge.add(Challenge(document.data["name"].toString(), document.data["desc"].toString(),
                    document.data["id"].toString(), document.data["duration"].toString().toInt(),
                    document.data["calories"].toString().toInt(), document.data["rewards"].toString().toInt()))
            }

            Log.d("ChallengeFragment------", "Challenge: $challenge")
            binding.challengeRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.challengeRecyclerView.adapter = AllChallengeAdapter(challenge, requireContext(), challengesViewModel)

        }.addOnFailureListener {
            Log.d("ChallengeFragment", "Error getting documents: ", it)
        }



    }


}