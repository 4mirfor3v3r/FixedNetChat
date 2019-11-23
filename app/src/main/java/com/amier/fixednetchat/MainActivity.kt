package com.amier.fixednetchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.amier.fixednetchat.Groupie.LastMessageItem
import com.amier.fixednetchat.Models.Message
import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.firstopen.Register
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        var user: User? = null
    }
    private val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainRecycle.adapter = adapter

        adapter.setOnItemClickListener { item, _ ->
            val intent = Intent(this, ChatView::class.java)

            val row = item as LastMessageItem
            intent.putExtra(NewMessage.USER_KEY, row.targetUser)
            startActivity(intent)
        }
        val addBtn = findViewById<FloatingActionButton>(R.id.fab)

        addBtn.setOnClickListener {
            val intent = Intent(this, NewMessage::class.java)
            startActivity(intent)
        }
        streamMessage()

        fetchUser()

        verifyLoginStatus()
    }

    val lastMessage = HashMap<String, Message>()

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        lastMessage.values.forEach {
            adapter.add(LastMessageItem(it))
        }
    }

    private fun streamMessage() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$uid")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                lastMessage[p0.key!!] = message
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java) ?: return
                lastMessage[p0.key!!] = message
                refreshRecyclerViewMessages()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun fetchUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                user = p0.getValue(User::class.java)
//                Log.d("LatestMessages", "Current user ${user?.profileImageUrl}")
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun verifyLoginStatus() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, Register::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Register::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
