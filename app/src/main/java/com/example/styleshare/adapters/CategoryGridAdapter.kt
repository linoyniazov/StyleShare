package com.example.styleshare.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.styleshare.databinding.ItemCategoryGridBinding
import com.example.styleshare.model.CategoryWithPosts
import com.example.styleshare.ui.profile.CategoryPostsDialogFragment

class CategoryGridAdapter(private val fragmentManager: FragmentManager) : RecyclerView.Adapter<CategoryGridAdapter.ViewHolder>() {
    private var categories = listOf<CategoryWithPosts>()

    fun submitList(newCategories: List<CategoryWithPosts>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, fragmentManager)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount() = categories.size

    class ViewHolder(
        private val binding: ItemCategoryGridBinding,
        private val fragmentManager: FragmentManager
    ) : RecyclerView.ViewHolder(binding.root) {
        private val categoryImagesAdapter = CategoryImagesAdapter()

        init {
            binding.categoryImagesGrid.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = categoryImagesAdapter
            }

            // Set click listener for the entire category card
            binding.root.setOnClickListener {
                val category = binding.categoryName.tag as? CategoryWithPosts
                category?.let {
                    CategoryPostsDialogFragment.newInstance(it.name, it.posts)
                        .show(fragmentManager, "category_posts")
                }
            }
        }

        fun bind(category: CategoryWithPosts) {
            binding.apply {
                // Store category data for click handling
                categoryName.tag = category

                categoryName.text = category.name
                postCount.text = "${category.posts.size} posts"
                categoryImagesAdapter.submitList(category.posts.map { it.imageUrl })
            }
        }
    }
}