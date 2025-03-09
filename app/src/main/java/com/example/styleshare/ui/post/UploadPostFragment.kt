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

        setupChipGroupListeners()
        setupCustomCategoryHandlers()

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
            if (isChecked && !isEditingCategory) {
                showCustomCategoryInput()
            }
        }

        // Add listeners for all other chips
        val chips = listOf(
            binding.chipCasual,
            binding.chipElegant,
            binding.chipParty,
            binding.chipFormal,
            binding.chipEvening
        )

        chips.forEach { chip ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    hideCustomCategoryInput()
                    binding.chipOther.isChecked = false
                }
            }
        }
    }

    private fun setupCustomCategoryHandlers() {
        binding.confirmCategoryButton.setOnClickListener {
            val categoryName = binding.customCategoryEditText.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                if (customCategoryChip != null) {
                    // Update existing chip
                    customCategoryChip?.text = categoryName
                } else {
                    // Create new chip
                    createCustomCategoryChip(categoryName)
                }
                hideCustomCategoryInput()
            } else {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cancelCategoryButton.setOnClickListener {
            hideCustomCategoryInput()
            if (!isEditingCategory) {
                binding.chipOther.isChecked = false
            }
            isEditingCategory = false
        }
    }

    private fun createCustomCategoryChip(categoryName: String) {
        customCategoryChip = Chip(requireContext()).apply {
            text = categoryName
            isCheckable = true
            isChecked = true
            setOnLongClickListener {
                isEditingCategory = true
                showCustomCategoryInput(categoryName)
                true
            }
        }

        binding.categoryChipGroup.addView(customCategoryChip)
        binding.chipOther.isChecked = false
    }

    private fun showCustomCategoryInput(existingCategory: String = "") {
        binding.customCategoryLayout.visibility = View.VISIBLE
        binding.customCategoryEditText.setText(existingCategory)
        binding.customCategoryEditText.requestFocus()
    }

    private fun hideCustomCategoryInput() {
        binding.customCategoryLayout.visibility = View.GONE
        binding.customCategoryEditText.text?.clear()
        isEditingCategory = false
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

        val uploadSuccess: (String) -> Unit = { uploadedImageUrl ->
            savePostToFirestore(uploadedImageUrl, caption, category, items, isDraft)
        }

        val uploadError: (String) -> Unit = { errorMessage ->
            Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
        }

        when {
            imageUri != null -> lifecycleScope.launch {
                CloudinaryModel.uploadImageFromUri(requireContext(), imageUri!!)?.let(uploadSuccess) ?: uploadError("Image upload failed")
            }
            imageBitmap != null -> CloudinaryModel.uploadImageFromBitmap(imageBitmap!!, requireContext(), onSuccess = uploadSuccess, onError = uploadError)
        }
    }


    private fun getSelectedCategory(): String {
        val selectedChipId = binding.categoryChipGroup.checkedChipId
        return if (selectedChipId != View.NO_ID) {
            binding.categoryChipGroup.findViewById<Chip>(selectedChipId)?.text.toString()
        } else {
            customCategoryChip?.takeIf { it.isChecked }?.text.toString()
        }
    }

    private fun getItemsFromViews(): List<String> {
        return itemViews.mapNotNull { view ->
            val itemName = view.findViewById<TextInputEditText>(R.id.itemNameInput).text.toString().trim()
            val shopBrand = view.findViewById<TextInputEditText>(R.id.shopBrandInput).text.toString().trim()
            val price = view.findViewById<TextInputEditText>(R.id.priceInput).text.toString().trim()

            if (itemName.isNotEmpty() && shopBrand.isNotEmpty() && price.isNotEmpty()) {
                "$itemName - $shopBrand - $price"
            } else null
        }
    }


    private fun savePostToFirestore(
        imageUrl: String,
        caption: String,
        category: String,
        items: List<String>,
        isDraft: Boolean
    ) {
        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val postData = mapOf(
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

        FirebaseFirestore.getInstance().collection("posts").document(postId)
            .set(postData)
            .addOnSuccessListener {
                savePostToRoom(postId, userId, imageUrl, caption, category, items, isDraft)
            }
            .addOnFailureListener { error ->
                Log.e("FirestoreError", "Failed to save post: ${error.message}")
                Toast.makeText(requireContext(), "Failed to save post", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}