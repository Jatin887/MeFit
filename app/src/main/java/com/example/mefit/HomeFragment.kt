package com.example.mefit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.adapter.AllChallengeAdapter
import com.example.mefit.adapter.ChallengeAdapter
import com.example.mefit.databinding.FragmentHomeBinding
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

        challengesViewModel.isTrue.observe(viewLifecycleOwner){
            challengesViewModel.loadHomeChallenges()
        }


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

        }


        challengesViewModel.mainHomeSuggestedChallengeList.observe(viewLifecycleOwner){
            binding.suggestedChallengeList.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            binding.suggestedChallengeList.adapter = AllChallengeAdapter(it, requireContext(), challengesViewModel)
            if(it.size>0){
                binding.suggestedChallengeList.visibility = View.VISIBLE
                binding.textView4.visibility = View.VISIBLE
            }else{
                //binding.suggestedChallengeList.visibility = View.GONE
                binding.textView4.visibility = View.GONE
            }
        }

        challengesViewModel.mainHomeOngoingChallengeList.observe(viewLifecycleOwner) {
            binding.onGoingChallengeList.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            binding.onGoingChallengeList.adapter = ChallengeAdapter(it, requireContext(), challengesViewModel)
            //notify adapter when data changes
            binding.onGoingChallengeList.adapter?.notifyDataSetChanged()
            if (it.size > 0) {
                binding.onGoingChallengeList.visibility = View.VISIBLE
                binding.textView5.visibility = View.VISIBLE
            } else {
                binding.onGoingChallengeList.visibility = View.GONE
                binding.textView5.visibility = View.GONE
            }

        }


        challengesViewModel.mainHomeCompletedChallengeList.observe(viewLifecycleOwner){
            if( it.size > 0){
                binding.cardView.visibility = View.VISIBLE
                val lastChallenge = it.last()
                var lastPassed = getLastPassedStatus(lastChallenge)
                binding.textView7.text = "Total Rewards: ${getTotalRewards(it)}"
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

    private fun getTotalRewards(completedChallenges: List<UserChallenge>): String {
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