package com.luanegra.blackmoonsocial

import android.R.attr.bitmap
import android.R.attr.path
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.shashank.sony.fancytoastlib.FancyToast
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class NewPostActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser?= null
    private var RequestCode: Int = 1
    private var storageRef: StorageReference? = null
    private var PhotoFile: File? = null
    private var txt_description_new_post: TextInputEditText? = null
    var easyImage: EasyImage? = null
    var img_new_post: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post)
        setSupportActionBar(findViewById(R.id.toolbar_newpost))
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_newpost)
        val user_name_newpost: TextView = findViewById(R.id.user_name_newpost)
        val profile_image_newpost: CircleImageView = findViewById(R.id.profile_image_newpost)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        storageRef = FirebaseStorage.getInstance().reference.child("Posts")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        user_name_newpost.text = intent.getStringExtra("user_name_newpost")
        profile_image_newpost.load(intent.getStringExtra("profile_image_newpost"))
        img_new_post = findViewById(R.id.img_new_post)
        txt_description_new_post = findViewById(R.id.txt_description_new_post)
        val btn_post_new_post: Button = findViewById(R.id.btn_post_new_post)
        btn_post_new_post.setOnClickListener{
            uploadImage()
        }
        img_new_post!!.setOnClickListener {
            RequestCode = 1
            pickimage()
        }
    }

    private fun pickimage(){
        easyImage = EasyImage.Builder(this)
            .setCopyImagesToPublicGalleryFolder(false)
            .setFolderName("Select Image")
            .allowMultiple(false)
            .build()
        easyImage!!.openChooser(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(RequestCode == 1 && resultCode == Activity.RESULT_OK && data!!.data != null){
            easyImage!!.handleActivityResult(
                requestCode,
                resultCode,
                data,
                this,
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(
                        imageFiles: Array<MediaFile>,
                        source: MediaSource
                    ) {
                        PhotoFile = imageFiles[0].file
                        img_new_post!!.load(imageFiles[0].file)
                    }

                    override fun onImagePickerError(error: Throwable, source: MediaSource) {
                        FancyToast.makeText(
                            this@NewPostActivity, error.message,
                            FancyToast.LENGTH_LONG,
                            FancyToast.WARNING, true
                        )
                    }

                    override fun onCanceled(source: MediaSource) {

                    }
                })
        }
    }

    private fun uploadImage(){
       if (txt_description_new_post!!.text.toString() != ""){
           if (PhotoFile != null){
               val loadingBar = ProgressDialog(this)
               val fileRef = storageRef!!.child(
                   System.currentTimeMillis().toString() + ".jpg"
               )
               lifecycleScope.launch {
                   val compressedImageFile = Compressor.compress(this@NewPostActivity, PhotoFile!!){
                       quality(90)
                       format(Bitmap.CompressFormat.JPEG)
                   }
                   if(compressedImageFile!!.length() < 30000000){
                       loadingBar.setMessage("Please wait")
                       loadingBar.setCancelable(false)
                       loadingBar.show()
                       val uploadTask2: UploadTask = fileRef.putBytes(compressedImageFile.readBytes())
                       uploadTask2.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                           if (task.isSuccessful) {
                               task.exception?.let {
                                   throw it
                               }
                           }
                           return@Continuation fileRef.downloadUrl
                       }).addOnCompleteListener { task ->
                           if(task.isSuccessful){
                               var newPostKey = FirebaseDatabase.getInstance().reference.child("Posts").push().key
                               val downloadUrl = task.result
                               val current = LocalDateTime.now()
                               val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")
                               val formatted = current.format(formatter)
                               val map = HashMap<String, Any>()
                               map["uid"] = newPostKey.toString()
                               map["userID"] = firebaseUser!!.uid
                               map["photoURL"] = downloadUrl.toString()
                               map["date"] = formatted
                               map["description"] = txt_description_new_post!!.text.toString()
                               FirebaseDatabase.getInstance().reference.child("Posts").child(
                                   newPostKey.toString()
                               ).setValue(map).addOnCompleteListener {
                                   loadingBar.dismiss()
                                   val intent = Intent(
                                       this@NewPostActivity,
                                       MainActivity::class.java
                                   )
                                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                   intent.putExtra("resultAUTH", "true")
                                   startActivity(intent)
                                   finish()
                               }
                           }
                       }
                   }else{
                       FancyToast.makeText(
                           this@NewPostActivity,
                           "Image is too big, please select another!",
                           FancyToast.LENGTH_LONG,
                           FancyToast.WARNING,
                           true
                       )
                   }
               }
           }else{
               FancyToast.makeText(
                   this@NewPostActivity,
                   "Select an image first!",
                   FancyToast.LENGTH_LONG,
                   FancyToast.WARNING,
                   true
               )
           }
       }else{
           FancyToast.makeText(
               this@NewPostActivity,
               "Write a description first!",
               FancyToast.LENGTH_LONG,
               FancyToast.WARNING,
               true
           )
       }
    }
}