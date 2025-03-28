package com.example.styleshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.R
import com.example.styleshare.adapters.PostsAdapter
import com.example.styleshare.databinding.FragmentHomeBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.viewmodel.PostViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels {
        PostViewModel.PostViewModelFactory(
            PostRepository(AppDatabase.getDatabase(requireContext()).postDao())
        )
    }

    private lateinit var postAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        showLoading(true)

        lifecycleScope.launch {
            try {
                postViewModel.syncPostsFromFirestore()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Sync failed", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }

        // ✅ רענון על ידי המשתמש עם טיפול בשגיאות
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                try {
                    postViewModel.syncPostsFromFirestore()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Sync failed", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }

        // ✅ מאזין לשינויים מה-ViewModel
        postViewModel.getAllPostsFromFirestore().observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)

            if (posts.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.noPostsText.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.noPostsText.visibility = View.GONE
            }
        }
    }


    private fun setupRecyclerView() {
        postAdapter = PostsAdapter(
            onEditClick = { post ->
                val bundle = Bundle().apply {
                    putString("postId", post.postId)
                }
                findNavController().navigate(R.id.action_homeFragment_to_uploadPostFragment, bundle)
            },
            onDeleteClick = { post ->
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
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show()
                            }

                        // ✅ גם מחיקה מ-ROOM
                        lifecycleScope.launch {
                            postViewModel.deletePost(post.postId)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                dialog.show()

                dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                    ?.setTextColor(requireContext().getColor(R.color.red))

                dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                    ?.setTextColor(requireContext().getColor(R.color.gray))
            }
        )

        binding.recyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
