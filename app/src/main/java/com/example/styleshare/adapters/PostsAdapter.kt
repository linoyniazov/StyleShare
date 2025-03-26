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
import com.example.styleshare.R
import android.view.MenuItem
import android.widget.PopupMenu

class PostsAdapter(
    private val onEditClick: (Post) -> Unit,
    private val onDeleteClick: (Post) -> Unit
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PostViewHolder(
        private val binding: ItemPostBinding,
        private val onEditClick: (Post) -> Unit,
        private val onDeleteClick: (Post) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(post: Post) {
            binding.apply {
                Glide.with(postImage)
                    .load(post.imageUrl)
                    .centerCrop()
                    .into(postImage)

                usernameText.text = post.username
                categoryChip.text = post.category
                captionText.text = post.caption
                itemsText.text = "Items: ${post.items.joinToString(", ")}"

                val date = Date(post.timestamp)
                timestampText.text = dateFormat.format(date)

                // כפתור ⋮ לעריכה / מחיקה
                binding.btnOptions.setOnClickListener {
                    val popup = PopupMenu(binding.root.context, binding.btnOptions)
                    popup.inflate(R.menu.post_options_menu)
                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.menu_edit -> {
                                onEditClick(post)
                                true
                            }
                            R.id.menu_delete -> {
                                onDeleteClick(post)
                                true
                            }
                            else -> false
                        }
                    }
                    popup.show()
                }
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
