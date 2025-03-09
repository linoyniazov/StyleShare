package com.example.styleshare.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.styleshare.ui.home.AllPostsFragment
import com.example.styleshare.ui.home.ForYouFragment
import com.example.styleshare.ui.home.FollowingFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ForYouFragment()   // טאב של "For You"
            1 -> FollowingFragment() // טאב של "Following"
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
