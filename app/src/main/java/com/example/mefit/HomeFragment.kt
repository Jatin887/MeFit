package com.example.mefit

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
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
import com.example.mefit.databinding.FragmentHomeBinding
import com.example.mefit.model.Challenge
import com.example.mefit.model.UserChallenge
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private var db = Firebase.firestore
    private var user = FirebaseAuth.getInstance().currentUser!!
    private var userChallenges = arrayListOf<UserChallenge>()
    private var challengesViewModel = AllChallengesViewModel()

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

        db.collection("users").document(user.uid).get().addOnSuccessListener {
            val name = it.get("name").toString()
            val consumed = it.get("consumedCalories").toString()
            val total = it.get("totalCalories").toString()
            val percentageConsumed =  ((consumed.toFloat() / total.toFloat()) * 100).toInt()
            setupPieChart(percentageConsumed.toFloat())
            binding.greetingName.text = "Welcome $name"
            binding.consumedPercentage.text = "$percentageConsumed%"
            binding.calorieGoal.text = "$total"



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

        db.collection("challenges").get().addOnSuccessListener {

            for (document in it) {

                val challengeID = document.get("id").toString()

                //check if challenge is ongoing by checking if it is in userChallenges and that the current time is less than the timestamp of the challenge start time + duration
                onGoingChallenges = userChallenges.filter { userChallenge ->
                    userChallenge.id == challengeID && System.currentTimeMillis() < (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)
                }.map { userChallenge ->
                    userChallenge
                } as ArrayList<UserChallenge>

                //check if challenge is completed by checking if it is in userChallenges and that the current time is greater than the timestamp of the challenge start time + duration
                completedChallenges = userChallenges.filter { userChallenge ->
                    userChallenge.id == challengeID && System.currentTimeMillis() > (userChallenge.startTime + userChallenge.duration * 1000 * 60 * 60 * 24)
                }.map { userChallenge ->
                    userChallenge
                } as ArrayList<UserChallenge>

                //make suggested challenges list by checking if challenge id is not in userChallenges
                suggestedChallenges = it.map { document ->
                    Challenge(
                        document.get("name").toString(),
                        document.get("desc").toString(),
                        document.get("id").toString(),
                        document.get("duration").toString().toInt(),
                        document.get("calories").toString().toInt(),
                        document.get("rewards").toString().toInt(),
                    )
                }.filter { challenge ->
                    !userChallenges.any { userChallenge ->
                        userChallenge.id == challenge.id
                    }
                } as ArrayList<Challenge>
            }

            binding.suggestedChallengeList.layoutManager = LinearLayoutManager(requireContext())
            binding.onGoingChallengeList.layoutManager = LinearLayoutManager(requireContext())
            binding.suggestedChallengeList.adapter = AllChallengeAdapter(suggestedChallenges, requireContext(), challengesViewModel)
            binding.onGoingChallengeList.adapter = ChallengeAdapter(onGoingChallenges, requireContext())

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
                binding.lastMilestoneChallenge.text = lastChallenge.name
            }
            else{
                binding.cardView.visibility = View.GONE
            }

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