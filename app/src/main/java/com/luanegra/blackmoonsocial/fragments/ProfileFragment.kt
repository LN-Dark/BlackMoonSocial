package com.luanegra.blackmoonsocial.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.luanegra.blackmoonsocial.R
import com.luanegra.blackmoonsocial.models.ASeguir
import com.luanegra.blackmoonsocial.models.Posts
import com.luanegra.blackmoonsocial.models.Users
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

class ProfileFragment : Fragment() {
    private var firebaseUser: FirebaseUser?= null
    private var RequestCode: Int = 1
    private var storageProfileRef: StorageReference? = null
    private var storageCoverRef: StorageReference? = null
    private var following_profile: TextView? = null
    private var posts_profile: TextView? = null
    private var follow_profile: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val cover_profile: ImageView = view.findViewById(R.id.cover_profile)
        val profileimage_profile: CircleImageView = view.findViewById(R.id.profileimage_profile)
        val aboutme_profile: TextInputEditText = view.findViewById(R.id.aboutme_profile)
        val check_notifications_profile: SwitchMaterial = view.findViewById(R.id.check_notifications_profile)
        following_profile = view.findViewById(R.id.following_profile)
        posts_profile = view.findViewById(R.id.posts_profile)
        follow_profile = view.findViewById(R.id.follow_profile)
        storageProfileRef = FirebaseStorage.getInstance().reference.child("profileImages")
        storageCoverRef = FirebaseStorage.getInstance().reference.child("coverImages")
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                profileimage_profile.load(user!!.getprofile())
                cover_profile.load(user.getcover())
                aboutme_profile.setText(user.getaboutMe())
                check_notifications_profile.isChecked = user.getnotificationsShow()
                check_notifications_profile.setOnClickListener {
                    notificationUpdate(check_notifications_profile.isChecked)
                }
                aboutme_profile.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        saveAboutme(s.toString())
                        aboutme_profile.setSelection(aboutme_profile.text!!.length)
                    }

                })
                cover_profile.setOnClickListener {
                    RequestCode = 2
                    pickimage()
                }
                profileimage_profile.setOnClickListener {
                    RequestCode = 1
                    pickimage()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(context,error.message,FancyToast.LENGTH_LONG, FancyToast.ERROR,true)
            }
        })
        getFollowers()
        getFollowing()
        getPosts()
        return view
    }

    fun getFollowers(){
        FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followeersCount = 0
                for (datasnapShot in snapshot.children){
                    val user: Users? = datasnapShot.getValue(Users::class.java)
                    if(user!!.getUid() != firebaseUser!!.uid){
                        FirebaseDatabase.getInstance().reference.child("users").child(user!!.getUid()).child("following").addValueEventListener(object: ValueEventListener{
                            override fun onDataChange(snapshotFollowers: DataSnapshot) {
                                for (datasnapFollowers in snapshotFollowers.children){
                                    val follower: ASeguir? = datasnapFollowers.getValue(ASeguir::class.java)
                                    if (follower!!.getfollowingID() == firebaseUser!!.uid){
                                        followeersCount += 1
                                        follow_profile!!.text = "Followed by $followeersCount"
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getFollowing(){
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).child("following").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var followeersCount = -1
                for (datasnapShot in snapshot.children){
                    followeersCount += 1
                }
                following_profile!!.text = "Following $followeersCount"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun getPosts(){
        FirebaseDatabase.getInstance().reference.child("Posts").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var postsCount = 0
                for (datasnapShot in snapshot.children){
                    val posts: Posts? = datasnapShot.getValue(Posts::class.java)
                    if (posts!!.getuserID() == firebaseUser!!.uid){
                        postsCount += 1
                    }
                }
                posts_profile!!.text = "$postsCount Posts"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    fun notificationUpdate(isShow: Boolean){
        val map = HashMap<String, Any>()
        map["notificationsShow"] = isShow
        FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).updateChildren(map)
    }

    private fun saveAboutme(newAboutme: String){
        if(newAboutme.isNotEmpty()){
            val map = HashMap<String, Any>()
            map["aboutMe"] = newAboutme
            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).updateChildren(map)
        }
    }

    var easyImage: EasyImage? = null
    private fun pickimage(){
        easyImage = EasyImage.Builder(requireContext())
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
                requireActivity(),
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                        uploadImage(1, imageFiles[0].file)
                    }

                    override fun onImagePickerError(error: Throwable, source: MediaSource) {
                        FancyToast.makeText(context,error.message,FancyToast.LENGTH_LONG,FancyToast.WARNING,true)
                    }

                    override fun onCanceled(source: MediaSource) {

                    }
                })
        }else if(RequestCode == 2 && resultCode == Activity.RESULT_OK && data!!.data != null){
            easyImage!!.handleActivityResult(
                requestCode,
                resultCode,
                data,
                requireActivity(),
                object : DefaultCallback() {
                    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
                        uploadImage(2, imageFiles.get(0).file)
                    }

                    override fun onImagePickerError(error: Throwable, source: MediaSource) {
                        FancyToast.makeText(context,error.message,FancyToast.LENGTH_LONG,FancyToast.WARNING,true)
                    }

                    override fun onCanceled(source: MediaSource) {
                    }
                })
        }
    }

    private fun uploadImage(type: Int, filetoup: File){
        val loadingBar = ProgressDialog(context)
        if(type == 1){
            val fileRef = storageProfileRef!!.child(
                System.currentTimeMillis().toString() + ".jpg"
            )

            lifecycleScope.launch {
                val compressedImageFile = Compressor.compress(requireView().context, filetoup){
                    quality(75)
                    format(Bitmap.CompressFormat.JPEG)
                }
                if(compressedImageFile.length() < 30000000){
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
                            val downloadUrl = task.result
                            val map = HashMap<String, Any>()
                            map["profile"] = downloadUrl.toString()
                            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).updateChildren(map)
                            loadingBar.dismiss()
                        }
                    }
                }else{
                    FancyToast.makeText(context,"Image is too big, please select another!",FancyToast.LENGTH_LONG,FancyToast.WARNING,true)
                }
            }


        }else if(type == 2){
            val fileRef = storageCoverRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            lifecycleScope.launch {
                val compressedImageFile = Compressor.compress(requireView().context, filetoup){
                    quality(25)
                    format(Bitmap.CompressFormat.JPEG)
                }
                if(compressedImageFile.length() < 30000000){
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
                            val downloadUrl = task.result
                            val map = HashMap<String, Any>()
                            map["cover"] = downloadUrl.toString()
                            FirebaseDatabase.getInstance().reference.child("users").child(firebaseUser!!.uid).updateChildren(map)
                            loadingBar.dismiss()
                        }
                    }
                }else{
                    FancyToast.makeText(context,"Image is too big, please select another!",FancyToast.LENGTH_LONG,FancyToast.WARNING,true)
                }
            }
        }
    }




}