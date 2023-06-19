package com.coventry.kennyapplication.ui.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.coventry.kennyapplication.LeaderboardEntry
import com.coventry.kennyapplication.databinding.FragmentLeaderboardBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val database = FirebaseDatabase.getInstance()
        val leaderboardRef = database.getReference("leaderboard")
        val listLeaderboard: RecyclerView = binding.listLeaderboard

        leaderboardRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val leaderboardEntries = mutableListOf<LeaderboardEntry>()

                for (entrySnapshot in snapshot.children) {
                    val username = entrySnapshot.child("username").getValue(String::class.java)
                    val score = entrySnapshot.child("score").getValue(Int::class.java)

                    if (username != null && score != null) {
                        val leaderboardEntry = LeaderboardEntry(username, score)
                        leaderboardEntries.add(leaderboardEntry)
                    }
                }

                // Step 5: Create an Adapter for the RecyclerView
                val leaderboardAdapter = LeaderboardAdapter(leaderboardEntries)

                // Set the adapter on the RecyclerView
                listLeaderboard.adapter = leaderboardAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}