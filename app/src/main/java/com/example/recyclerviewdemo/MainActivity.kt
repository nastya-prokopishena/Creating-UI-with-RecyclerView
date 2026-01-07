package com.example.recyclerviewdemo

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: MessageAdapter
    private var messages: List<Message> = emptyList()
    private lateinit var recyclerView: RecyclerView
    private var deletedMessage: Message? = null
    private var deletedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        val searchView: SearchView = findViewById(R.id.searchView)
        val btnAdd: Button = findViewById(R.id.btnAdd)
        val btnDelete: Button = findViewById(R.id.btnDelete)
        val btnUndo: Button = findViewById(R.id.btnUndo)

        initializeMessages()

        adapter = MessageAdapter(messages) { clickedMessage ->
            Toast.makeText(this, "Clicked: ${clickedMessage.title}", Toast.LENGTH_SHORT).show()

            // Mark as read
            val updatedList = messages.map {
                if (it.id == clickedMessage.id) it.copy(isRead = true) else it
            }
            updateMessages(updatedList)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(SpaceItemDecoration(16))

        // Add Button - demonstrates notifyItemInserted
        btnAdd.setOnClickListener {
            val newId = (messages.maxOfOrNull { it.id } ?: 0) + 1
            val newMessage = Message(
                id = newId,
                title = "New Message $newId",
                text = "Added at ${System.currentTimeMillis()}",
                isRead = false
            )
            val newList = messages.toMutableList().apply { add(0, newMessage) }
            updateMessages(newList)

            // Scroll to top
            recyclerView.scrollToPosition(0)
        }

        // Delete Button - demonstrates notifyItemRemoved
        btnDelete.setOnClickListener {
            if (messages.isNotEmpty()) {
                val list = messages.toMutableList()
                deletedMessage = list.removeAt(0)
                deletedPosition = 0
                updateMessages(list)

                Snackbar.make(recyclerView, "Item deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        deletedMessage?.let {
                            val restoreList = messages.toMutableList().apply {
                                add(deletedPosition, it)
                            }
                            updateMessages(restoreList)
                        }
                    }
                    .show()
            }
        }

        // Undo Button - demonstrates notifyItemChanged
        btnUndo.setOnClickListener {
            val updatedList = messages.map {
                it.copy(isRead = false)
            }
            updateMessages(updatedList)
        }

        // Search
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

        setupSwipeToDelete()
        setupDragAndDrop()
    }

    private fun initializeMessages() {
        messages = listOf(
            Message(1, "Exam", "Exam tomorrow at 9:00", false),
            Message(2, "Library", "Book returned successfully", true),
            Message(3, "Meeting", "Project discussion at 14:00", false),
            Message(4, "System", "Profile updated", true),
            Message(5, "Gym", "Workout session at 18:00", false),
            Message(6, "Shopping", "Grocery list updated", true)
        )
    }

    private fun updateMessages(newList: List<Message>) {
        messages = newList
        adapter.updateList(newList)
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

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                deletedMessage = messages[position]
                deletedPosition = position

                val newList = messages.toMutableList().apply { removeAt(position) }
                updateMessages(newList)

                Snackbar.make(recyclerView, "Message deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        deletedMessage?.let {
                            val restoreList = messages.toMutableList().apply {
                                add(deletedPosition, it)
                            }
                            updateMessages(restoreList)
                        }
                    }
                    .show()
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun setupDragAndDrop() {
        val dragCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition

                val newList = messages.toMutableList()
                val movedItem = newList.removeAt(from)
                newList.add(to, movedItem)
                updateMessages(newList)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        }
        ItemTouchHelper(dragCallback).attachToRecyclerView(recyclerView)
    }
}