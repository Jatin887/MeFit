package com.example.mefit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Half.toFloat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.adapter.ChallengeAdapter
import com.example.mefit.databinding.FragmentHomeBinding
import com.example.mefit.model.Challenge
import com.example.mefit.model.UserChallenge
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var userChallenges = arrayListOf<UserChallenge>()
    private var challengesViewModel = AllChallengesViewModel()
    private var consumedCalories = 0
    private var goal = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        challengesViewModel = ViewModelProvider(this)[AllChallengesViewModel::class.java]

        binding.progressBarAddFood.visibility = View.VISIBLE
        db.collection("users").document(user.uid).get().addOnSuccessListener {
            val name = it.get("name").toString()
            consumedCalories = it.get("consumedCalories").toString().toInt()
            val total = it.get("totalCalories")
            val percentageConsumed =  ((consumedCalories.toFloat() / total.toString().toFloat()) * 100).toInt()
            goal = it.get("goal").toString()
            setupPieChart(percentageConsumed.toFloat())
            binding.greetingName.text = "Welcome $name"
            binding.consumedPercentage.text = "$percentageConsumed%"
            binding.calorieGoal.text = "${total.toString()} cal"



            val _userChallenges = it.get("userChallenges")  as? List<HashMap<String, Any>>

            if(_userChallenges != null) {
                userChallenges = _userChallenges.map { map ->
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

        }

        var onGoingChallenges = arrayListOf<UserChallenge>()
        var completedChallenges = arrayListOf<UserChallenge>()
        var suggestedChallenges = arrayListOf<Challenge>()

        db.collection("challenges").get().addOnSuccessListener { it ->

            suggestedChallenges = it.map { document ->
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
                userChallenges.forEach { userChallenge ->
                    Log.d("HEREALL---", userChallenge.name  + (userChallenge.id !in onGoingChallenges.map { it.id }) + (System.currentTimeMillis() < (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)))
                    if ( userChallenge.id !in onGoingChallenges.map { it.id } && System.currentTimeMillis() < (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)) {
                        onGoingChallenges.add(userChallenge)
                        Log.d("HERE---", userChallenge.name)
                    }
                }

                //check if challenge is completed by checking if it is in userChallenges and that the current time is greater than the timestamp of the challenge start time + duration
                userChallenges.forEach { userChallenge ->
                    Log.d("TAG",
                        ("ongoingchallenge  " + System.currentTimeMillis() > ((userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24).toString())).toString()
                    )

                    if (userChallenge.id == challengeID && System.currentTimeMillis() > (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)) {
                        completedChallenges.add(userChallenge)
                    }
                }

            }

            val filteredSuggestedChallenges = arrayListOf<Challenge>()
            for(i in suggestedChallenges){
                var isPresent = false
                for(j in onGoingChallenges){
                    if(i.id == j.id){
                        isPresent = true
                        break
                    }
                }
                for(j in completedChallenges){
                    if(i.id == j.id){
                        isPresent = true
                        break
                    }
                }
                if(!isPresent){
                    filteredSuggestedChallenges.add(i)
                }
            }
            suggestedChallenges = filteredSuggestedChallenges
            binding.suggestedChallengeList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            binding.onGoingChallengeList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            binding.suggestedChallengeList.adapter = AllChallengeAdapter(suggestedChallenges, requireContext(), challengesViewModel)
            binding.onGoingChallengeList.adapter = ChallengeAdapter(onGoingChallenges, requireContext(), challengesViewModel)

           if(onGoingChallenges.size>0){
               binding.progressBarAddFood.visibility = View.GONE
           }else{

               Toast.makeText(requireContext(), "Add ongoing challenges", Toast.LENGTH_SHORT).show()
               //binding.textView5.visibility = View.VISIBLE
               //binding.onGoingChallengeList.visibility = View.GONE
           }

            if(suggestedChallenges.size>0) {
                binding.textView4.visibility = View.VISIBLE
                binding.suggestedChallengeList.visibility = View.VISIBLE
            }else{
                binding.textView4.visibility = View.GONE
                binding.suggestedChallengeList.visibility = View.GONE
            }


            if(completedChallenges.size > 0){
                binding.cardView.visibility = View.VISIBLE
                val lastChallenge = completedChallenges.last()
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
            }



        //logout button
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun getLastPassedStatus(lastChallenge: UserChallenge): Boolean {

        if(goal=="Muscle Building" || goal=="Weight Gain"){
            if(consumedCalories>=lastChallenge.calories){
                return true
            }
        }
        if(goal=="Weight Loss"){
            if(consumedCalories<=lastChallenge.calories){
                return true
            }
        }
        return false
    }

    private fun getTotalRewards(completedChallenges: ArrayList<UserChallenge>): String {
        var totalRewards = 0
        completedChallenges.forEach {

            if(goal=="Muscle Building" || goal=="Weight Gain"){
            if(consumedCalories>=it.calories){
                    totalRewards += it.rewards
                }
            }
            if(goal=="Weight Loss"){
                if(consumedCalories<=it.calories){
                    totalRewards += it.rewards
                }
            }

        }
        return totalRewards.toString()

    }

    private fun setupPieChart(consumedPercentage: Float) {
        val pieChart = binding.imageView5


        // Create a list of PieEntries with your data
        val pieEntries = listOf(PieEntry(consumedPercentage), PieEntry(100-consumedPercentage))

        val dataSet = PieDataSet(pieEntries, "Data Set")
        val colors: ArrayList<Int> = ArrayList()
        colors.add(Color.RED)
        colors.add(Color.GREEN)
        dataSet.colors = colors

        // Create a PieData object from the dataset
        val pieData = PieData(dataSet)

        pieChart.apply {
            data = pieData
            description.isEnabled = false // Disable description label
            legend.isEnabled = false // Disable legend
            setDrawEntryLabels(false) // Disable entry labels
            animateY(1000) // Optional animation
        }
    }
}