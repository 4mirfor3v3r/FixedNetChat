package com.amier.fixednetchat.Groupie

import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.R
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_view_target.view.*

class ChatViewTarget(val text: String, private val user: User): Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.root.chat_view_target_msg.text = text
        viewHolder.root.chat_view_target_name.text = user.username

    }

    override fun getLayout(): Int {
        return R.layout.chat_view_target
    }
}