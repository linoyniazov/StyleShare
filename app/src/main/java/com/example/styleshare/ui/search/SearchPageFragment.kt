package com.example.styleshare.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentSearchPageBinding
import com.example.styleshare.adapters.PostAdapter
import com.example.styleshare.viewmodel.PostViewModel

class SearchPageFragment : Fragment() {

    private var _binding: FragmentSearchPageBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // אתחול ה-RecyclerView עם תמיכה בלחיצה על פוסט
        setupRecyclerView()

        // חיפוש – כל שינוי בטקסט בשדה החיפוש יפעיל את הפונקציה
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchPosts(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter { post ->  // ✅ שימוש בפונקציה ללחיצה על פוסט
            val action = SearchPageFragmentDirections.actionSearchToPostDetail(post.postId)
            findNavController().navigate(action)
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = postAdapter
        }
    }

    private fun searchPosts(query: String) {
        postViewModel.searchPosts(query).observe(viewLifecycleOwner, Observer { posts ->
            postAdapter.submitList(posts)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
