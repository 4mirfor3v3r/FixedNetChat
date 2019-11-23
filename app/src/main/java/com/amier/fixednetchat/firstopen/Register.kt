package com.amier.fixednetchat.firstopen

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import com.amier.fixednetchat.MainActivity
import com.amier.fixednetchat.Models.User
import com.amier.fixednetchat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_register.*
import me.itangqi.waveloadingview.WaveLoadingView
import java.io.File
import java.util.*

class Register : AppCompatActivity() {
    private lateinit var tempImage: File
    private lateinit var loading : WaveLoadingView

    companion object {
        const val TAG = "Register"
        var photoUri: Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_btn.setOnClickListener {
            performRegister()
        }

        register_log.setOnClickListener {
            Log.d(TAG, "Try to show login activity")

            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        register_photo_btn.setOnClickListener {
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        loading = findViewById(R.id.register_loading)
    }

    private fun performRegister() {
        val email = register_email.text.toString()
        val password = register_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
            return
        }
        register_container.visibility = View.INVISIBLE
        loading.visibility = View.VISIBLE
        loading.startAnimation()
        loading.topTitle = "Registering"

        Log.d(TAG, "Attempting to create user with email: $email")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

//                Log.d(TAG, "Successfully created user with uid: ${it.result.user.uid}")
                if(photoUri != null){
                    uploadImageToFirebaseStorage()
                }
                else{
                    saveUserToDatabase("null")
                }
            }
            .addOnFailureListener{
                Log.d(TAG, "Failed to create user: ${it.message}")
                register_container.visibility = View.VISIBLE
                loading.cancelAnimation()
                loading.visibility = View.GONE
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            photoUri = data.data

            tempImage = File.createTempFile("profile",".png")

            CropImage.activity(photoUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1,1)
                .setGuidelinesThickness(3f)
                .setMaxCropResultSize(300,300)
                .setGuidelinesColor(Color.argb(130,0,0,0))
                .setBorderCornerColor(Color.argb(130,0,0,0))
                .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                .setOutputUri(tempImage.toUri())
                .setFixAspectRatio(true)
                .start(this)
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, tempImage.toUri())
                register_photo.setImageBitmap(bitmap)
                Log.d("Reg Image Cropped: ", tempImage.toUri().toString())
                register_photo_btn.alpha = 0f

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
//                Log.d("Register CROP Image : ", error.message)
            }
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (photoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(photoUri!!)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                loading.progressValue = progress.toInt()
                loading.centerTitle = "${String.format("%.1f", progress).toDouble()} %"
                loading.bottomTitle = "${taskSnapshot.bytesTransferred.div(1000)} kb / ${taskSnapshot.totalByteCount.div(1000)} kb "
                loading.topTitle = "Uploading"
            }
            .addOnSuccessListener { _ ->
                //                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File Location: $it")
                    loading.topTitle = "Finishing"
                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener {

                //                Log.d(TAG, "Failed to upload image to storage: ${it.message}")

            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, register_username.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")
                loading.cancelAnimation()
                loading.visibility = View.GONE
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database: ${it.message}")
                loading.cancelAnimation()
                loading.visibility = View.GONE
                register_container.visibility = View.VISIBLE
            }
    }
}
