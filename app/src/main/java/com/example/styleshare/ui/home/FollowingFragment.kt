package com.example.styleshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.databinding.FragmentFollowingBinding
import com.example.styleshare.viewmodel.HomeViewModel
import com.example.styleshare.adapters.PostAdapter
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.HomeRepository

class FollowingFragment : Fragment() {

    private var _binding: FragmentFollowingBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.HomeViewModelFactory(HomeRepository(
            AppDatabase.getDatabase(requireContext()).postDao(),
            AppDatabase.getDatabase(requireContext()).followDao()
        ))
    }
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFollowingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostAdapter()
        binding.recyclerViewPosts.adapter = postAdapter
        binding.recyclerViewPosts.layoutManager = LinearLayoutManager(requireContext())

        homeViewModel.followingPosts.observe(viewLifecycleOwner) { posts ->
            Log.d("FollowingFragment", "Number of posts received: ${posts.size}")
            if (posts.isNotEmpty()) {
                postAdapter.submitList(posts)
                binding.recyclerViewPosts.visibility = View.VISIBLE
            } else {
                binding.recyclerViewPosts.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}