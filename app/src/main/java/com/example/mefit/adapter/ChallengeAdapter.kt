package com.example.mefit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.R
import com.example.mefit.model.Challenge

class ChallengeAdapter(private val challengeList: List<Challenge>, private val context: android.content.Context) :
    RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.challengeTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.challengeDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.challenge_item, parent, false)
        return ChallengeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        val currentChallenge = challengeList[position]
        holder.titleTextView.text = currentChallenge.name
        holder.descriptionTextView.text = currentChallenge.desc
    }

    override fun getItemCount(): Int {
        return challengeList.size
    }
}

