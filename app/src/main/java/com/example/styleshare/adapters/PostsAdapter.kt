package com.example.styleshare.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.styleshare.databinding.ItemPostBinding
import com.example.styleshare.model.entities.Post
import java.text.SimpleDateFormat
import java.util.*

class PostsAdapter : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(post: Post) {
            binding.apply {
                // ✅ הצגת תמונת הפוסט
                Glide.with(postImage)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(postImage)

                // ✅ הצגת שם המשתמש
                usernameText.text = post.username

                // ✅ הצגת קטגוריה
                categoryChip.text = post.category

                // ✅ הצגת כיתוב
                captionText.text = post.caption

                // ✅ הצגת פריטים
                itemsText.text = "Items: ${post.items.joinToString(", ")}"

                // ✅ הצגת זמן
                val date = Date(post.timestamp)
                timestampText.text = dateFormat.format(date)
            }
        }
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}
