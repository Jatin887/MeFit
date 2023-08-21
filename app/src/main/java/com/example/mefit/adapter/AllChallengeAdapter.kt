package com.example.mefit.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
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


        init {
            itemView.setOnClickListener {
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

        val name = currentChallenge.name
        val rewards = currentChallenge.rewards
        val duration = currentChallenge.duration

        val boldName = SpannableString(name)
        boldName.setSpan(StyleSpan(Typeface.BOLD), 0, name.length, 0)

        val formattedText = SpannableStringBuilder()
            .append(boldName).append("\n")
            .append("$rewards rewards\n")
            .append("$duration days")

        holder.titleTextView.text = formattedText
        holder.descriptionTextView.text = currentChallenge.desc
    }
}
