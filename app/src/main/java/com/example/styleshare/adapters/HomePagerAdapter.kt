package com.example.styleshare.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.styleshare.ui.home.AllPostsFragment
import com.example.styleshare.ui.home.ForYouFragment
import com.example.styleshare.ui.home.FollowingFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllPostsFragment()
            1 -> ForYouFragment()
            2 -> FollowingFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}