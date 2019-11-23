package com.amier.fixednetchat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amier.fixednetchat.Groupie.ChatViewTarget
import com.amier.fixednetchat.Groupie.ChatViewUser
import com.amier.fixednetchat.Models.Message
import com.amier.fixednetchat.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_view.*

class ChatView : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_view)

        chat_view_recycle.adapter = adapter

        toUser = intent.getParcelableExtra(NewMessage.USER_KEY)

        supportActionBar?.title = toUser?.username

        listenForMessages()

        chat_view_btn.setOnClickListener {
            performSendMessage()
        }
    }

    private fun listenForMessages() {
        val uid = FirebaseAuth.getInstance().uid
        val targetId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$uid/$targetId")

        ref.addChildEventListener(object: ChildEventListener {

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message = p0.getValue(Message::class.java)

                if (message != null) {

                    if (message.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = MainActivity.user ?: return
                        adapter.add(ChatViewUser(message.text, currentUser))
                    } else {
                        adapter.add(ChatViewTarget(message.text, toUser!!))
                    }
                }

                chat_view_recycle.scrollToPosition(adapter.itemCount - 1)

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }

        })

    }

    private fun performSendMessage() {
        val text = chat_view_edittext.text.toString()

        val uid = FirebaseAuth.getInstance().uid
        val target = intent.getParcelableExtra<User>(NewMessage.USER_KEY)
        val targetId = target.uid

        if (uid == null) return

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$uid/$targetId").push()

        val targetRef = FirebaseDatabase.getInstance().getReference("/user-messages/$targetId/$uid").push()

        val message = Message(ref.key!!, text, uid, targetId, System.currentTimeMillis() / 1000)

        ref.setValue(message)
            .addOnSuccessListener {
                chat_view_edittext.text.clear()
                chat_view_recycle.scrollToPosition(adapter.itemCount - 1)
            }

        targetRef.setValue(message)

        val lastMessageUserRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$uid/$targetId")
        lastMessageUserRef.setValue(message)

        val lastMessageTargetRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$targetId/$uid")
        lastMessageTargetRef.setValue(message)
    }
}
