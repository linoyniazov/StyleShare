package com.example.styleshare.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.styleshare.databinding.FragmentPostDetailBinding
import com.example.styleshare.viewmodel.PostViewModel

class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val args: PostDetailFragmentArgs by navArgs() // ✅ קבלת ה- postId מ-SafeArgs
    private val postViewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = args.postId // קבלת ה-ID של הפוסט שנבחר
        postViewModel.getPostById(postId).observe(viewLifecycleOwner) { post ->
            post?.let {
                binding.textViewCaption.text = it.caption
                binding.textViewCategory.text = it.category
                binding.textViewTimestamp.text = it.timestamp.toString() // ✅ ניתן לעצב את התאריך בפורמט מתאים

                // טעינת תמונה מה-URL של הפוסט
                Glide.with(this)
                    .load(it.imageUrl)
                    .into(binding.imageViewPost)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
