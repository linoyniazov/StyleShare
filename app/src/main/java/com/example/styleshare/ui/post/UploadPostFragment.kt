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
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.styleshare.databinding.FragmentUploadPostBinding
import com.example.styleshare.model.AppDatabase
import com.example.styleshare.model.CloudinaryModel
import com.example.styleshare.model.entities.Post
import com.example.styleshare.repository.PostRepository
import com.example.styleshare.viewmodel.PostViewModel
import kotlinx.coroutines.launch
import java.util.*

class UploadPostFragment : Fragment() {

    private var _binding: FragmentUploadPostBinding? = null
    private val binding get() = _binding!!

    private val postViewModel: PostViewModel by viewModels {
        PostViewModel.PostViewModelFactory(PostRepository(AppDatabase.getDatabase(requireContext()).postDao()))
    }

    private var imageUri: Uri? = null
    private var imageBitmap: Bitmap? = null
    private val itemList = mutableListOf<String>() // ✅ רשימה לפריטים בפוסט

    /** ✅ בוחר תמונה מהגלריה **/
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                imageUri = it
                imageBitmap = null
                binding.postImage.setImageURI(it)
            }
        }

    /** ✅ מצלם תמונה **/
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
        inflater: LayoutInflater, container: ViewGroup?,
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
            addItem()
        }

        binding.postButton.setOnClickListener {
            uploadPost(isDraft = false)
        }

        binding.saveDraftButton.setOnClickListener {
            uploadPost(isDraft = true)
        }
    }

    /** ✅ מציג אפשרות לבחור תמונה מהגלריה או לצלם תמונה **/
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

    /** ✅ מעלה את התמונה ל- Cloudinary ושומר במסד הנתונים **/
    private fun uploadPost(isDraft: Boolean) {
        val caption = binding.captionEditText.text.toString()
        val category = getSelectedCategory()

        if ((imageUri == null && imageBitmap == null) || caption.isEmpty() || category.isEmpty()) {
            Toast.makeText(requireContext(), "Please select an image and enter details", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            lifecycleScope.launch {
                val uploadedImageUrl = CloudinaryModel.uploadImageFromUri(requireContext(), imageUri!!)
                if (uploadedImageUrl != null) {
                    Log.d("CloudinaryUpload", "Uploaded Image URL: $uploadedImageUrl")
                    savePostToDatabase(uploadedImageUrl, caption, category, isDraft)
                } else {
                    Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else if (imageBitmap != null) {
            CloudinaryModel.uploadImageFromBitmap(imageBitmap!!, requireContext(),
                onSuccess = { uploadedImageUrl ->
                    savePostToDatabase(uploadedImageUrl, caption, category, isDraft)
                },
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), "Upload failed: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    /** ✅ מקבל את הקטגוריה שנבחרה */
    private fun getSelectedCategory(): String {
        return when {
            binding.chipCasual.isChecked -> "Casual"
            binding.chipElegant.isChecked -> "Elegant"
            binding.chipParty.isChecked -> "Party"
            binding.chipFormal.isChecked -> "Formal"
            binding.chipEvening.isChecked -> "Evening"
            binding.chipOther.isChecked -> "Other"
            else -> ""
        }
    }

    /** ✅ שמירת הפוסט במסד הנתונים **/
    private fun savePostToDatabase(imageUrl: String, caption: String, category: String, isDraft: Boolean) {
        val post = Post(
            postId = UUID.randomUUID().toString(),
            userId = "user123",
            imageUrl = imageUrl,
            caption = caption,
            category = category,
            timestamp = System.currentTimeMillis(),
            likedBy = emptyList(),
            commentsCount = 0,
            items = itemList.toList(),
            isDraft = isDraft
        )

        postViewModel.insertPost(post)

        val message = if (isDraft) "Draft saved successfully!" else "Post uploaded successfully!"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        findNavController().navigateUp() // ✅ חזרה למסך הקודם
    }

    /** ✅ הוספת פריטים לפוסט */
    private fun addItem() {
        val itemName = binding.itemNameInput.text.toString().trim()
        val shopBrand = binding.shopBrandInput.text.toString().trim()
        val price = binding.priceInput.text.toString().trim()

        if (itemName.isNotEmpty() && shopBrand.isNotEmpty() && price.isNotEmpty()) {
            val newItem = "$itemName - $shopBrand ($$price)"
            itemList.add(newItem)
            binding.itemNameInput.text?.clear()
            binding.shopBrandInput.text?.clear()
            binding.priceInput.text?.clear()
            Toast.makeText(requireContext(), "Item added!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Fill all item fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}