package com.example.myfriend

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myfriend.databinding.ItemFriendBinding

class FriendAdapter(private val onClick: (Friend) -> Unit) :
    ListAdapter<Friend, FriendAdapter.FriendViewHolder>(FriendDiffCallback()) {

    class FriendViewHolder(
        private val binding: ItemFriendBinding,
        private val onClick: (Friend) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            binding.tvName.text = friend.name
            binding.tvSchool.text = friend.school
            binding.ivPhoto.load(friend.photoUri ?: R.drawable.placeholder) {
                placeholder(R.drawable.placeholder)
            }
            binding.root.setOnClickListener { onClick(friend) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class FriendDiffCallback : DiffUtil.ItemCallback<Friend>() {
    override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean = oldItem == newItem
}