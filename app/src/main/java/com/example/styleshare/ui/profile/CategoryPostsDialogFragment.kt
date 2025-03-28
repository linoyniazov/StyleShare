package com.example.styleshare.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.adapters.PostsAdapter
import com.example.styleshare.databinding.FragmentCategoryPostsDialogBinding
import com.example.styleshare.model.entities.Post
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.viewmodel.PostViewModel
import kotlinx.coroutines.launch

class CategoryPostsDialogFragment : DialogFragment() {
    private var _binding: FragmentCategoryPostsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter

    private val postViewModel: PostViewModel by viewModels {
        PostViewModel.PostViewModelFactory(
            PostRepository(AppDatabase.getDatabase(requireContext()).postDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryPostsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString(ARG_CATEGORY_NAME) ?: ""
        val posts = arguments?.getParcelableArrayList<Post>(ARG_POSTS)?.toList() ?: emptyList()

        setupToolbar(categoryName)
        setupRecyclerView(posts)
    }

    private fun setupToolbar(categoryName: String) {
        binding.toolbar.apply {
            title = categoryName
            setNavigationOnClickListener {
                dismiss()
            }
        }
    }

    private fun setupRecyclerView(posts: List<Post>) {
        postsAdapter = PostsAdapter(
            onEditClick = { post ->
                val bundle = Bundle().apply {
                    putString("postId", post.postId)
                }
                findNavController().navigate(R.id.action_profileFragment_to_uploadPostFragment, bundle)
                dismiss()
            },
            onDeleteClick = { post ->
                showDeleteConfirmationDialog(post)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postsAdapter
        }

        postsAdapter.submitList(posts)
    }

    private fun showDeleteConfirmationDialog(post: Post) {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseFirestore.getInstance()
                    .collection("posts")
                    .document(post.postId)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show()

                        // Delete from Room database
                        lifecycleScope.launch {
                            postViewModel.deletePost(post.postId)
                        }

                        dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()

        // Style the dialog buttons
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(requireContext().getColor(R.color.red))

        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(requireContext().getColor(R.color.gray))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CATEGORY_NAME = "categoryName"
        private const val ARG_POSTS = "posts"

        fun newInstance(categoryName: String, posts: List<Post>): CategoryPostsDialogFragment {
            return CategoryPostsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_NAME, categoryName)
                    putParcelableArrayList(ARG_POSTS, ArrayList(posts))
                }
            }
        }
    }
}