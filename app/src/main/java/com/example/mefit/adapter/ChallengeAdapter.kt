package com.example.mefit.adapter

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.AllChallengesViewModel
import com.example.mefit.R
import com.example.mefit.model.UserChallenge

class ChallengeAdapter(private val challengeList: List<UserChallenge>, private val context: Context, private val challengesViewModel: AllChallengesViewModel) :
    RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    inner class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.challengeTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.challengeDescription)

        init {
            itemView.setOnClickListener {
                challengesViewModel.displayOngoingDialog(challengeList[adapterPosition], context)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.challenge_item, parent, false)
        return ChallengeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {


        val currentChallenge = challengeList[position]
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
        holder.titleTextView.text = currentChallenge.name
        holder.descriptionTextView.text = currentChallenge.desc
    }

    override fun getItemCount(): Int {
        return challengeList.size
    }
}

