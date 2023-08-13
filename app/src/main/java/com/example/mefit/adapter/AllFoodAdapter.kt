package com.example.mefit.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mefit.FoodSharedViewModel
import com.example.mefit.R
import com.example.mefit.model.Food

class AllFoodAdapter(private val foodList: List<Food>, private val sharedViewModel: FoodSharedViewModel) : RecyclerView.Adapter<AllFoodAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.all_food_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val foodItem = foodList[position]
        holder.bind(foodItem)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val foodImageView: ImageView = itemView.findViewById(R.id.addButton)

        init {
            foodImageView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedFood = foodList[position]
                    sharedViewModel.addFoodToFirebase(selectedFood)
                }
            }
        }
        fun bind(foodItem: Food) {
            itemView.findViewById<TextView>(R.id.foodNameTextView).text = foodItem.name
            itemView.findViewById<TextView>(R.id.caloriesTextView).text = foodItem.calories.toString()
        }
    }
}
