package com.example.styleshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.databinding.FragmentHomeBinding
import com.example.styleshare.viewmodel.PostViewModel
import com.example.styleshare.adapters.PostsAdapter
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.PostRepository

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
        observePostsFromFirestore()
    }

    private fun setupRecyclerView() {
        postAdapter = PostsAdapter(
            onEditClick = { /* אפשר להשאיר ריק או להראות הודעה */ },
            onDeleteClick = { /* כנ"ל */ }
        )
        binding.recyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observePostsFromFirestore() {
        postViewModel.getAllPostsFromFirestore().observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
            showLoading(false)

            if (posts.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.noPostsText.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.noPostsText.visibility = View.GONE
            }
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
