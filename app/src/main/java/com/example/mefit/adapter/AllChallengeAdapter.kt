package com.example.mefit.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.AllChallengesViewModel
import com.example.mefit.R
import com.example.mefit.model.Challenge

class AllChallengeAdapter (private val allChallengeList: List<Challenge>,
                           private val context: Context,
                           private val challengesViewModel: AllChallengesViewModel) :
    RecyclerView.Adapter<AllChallengeAdapter.AllChallengeViewHolder>() {


    inner class AllChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.allChallengeTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.allChallengeDescription)

        //setOnclicklistener to
        init {
            itemView.setOnClickListener {
                Log.d("AllChallengesViewModel", "onLetsDoItClicked: ${allChallengeList[adapterPosition]}")
                challengesViewModel.displayDialog(allChallengeList[adapterPosition],context)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllChallengeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_challenge_item, parent, false)
        return AllChallengeViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return allChallengeList.size
    }

    override fun onBindViewHolder(holder: AllChallengeViewHolder, position: Int) {
        val currentChallenge = allChallengeList[position]
        holder.titleTextView.text = currentChallenge.name + " (" + currentChallenge.rewards + " rewards)" + " (" + currentChallenge.duration + " days)"
        holder.descriptionTextView.text = currentChallenge.desc
        Log.d("AllChallengeAdapter", "Challenge: $currentChallenge")
    }
}
