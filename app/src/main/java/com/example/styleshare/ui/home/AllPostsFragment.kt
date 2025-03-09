package com.example.styleshare.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.styleshare.adapters.PostAdapter
import com.example.styleshare.databinding.FragmentAllpostsBinding
import androidx.fragment.app.viewModels
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.repository.HomeRepository
import com.example.styleshare.viewmodel.HomeViewModel
import com.google.android.material.tabs.TabLayout
import com.example.styleshare.model.entities.Post
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap

class AllPostsFragment : Fragment() {
    private var _binding: FragmentAllpostsBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = HomeRepository(database.postDao(), database.followDao())
        HomeViewModel.HomeViewModelFactory(repository)
    }

    private lateinit var postAdapter: PostAdapter
    private val selectedTab = MutableLiveData(0) // 🔹 שמירת המיקום של הטאב שנבחר

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAllpostsBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postAdapter = PostAdapter()
        binding.recyclerViewPosts.adapter = postAdapter
        binding.recyclerViewPosts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

    }
    /**
     * ✅ פונקציה שמחזירה LiveData לפי הטאב שנבחר
     */
    private fun getPostsForTab(tabIndex: Int): LiveData<List<Post>> {
        return when (tabIndex) {
            0 -> homeViewModel.allPosts
            1 -> homeViewModel.forYouPosts
            2 -> homeViewModel.followingPosts
            else -> homeViewModel.allPosts
        }
    }

    /**
     * ✅ פונקציה לעדכון הרשימה כדי למנוע כפילות קוד
     */
    private fun updatePosts(posts: List<Post>, tabName: String) {
        postAdapter.submitList(posts)
        Log.d("TabLayout", "🔹 הצגת $tabName: ${posts.size} פוסטים")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
