package com.coventry.kennyapplication.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coventry.kennyapplication.LeaderboardEntry
import com.coventry.kennyapplication.R

class LeaderboardAdapter(private val dataSet: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textLeaderboardUsername : TextView
        val textLeaderboardScore : TextView

        init {
            // Define click listener for the ViewHolder's View
            textLeaderboardUsername = view.findViewById(R.id.text_leaderboard_username)
            textLeaderboardScore = view.findViewById(R.id.text_leaderboard_score)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.leaderboard_item, viewGroup, false)

        return ViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textLeaderboardUsername.text = dataSet[position].username
        viewHolder.textLeaderboardScore.text = dataSet[position].score.toString()
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet.size

}
