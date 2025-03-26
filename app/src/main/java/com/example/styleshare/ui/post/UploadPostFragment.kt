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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentUploadPostBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.CloudinaryModel
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.viewmodel.PostViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.*
import com.bumptech.glide.Glide


class UploadPostFragment : Fragment() {

    private var _binding: FragmentUploadPostBinding? = null
    private val binding get() = _binding!!
    private var editingPostId: String? = null
    private var originalImageUrl: String? = null


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

        binding.backButton.setOnClickListener {
            // This will pop the current fragment and return to the previous one
            findNavController().navigateUp()
        }
        binding.postButton.apply {
            visibility = View.VISIBLE
            bringToFront() // Ensure it's on top of other views
        }

        setupChipGroupListeners()
        setupCustomCategoryHandlers()

        val postId = arguments?.getString("postId")
        if (postId != null) {
            // מצב עריכה
            FirebaseFirestore.getInstance().collection("posts").document(postId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val post = Post(
                            postId = document.getString("postId") ?: "",
                            userId = document.getString("userId") ?: "",
                            username = document.getString("username") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            caption = document.getString("caption") ?: "",
                            category = document.getString("category") ?: "",
                            timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                            items = (document.get("items") as? List<*>)?.filterIsInstance<String>()
                                ?: emptyList()
                        )

                        binding.captionEditText.setText(post.caption)
                        loadCategoryIntoChip(post.category)
                        loadItemsIntoViews(post.items)
                        Glide.with(this).load(post.imageUrl).into(binding.postImage)

                        originalImageUrl = post.imageUrl
                        editingPostId = post.postId

                        binding.postButton.text = "Update"

                    } else {
                        Toast.makeText(requireContext(), "הפוסט לא נמצא", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "שגיאה בטעינת הפוסט", Toast.LENGTH_SHORT)
                        .show()
                    findNavController().navigateUp()
                }
        }

        binding.postImage.setOnClickListener {
            showImagePickerOptions()
        }

        binding.addMoreItemButton.setOnClickListener {
            addNewItemFields()
        }

        binding.postButton.setOnClickListener {
            val caption = binding.captionEditText.text.toString()
            val category = getSelectedCategory()
            val items = getItemsFromViews()

            if (caption.isEmpty() || category.isEmpty() || items.isEmpty()) {
                Toast.makeText(requireContext(), "יש למלא את כל השדות", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            val onComplete: (String) -> Unit = { imageUrl ->
                if (editingPostId != null) {
                    updatePost(editingPostId!!)
                } else {
                    savePostToFirestore(imageUrl, caption, category, items)
                }
            }

            val onError: (String) -> Unit = { error ->
                Toast.makeText(requireContext(), "שגיאה בהעלאת תמונה: $error", Toast.LENGTH_SHORT)
                    .show()
            }

            lifecycleScope.launch {
                try {
                    when {
                        imageUri != null -> {
                            val uploadedUrl =
                                CloudinaryModel.uploadImageFromUri(requireContext(), imageUri!!)
                            if (uploadedUrl != null) {
                                onComplete(uploadedUrl)
                            } else {
                                onError("Image upload failed")
                            }
                        }

                        imageBitmap != null -> {
                            CloudinaryModel.uploadImageFromBitmap(
                                bitmap = imageBitmap!!,
                                context = requireContext(),
                                onSuccess = onComplete,
                                onError = onError
                            )
                        }

                        else -> {
                            originalImageUrl?.let { onComplete(it) }
                                ?: onError("לא נבחרה תמונה")
                        }
                    }
                } catch (e: Exception) {
                    onError("Exception: ${e.message}")
                }
            }
        }
    }

    private fun loadCategoryIntoChip(category: String) {
        val chip = when (category.lowercase()) {
            "casual" -> binding.chipCasual
            "elegant" -> binding.chipElegant
            "party" -> binding.chipParty
            "formal" -> binding.chipFormal
            "evening" -> binding.chipEvening
            else -> {
                createCustomCategoryChip(category)
                customCategoryChip
            }
        }
        chip?.isChecked = true
    }

    private fun setupChipGroupListeners() {
        binding.chipOther.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !isEditingCategory) {
                showCustomCategoryInput()
            }
        }

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
    private fun loadItemsIntoViews(items: List<String>) {
        items.forEach { item ->
            addNewItemFields()
            val lastView = itemViews.last()
            val parts = item.split(" - ")
            if (parts.size == 3) {
                lastView.findViewById<TextInputEditText>(R.id.itemNameInput).setText(parts[0])
                lastView.findViewById<TextInputEditText>(R.id.shopBrandInput).setText(parts[1])
                lastView.findViewById<TextInputEditText>(R.id.priceInput).setText(parts[2])
            }
        }
    }

    private fun setupCustomCategoryHandlers() {
        binding.confirmCategoryButton.setOnClickListener {
            val categoryName = binding.customCategoryEditText.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                if (customCategoryChip != null) {
                    customCategoryChip?.text = categoryName
                } else {
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

        itemView.findViewById<TextView>(R.id.itemNumberLabel).text = "Item $itemCount"

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

    private fun updatePost(postId: String) {
        val caption = binding.captionEditText.text.toString()
        val category = getSelectedCategory()
        val items = getItemsFromViews()

        if (caption.isEmpty() || category.isEmpty() || items.isEmpty()) {
            Toast.makeText(requireContext(), "יש למלא את כל השדות", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val onComplete: (String) -> Unit = { imageUrl ->
            // שליפת שם משתמש מה־Firestore לפי ה־userId
            FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Unknown"

                    val updatedPost = mapOf(
                        "postId" to postId,
                        "userId" to userId,
                        "username" to username,
                        "imageUrl" to imageUrl,
                        "caption" to caption,
                        "category" to category,
                        "timestamp" to System.currentTimeMillis(),
                        "items" to items
                    )

                    FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(postId)
                        .set(updatedPost)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "הפוסט עודכן!", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "שגיאה בעדכון הפוסט: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "שגיאה בשליפת שם המשתמש", Toast.LENGTH_SHORT).show()
                }
        }

        val onError: (String) -> Unit = { error ->
            Toast.makeText(requireContext(), "שגיאה בהעלאת תמונה: $error", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            try {
                when {
                    imageUri != null -> {
                        val uploadedUrl = CloudinaryModel.uploadImageFromUri(requireContext(), imageUri!!)
                        if (uploadedUrl != null) {
                            onComplete(uploadedUrl)
                        } else {
                            onError("Image upload failed")
                        }
                    }
                    imageBitmap != null -> {
                        CloudinaryModel.uploadImageFromBitmap(
                            bitmap = imageBitmap!!,
                            context = requireContext(),
                            onSuccess = onComplete,
                            onError = onError
                        )
                    }
                    else -> {
                        originalImageUrl?.let { onComplete(it) }
                            ?: onError("לא קיימת תמונה לפוסט")
                    }
                }
            } catch (e: Exception) {
                onError("Exception: ${e.message}")
            }
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
        items: List<String>
    ) {
        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val post = Post(
                        postId = document.getString("postId") ?: "",
                        userId = document.getString("userId") ?: "",
                        username = document.getString("username") ?: "",
                        imageUrl = document.getString("imageUrl") ?: "",
                        caption = document.getString("caption") ?: "",
                        category = document.getString("category") ?: "",
                        timestamp = document.getLong("timestamp") ?: System.currentTimeMillis(),
                        items = (document.get("items") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    )

                    binding.captionEditText.setText(post.caption)
                    loadCategoryIntoChip(post.category)
                    loadItemsIntoViews(post.items)
                    Glide.with(this).load(post.imageUrl).into(binding.postImage)

                    originalImageUrl = post.imageUrl
                    editingPostId = post.postId

                    binding.postButton.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "הפוסט לא נמצא", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
    }

    private fun savePostToRoom(
        postId: String,
        userId: String,
        username: String,
        imageUrl: String,
        caption: String,
        category: String,
        items: List<String>
    ) {
        lifecycleScope.launch {
            val userExists = AppDatabase.getDatabase(requireContext()).userDao().doesUserExist(userId) > 0

            if (!userExists) {
                Toast.makeText(requireContext(), "משתמש לא קיים במסד המקומי", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val post = Post(
                postId = postId,
                userId = userId,
                username = username,
                imageUrl = imageUrl,
                caption = caption,
                category = category,
                timestamp = System.currentTimeMillis(),
                items = items
            )

            postViewModel.insertPost(post)
            Toast.makeText(requireContext(), "הפוסט הועלה בהצלחה!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}