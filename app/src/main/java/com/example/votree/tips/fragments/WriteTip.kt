package com.example.votree.tips.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.R
import com.example.votree.databinding.ActivityWriteTipBinding
import com.example.votree.tips.models.ProductTip
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class WriteTip : AppCompatActivity(R.layout.activity_write_tip) {
    private val fireStoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()
    var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWriteTipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                Toast.makeText(this, "Selected 1 image", Toast.LENGTH_SHORT).show()
                binding.writeTipPreviewIv.setImageURI(uri)
                imageUri = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
                Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Add thumbnail button
        findViewById<MaterialButton>(R.id.add_thumbnail_btn).setOnClickListener{
            Log.d("PhotoPicker", "Launching photo picker")
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.submitNewTipButton.setOnClickListener{
            val tip = ProductTip(
                0,
                content=binding.tipContentInputEditText.text.toString(),
                createdAt=FieldValue.serverTimestamp().toString(),
                updatedAt=FieldValue.serverTimestamp().toString(),
                shortDescription=binding.tipShortDescriptionInputEditText.text.toString(),
                title=binding.tipTitleInputEditText.text.toString(),
                userId = "1",
                vote=0)
            pushTiptoDatabase(tip)
        }
        binding.cancelNewTipButton.setOnClickListener{
            finish()
        }
    }

    private fun pushTiptoDatabase(tip: ProductTip) {
        val storageRef = storageInstance.reference
        if (imageUri == null) {
            Toast.makeText(this, "Please add an image", Toast.LENGTH_SHORT).show()
            return
        }
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    tip.imageList[0] = uri.toString()
                    fireStoreInstance.collection("ProductTip").add(tip)
                        .addOnSuccessListener { documentReference ->
                            val documentId = documentReference.id.toString()
                            fireStoreInstance.collection("products").document(documentId)
                                .update("id", documentId);
                            Toast.makeText(
                                this,
                                "Product added successfully",
                                Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT)
                                .show();
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
