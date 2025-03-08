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
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private var customCategoryChip: Chip? = null
    private var isEditingCategory = false

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
                    savePostToFirestore(uploadedImageUrl, caption, category, items, isDraft)
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (imageBitmap != null) {
            CloudinaryModel.uploadImageFromBitmap(imageBitmap!!, requireContext(),
                onSuccess = { uploadedImageUrl ->
                    savePostToFirestore(uploadedImageUrl, caption, category, items, isDraft)
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getSelectedCategory(): String {
        val selectedChipId = binding.categoryChipGroup.checkedChipId
        if (selectedChipId != View.NO_ID) {
            val selectedChip = binding.categoryChipGroup.findViewById<Chip>(selectedChipId)
            return selectedChip.text.toString()
        }
        return ""
    }

    private fun getItemsFromViews(): List<String> {
        val items = mutableListOf<String>()
        for (view in itemViews) {
            val itemName = view.findViewById<TextInputEditText>(R.id.itemNameInput).text.toString()
            val shopBrand = view.findViewById<TextInputEditText>(R.id.shopBrandInput).text.toString()
            val price = view.findViewById<TextInputEditText>(R.id.priceInput).text.toString()

            if (itemName.isNotEmpty() && shopBrand.isNotEmpty() && price.isNotEmpty()) {
                items.add("$itemName - $shopBrand - $price")
            }
        }
        return items
    }

    private fun savePostToFirestore(
        imageUrl: String,
        caption: String,
        category: String,
        items: List<String>,
        isDraft: Boolean
    ) {
        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown_user"

        val postData = hashMapOf(
            "postId" to postId,
            "userId" to userId,
            "imageUrl" to imageUrl,
            "caption" to caption,
            "category" to category,
            "timestamp" to System.currentTimeMillis(),
            "likes" to 0,
            "commentsCount" to 0,
            "items" to items,
            "isDraft" to isDraft
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("posts").document(postId)
            .set(postData)
            .addOnSuccessListener {
                savePostToRoom(postId, userId, imageUrl, caption, category, items, isDraft)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save post to Firestore", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePostToRoom(
        postId: String,
        userId: String,
        imageUrl: String,
        caption: String,
        category: String,
        items: List<String>,
        isDraft: Boolean
    ) {
        val post = Post(
            postId = postId,
            userId = userId,
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
        Toast.makeText(requireContext(), "Post uploaded successfully!", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
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

        // עדכון המספור של הפריט
        itemView.findViewById<TextView>(R.id.itemNumberLabel).text = "Item $itemCount"

        // הוספת כפתור להסרת הפריט
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}