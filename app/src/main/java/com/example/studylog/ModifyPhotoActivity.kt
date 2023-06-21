package com.example.studylog

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studylog.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_btn_upload
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_edit_explain
import kotlinx.android.synthetic.main.activity_add_photo.addphoto_image
import kotlinx.android.synthetic.main.activity_modify_photo.*
import java.text.SimpleDateFormat
import java.util.*

class ModifyPhotoActivity :AppCompatActivity(){

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage : FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth : FirebaseAuth? = null
    var firestore : FirebaseFirestore? = null

    var uid : String = ""
    var url : String = ""
    var newPhotoUri: String = ""

    var content: ContentDTO? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_modify_photo)

        //Initiate
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //add image upload event

        uid = intent.getStringExtra("uid")!!
        url = intent.getStringExtra("url")!!

        getPhotoData(url,uid)

        addphoto_btn_upload.setOnClickListener {
            content?.let { it1 -> contentUpdate(it1) }
        }

        modifyphoto_image.setOnClickListener{
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,PICK_IMAGE_FROM_ALBUM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_FROM_ALBUM){
            if (resultCode == Activity.RESULT_OK){
                //This is path to the selected image
                photoUri = data?.data
                modifyphoto_image.setImageURI(photoUri)


                var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                var imageFileName = "IMAGE_" + timestamp + "_.png"
                var storageRef = storage?.reference?.child("images")?.child(imageFileName)

                //Promise method

                // 프로그레스 바를 표시하기 위한 변수
                var progressBar: ProgressBar? = null

                progressBar = findViewById(R.id.progressBar) // XML에서 정의한 프로그레스 바의 ID를 사용합니다.
                progressBar?.visibility = View.VISIBLE

                storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                    return@continueWithTask  storageRef.downloadUrl
                }?.addOnSuccessListener { uri ->
                    content?.imageUrl = uri.toString()
                    newPhotoUri = uri.toString()


                    progressBar?.visibility = View.GONE
                }
            }else{

            }
        }
    }

    //추가

    fun getPhotoData(url: String, uid: String){
        firestore?.collection("images")?.whereEqualTo("uid", uid)
            ?.whereEqualTo("imageUrl", url)
            ?.get()
            ?.addOnSuccessListener { querySnapshot ->
                for (snapshot in querySnapshot.documents) {
                    val contentDTO = snapshot.toObject(ContentDTO::class.java)
                    content = contentDTO
                    content?.id = snapshot.id
                    Glide.with(this).load(contentDTO!!.imageUrl).into(modifyphoto_image)
                    modifyphoto_edit_explain.setText(contentDTO.explain)
                }
            }
            ?.addOnFailureListener { exception ->
            }
    }

    // 추가
    fun contentUpdate(contentDTO: ContentDTO){
        contentDTO.id?.let {
            contentDTO.explain = modifyphoto_edit_explain.text.toString()
            if(newPhotoUri.isNotEmpty()) {
                contentDTO.imageUrl = newPhotoUri
            }
            firestore?.collection("images")?.document(it)
                ?.set(contentDTO)
                ?.addOnSuccessListener {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                ?.addOnFailureListener { exception ->

                }
        }
    }
}