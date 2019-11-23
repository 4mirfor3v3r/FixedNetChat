package com.amier.fixednetchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amier.fixednetchat.Groupie.NewMessageItem
import com.amier.fixednetchat.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*

class NewMessage : AppCompatActivity() {

    companion object {
        const val USER_KEY = "USER_KEY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {

            override fun onDataChange(p0: DataSnapshot) {
                val adapter = GroupAdapter<GroupieViewHolder>()

                p0.children.forEach {
                    Log.d("NewMessage", it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        if(user.uid != FirebaseAuth.getInstance().uid){
                            adapter.add(NewMessageItem(user))
                        }
                    }
                }

                adapter.setOnItemClickListener { item, view ->

                    val userItem = item as NewMessageItem

                    val intent = Intent(view.context, ChatView::class.java)
                    intent.putExtra(USER_KEY, userItem.user)
                    startActivity(intent)

                    finish()

                }

                newmessage_recycler.adapter = adapter
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
