package com.example.handsight

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.handsight.databinding.ActivityHistoryBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val adapter by lazy { HistoryAdapter() }
    private val db: FirebaseFirestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uid = intent.extras!!.getString("uid").toString()
        binding.topAppBar.setNavigationOnClickListener { finish() }

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        db.collection("history")
            .whereEqualTo("uid", uid)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val data = ArrayList<History>()
                for (document in result) {
                    data.add(
                        History(
                            name = document.data["name"].toString(),
                            createdAt = getTimestampFormatString(document.getTimestamp("created_at")!!.seconds)
                        )
                    )
                }
                adapter.differ.submitList(data)
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }
}