package kot.note.musashi.com.appunti.upload


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import kotlinx.android.synthetic.main.fragment_upload_pictures.*
import permissions.dispatcher.RuntimePermissions
import android.provider.MediaStore
import android.content.Intent
import android.net.Uri
import android.support.v4.view.MotionEventCompat

import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import kot.note.musashi.com.appunti.R
import permissions.dispatcher.NeedsPermission
import xyz.dev_juyoung.cropicker.CroPicker
import android.util.Log
import android.view.MotionEvent
import com.squareup.picasso.Picasso
import kot.note.musashi.com.appunti.extensions.*
import kotlinx.android.synthetic.main.user_image_card.*
import kotlinx.android.synthetic.main.user_image_card.view.*
import xyz.dev_juyoung.cropicker.models.Media
import java.io.File
import java.util.*


@RuntimePermissions
class UploadPictures : Fragment(), OnStartDragListener {

    private val cameraRequestCode = 0
    private var mItemTouchHelper : ItemTouchHelper?= null

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val root = inflateInContainer(R.layout.fragment_upload_pictures, container)



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        camera.setOnClickListener { goToCameraWithPermissionCheck() }
        gallery.setOnClickListener { goToGalleryWithPermissionCheck() }

        imageRecyclerView.layoutManager = GridLayoutManager(activity as AppCompatActivity,3)
        imageRecyclerView.setHasFixedSize(true)

    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            // handler per la camera
            cameraRequestCode -> {
                if(resultCode == Activity.RESULT_OK){
                    showInfo(data?.toUri(0).toString())
                }
            }
            // handler per la gallery
            CroPicker.REQUEST_ALBUM -> {
                if(resultCode == Activity.RESULT_OK){
                    val results = data?.getParcelableArrayListExtra<Media>(CroPicker.EXTRA_RESULT_IMAGES) as ArrayList<Media>
                    if(results == null || results.isEmpty()) showError("isnull or empty")
                    showInfo(results?.get(0)?.imagePath.toString())
                    val pathArray = mutableListOf<String>()
                    results?.forEach {
                        Log.d("DATA_IMAGES","${results.indexOf(it)} : path -> ${it.imagePath}")
                        pathArray.add(it.imagePath.toString())
                    }
                    val myAdapter = UserImageAdapter(pathArray,this)
                    val callback = SimpleItemTouchHelperCallback(myAdapter)
                    mItemTouchHelper = ItemTouchHelper(callback)
                    mItemTouchHelper?.attachToRecyclerView(imageRecyclerView)
                    imageRecyclerView.adapter = myAdapter
                }else{
                    showInfo("Result code not ok")
                }
            }

        }



    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun goToGallery(){
        val options = CroPicker.Options()
        options.setToolbarTitle("Aggiungi")
        options.setNotSelectedMessage("Per procedere seleziona almeno un immagine")
        options.setMessageViewType(CroPicker.MESSAGE_VIEW_TYPE_SNACKBAR)
        CroPicker.init(activity as AppCompatActivity).withOptions(options).start()
    }


    @NeedsPermission(Manifest.permission.CAMERA)
    fun goToCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // start the image capture Intent
        startActivityForResult(intent, cameraRequestCode)
    }



    inner class UserImageAdapter(imageList : MutableList<String>, dragListener: OnStartDragListener): RecyclerView.Adapter<UserImageAdapter.UserImageViewHolder>(),ItemTouchHelperAdapter{
        var images = imageList
        val dragger = dragListener



        override fun onItemMove(fromPosition: Int, toPosition: Int) : Boolean {
            Collections.swap(images, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }



        override fun onItemDismiss(position: Int) {
        }

        inner class UserImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
            fun bindViews(path : String){
                Picasso.with(activity).load(Uri.fromFile(File(path))).fit().centerCrop().into(itemView.userImageHolder)
            }
        }

        override fun getItemCount(): Int = images.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserImageViewHolder {
            val view = parent.inflate(R.layout.user_image_card, false)
            return UserImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserImageViewHolder, position: Int) {
            holder.bindViews(images[position])


            holder.itemView.setOnTouchListener { view, motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    dragger.onStartDrag(holder)
                }
                false
            }
        }




    }



}
// Required empty public constructor
