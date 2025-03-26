package com.example.styleshare.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.adapters.PostsAdapter
import com.example.styleshare.databinding.FragmentPostsListBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.PostRepository

import com.example.styleshare.viewmodel.PostViewModel

class PostsListFragment : Fragment() {


    private var _binding: FragmentPostsListBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels {
        PostViewModel.PostViewModelFactory(PostRepository(AppDatabase.getDatabase(requireContext()).postDao()))
    }

    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observePosts()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(
            onEditClick = { /* אפשר להשאיר ריק או להראות הודעה */ },
            onDeleteClick = { /* כנ"ל */ }
        )
        binding.postsRecyclerView.apply {
            adapter = postsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun observePosts() {
        binding.loadingProgressBar.visibility = View.VISIBLE
        postViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            binding.loadingProgressBar.visibility = View.GONE
            postsAdapter.submitList(posts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
