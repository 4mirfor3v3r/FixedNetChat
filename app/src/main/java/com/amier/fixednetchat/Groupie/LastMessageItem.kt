package com.amier.fixednetchat.Groupie

import com.amier.fixednetchat.Models.Message
import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.last_message_item.view.*

class LastMessageItem(private val messages: Message): Item<GroupieViewHolder>() {
    var targetUser: User? = null

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.root.last_message_items_message.text = messages.text

        val chatPartnerId: String = if (messages.fromId == FirebaseAuth.getInstance().uid) {
            messages.toId
        } else {
            messages.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                targetUser = p0.getValue(User::class.java)
                viewHolder.root.last_message_items_username.text = targetUser?.username

                val targetImageView = viewHolder.root.last_message_items_image
                Picasso.get().load(targetUser?.profileImageUrl)
                    .resize(64,64)
                    .into(targetImageView)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.last_message_item
    }
}
