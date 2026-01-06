package com.example.recyclerviewdemo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private var messages: List<Message> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val searchView: SearchView = findViewById(R.id.searchView)

        messages = listOf(
            Message(1, "Exam", "Exam tomorrow at 9:00", false),
            Message(2, "Library", "Book returned successfully", true),
            Message(3, "Meeting", "Project discussion at 14:00", false),
            Message(4, "System", "Profile updated", true)
        )

        adapter = MessageAdapter(messages) { clickedMessage ->
            Toast.makeText(this, clickedMessage.title, Toast.LENGTH_SHORT).show()

            val updatedList = messages.map {
                if (it.id == clickedMessage.id)
                    it.copy(isRead = true)
                else
                    it
            }

            messages = updatedList
            adapter.updateList(updatedList)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(16))

        // --- Search ---
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMessages(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMessages(newText)
                return true
            }
        })
    }

    private fun filterMessages(query: String?) {
        val filteredList = if (!query.isNullOrEmpty()) {
            messages.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.text.contains(query, ignoreCase = true)
            }
        } else {
            messages
        }
        adapter.updateList(filteredList)
    }
}
