package com.amier.fixednetchat.Groupie

import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.new_message_item.view.*

class NewMessageItem(val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.root.new_message_item_textview.text = user.username

        Picasso.get().load(user.profileImageUrl)
            .resize(48,48)
            .into(viewHolder.root.new_message_item_image)
    }

    override fun getLayout(): Int {
        return R.layout.new_message_item
    }
}