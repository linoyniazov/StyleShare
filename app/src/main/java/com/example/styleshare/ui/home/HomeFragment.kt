package com.example.styleshare.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.databinding.FragmentHomeBinding
import com.example.styleshare.viewmodel.HomeViewModel
import com.example.styleshare.adapters.PostAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.styleshare.R
import com.example.styleshare.repository.HomeRepository
import com.example.styleshare.model.AppDatabase


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModel.HomeViewModelFactory(
            HomeRepository(AppDatabase.getDatabase(requireContext()).postDao())
        )
    }

    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // הסרת כפתור חזרה
        binding.toolbar.navigationIcon = null
        binding.toolbar.title = "StyleShare"

        // הגדרת האדפטר וה-RecyclerView
        postAdapter = PostAdapter { post ->
            // פעולה בלחיצה על פוסט (אם תרצי לפתוח מסך פרטים או עריכה)
        }

        binding.recyclerView.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // צפייה בנתוני הפוסטים מה-ViewModel
        homeViewModel.allPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
