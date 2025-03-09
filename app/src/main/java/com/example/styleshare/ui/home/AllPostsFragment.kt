package com.example.styleshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.styleshare.adapters.PostAdapter
import com.example.styleshare.databinding.FragmentAllpostsBinding
import com.example.styleshare.viewmodel.PostViewModel
import androidx.fragment.app.viewModels
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.viewmodel.HomeViewModel

class AllPostsFragment : Fragment() {
    private var _binding: FragmentAllpostsBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllpostsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostAdapter()
        binding.recyclerViewPosts.adapter = postAdapter
        binding.recyclerViewPosts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        homeViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            if (posts.isNotEmpty()) {
                postAdapter.submitList(posts)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
