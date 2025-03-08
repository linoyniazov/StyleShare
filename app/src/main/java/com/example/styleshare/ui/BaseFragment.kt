package com.example.styleshare.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.styleshare.MainActivity
import com.example.styleshare.R

open class BaseFragment : Fragment() {
    open val showToolbar: Boolean = true
    open val showBottomNav: Boolean = true
    open val showBackButton: Boolean = false // אפשרות להציג כפתור חזרה

    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            (activity as MainActivity).updateNavigationVisibility(showToolbar, showBottomNav)

            // הפעלת כפתור חזרה אם הוא נדרש
            val backButton = activity?.findViewById<ImageButton>(R.id.backButton)
            backButton?.visibility = if (showBackButton) View.VISIBLE else View.GONE
            backButton?.setOnClickListener {
                findNavController().navigateUp() // חזרה למסך הקודם
            }
        }
    }
}
