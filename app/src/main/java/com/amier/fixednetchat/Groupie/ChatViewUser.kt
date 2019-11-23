package com.amier.fixednetchat.Groupie

import android.annotation.SuppressLint
import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_view_user.view.*

class ChatViewUser(val text: String, private val user: User): Item<GroupieViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.root.chat_view_user_msg.text = text
        viewHolder.root.chat_view_user_name.text = "You"

    }

    override fun getLayout(): Int {
        return R.layout.chat_view_user
    }
}