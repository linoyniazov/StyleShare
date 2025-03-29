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

class CategoryPostsDialogFragment : DialogFragment() {
    private var _binding: FragmentCategoryPostsDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter

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

        val categoryName = arguments?.getString("categoryName") ?: ""
        val posts = arguments?.getParcelableArrayList<Post>("posts") ?: arrayListOf()

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
                deletePost(post)
            }
            .setNegativeButton("Cancel", null)
            .show()

        // הוספת הצבעים לכפתורים
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(requireContext().getColor(R.color.red))

        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(requireContext().getColor(R.color.gray))
    }


    private fun deletePost(post: Post) {
        FirebaseFirestore.getInstance()
            .collection("posts")
            .document(post.postId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(categoryName: String, posts: List<Post>): CategoryPostsDialogFragment {
            return CategoryPostsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("categoryName", categoryName)
                    putParcelableArrayList("posts", ArrayList(posts))
                }
            }
        }
    }
}