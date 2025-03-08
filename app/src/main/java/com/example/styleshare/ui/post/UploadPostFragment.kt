package com.example.styleshare.ui.post

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentUploadPostBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.CloudinaryModel
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.ui.BaseFragment
import com.example.styleshare.viewmodel.PostViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.*

class UploadPostFragment : BaseFragment() {
    override val showToolbar: Boolean = true
    override val showBottomNav: Boolean = false
    override val showBackButton: Boolean = true

    private var _binding: FragmentUploadPostBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels {
        PostViewModel.PostViewModelFactory(PostRepository(AppDatabase.getDatabase(requireContext()).postDao()))
    }

    private var imageUri: Uri? = null
    private var imageBitmap: Bitmap? = null
    private var itemCount = 0
    private val itemViews = mutableListOf<View>()

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                imageBitmap = null
                binding.postImage.setImageURI(it)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    imageBitmap = bitmap
                    imageUri = null
                    binding.postImage.setImageBitmap(bitmap)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadPostBinding.inflate(inflater, container, false)
        CloudinaryModel.init(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChipGroupListeners()

        binding.postImage.setOnClickListener {
            showImagePickerOptions()
        }

        binding.addMoreItemButton.setOnClickListener {
            addNewItemFields()
        }

        binding.postButton.setOnClickListener {
            uploadPost(isDraft = false)
        }

        binding.saveDraftButton.setOnClickListener {
            uploadPost(isDraft = true)
        }
    }

    private fun setupChipGroupListeners() {
        binding.chipOther.setOnCheckedChangeListener { _, isChecked ->
            binding.otherCategoryInput.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Choose from Gallery", "Take a Picture")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Image")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageLauncher.launch("image/*")
                    1 -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePictureLauncher.launch(intent)
                    }
                }
            }
            .show()
    }

    private fun addNewItemFields() {
        itemCount++
        val itemView = layoutInflater.inflate(R.layout.item_post_fields, binding.dynamicItemsContainer, false)

        // Update the item number
        itemView.findViewById<TextView>(R.id.itemNumberLabel).text = "Item $itemCount"

        // Set up remove button
        itemView.findViewById<MaterialButton>(R.id.removeItemButton).setOnClickListener {
            binding.dynamicItemsContainer.removeView(itemView)
            itemViews.remove(itemView)
            updateItemNumbers()
        }

        binding.dynamicItemsContainer.addView(itemView)
        itemViews.add(itemView)
    }

    private fun updateItemNumbers() {
        itemViews.forEachIndexed { index, view ->
            view.findViewById<TextView>(R.id.itemNumberLabel).text = "Item ${index + 1}"
        }
        itemCount = itemViews.size
    }

    private fun getItemsFromViews(): List<String> {
        return itemViews.mapNotNull { view ->
            val itemName = view.findViewById<TextInputEditText>(R.id.itemNameInput).text.toString().trim()
            val shopBrand = view.findViewById<TextInputEditText>(R.id.shopBrandInput).text.toString().trim()
            val price = view.findViewById<TextInputEditText>(R.id.priceInput).text.toString().trim()

            if (itemName.isNotEmpty() && shopBrand.isNotEmpty() && price.isNotEmpty()) {
                "$itemName - $shopBrand ($$price)"
            } else null
        }
    }

    private fun uploadPost(isDraft: Boolean) {
        val caption = binding.captionEditText.text.toString()
        val category = getSelectedCategory()
        val items = getItemsFromViews()

        if ((imageUri == null && imageBitmap == null) || caption.isEmpty() || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please select an image and enter details", Toast.LENGTH_SHORT).show()
            return
        }

        if (items.isEmpty()) {
            Toast.makeText(requireContext(), "Please add at least one item", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            lifecycleScope.launch {
                val uploadedImageUrl = CloudinaryModel.uploadImageFromUri(requireContext(), imageUri!!)
                if (uploadedImageUrl != null) {
                    Log.d("CloudinaryUpload", "Uploaded Image URL: $uploadedImageUrl")
                    savePostToDatabase(uploadedImageUrl, caption, category, items, isDraft)
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (imageBitmap != null) {
            CloudinaryModel.uploadImageFromBitmap(imageBitmap!!, requireContext(),
                onSuccess = { uploadedImageUrl ->
                    savePostToDatabase(uploadedImageUrl, caption, category, items, isDraft)
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getSelectedCategory(): String {
        return when {
            binding.chipCasual.isChecked -> "Casual"
            binding.chipElegant.isChecked -> "Elegant"
            binding.chipParty.isChecked -> "Party"
            binding.chipFormal.isChecked -> "Formal"
            binding.chipEvening.isChecked -> "Evening"
            binding.chipOther.isChecked -> {
                val customCategory = binding.otherCategoryEditText.text.toString().trim()
                if (customCategory.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a custom category", Toast.LENGTH_SHORT).show()
                    ""
                } else {
                    customCategory
                }
            }
            else -> ""
        }
    }

    private fun savePostToDatabase(
        imageUrl: String,
        caption: String,
        category: String,
        items: List<String>,
        isDraft: Boolean
    ) {
        val post = Post(
            postId = UUID.randomUUID().toString(),
            userId = "user123",
            imageUrl = imageUrl,
            caption = caption,
            category = category,
            timestamp = System.currentTimeMillis(),
            likedBy = emptyList(),
            commentsCount = 0,
            items = items,
            isDraft = isDraft
        )

        postViewModel.insertPost(post)

        val message = if (isDraft) "Draft saved successfully!" else "Post uploaded successfully!"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
