package com.example.studylog.navigation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.studylog.*
import com.example.studylog.databinding.ItemUserPhotoBinding
import com.example.studylog.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_add_photo.*
import kotlinx.android.synthetic.main.activity_add_todo.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_grid.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null

    var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid
        auth = Firebase.auth

        if (uid == currentUserUid) {
            //MyPage

        } else {
            //OtherUserPage
            var mainactivity = (activity as MainActivity)
            mainactivity?.toolbar_username?.text = arguments?.getString("userId")
            mainactivity?.toolbar_btn_back?.setOnClickListener {
                mainactivity.bottom_navigation.selectedItemId = R.id.action_home
            }
            mainactivity?.toolbar_title_image?.visibility = View.GONE
            mainactivity?.toolbar_username?.visibility = View.VISIBLE
            mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
        }
        fragmentView?.account_reyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_reyclerview?.layoutManager = GridLayoutManager(context,3)

        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()

        fragmentView?.logout_button?.setOnClickListener {
            // 로그인 화면으로
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()
        }

        fragmentView?.list?.setOnClickListener {
            val intent = Intent(requireContext(), UserListActivity::class.java)
            startActivity(intent)
        }

        return fragmentView
    }

    fun getProfileImage() {
        // fixme

        if (uid != null) {
            firestore?.collection("profileImages")?.document(uid!!)
                ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    if (documentSnapshot == null) return@addSnapshotListener
                    if (documentSnapshot.data != null) {
                        var url = documentSnapshot.data!!["image"]
                        Glide.with(fragmentView!!).load(url).apply(RequestOptions().circleCrop())
                            .into(fragmentView?.account_iv_profile as ImageView)
                    }
                }
        } else {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            auth?.signOut()
        }

    }

    override fun onResume() {
        super.onResume()
        contentDTOs.clear()
        firestore?.collection("images")?.whereEqualTo("uid", uid)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                //Sometimes, This code return null of querySnapshot when it signout
                if (querySnapshot == null) return@addSnapshotListener

                //Get data
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }

                fragmentView?.account_reyclerview?.adapter = UserFragmentRecyclerViewAdapter()
                fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
            }
    }


    inner class UserFragmentRecyclerViewAdapter :
        RecyclerView.Adapter<UserFragmentRecyclerViewAdapter.CustomViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
//            var width = resources.displayMetrics.widthPixels / 3

//            var imageView = ImageView(p0.context)
//            imageView.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(
                ItemUserPhotoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        //게시물 삭제 기능

        fun deletePhotoItem(item: ContentDTO) {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.whereEqualTo(" l", item.imageUrl)
                ?.get()
                ?.addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.delete()
                    }
                }
                ?.addOnFailureListener { exception ->
                }
            contentDTOs.remove(item)
            notifyDataSetChanged()

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            holder.bind(contentDTOs[position])

        }

        inner class CustomViewHolder(private val binding: ItemUserPhotoBinding) :
            BaseViewHolder<ContentDTO>(binding) {
            override fun define(item: ContentDTO) {
                Glide.with(binding.root.context).load(item.imageUrl).into(binding.itemImage)
                binding.itemImage.setOnClickListener {

                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {

                        val intent =
                            Intent(requireContext(), UserPostActivity::class.java)
                        intent.putExtra("fromUser",true)
                        intent.putExtra("uid", contentDTOs[position].uid)
                        intent.putExtra("userid",contentDTOs[position].userId)
                        startActivity(intent)
                    }

                }
            }
        }

    }
}
