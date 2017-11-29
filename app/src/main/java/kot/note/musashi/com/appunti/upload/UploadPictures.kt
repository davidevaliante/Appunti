package kot.note.musashi.com.appunti.upload


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import kotlinx.android.synthetic.main.fragment_upload_pictures.*
import permissions.dispatcher.RuntimePermissions
import android.provider.MediaStore
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import kot.note.musashi.com.appunti.R
import permissions.dispatcher.NeedsPermission
import xyz.dev_juyoung.cropicker.CroPicker
import android.util.Log
import android.view.*
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kot.note.musashi.com.appunti.NoteUpload
import kot.note.musashi.com.appunti.extensions.*
import kotlinx.android.synthetic.main.activity_note_upload.*
import kot.note.musashi.com.appunti.compressorone.Compressor as Compressorone
import kotlinx.android.synthetic.main.user_image_card.view.*
import xyz.dev_juyoung.cropicker.models.Media
import java.io.File
import java.util.*


@RuntimePermissions
class UploadPictures : Fragment(), OnStartDragListener {


    private val cameraRequestCode = 0
    private var mItemTouchHelper : ItemTouchHelper?= null
    private var isInEditMode = false

    // callback per il recyclerView delle immagini per il drag e drop (interfacce in ./extensions/Adapters)
    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper?.startDrag(viewHolder)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val root = inflateInContainer(R.layout.fragment_upload_pictures, container)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crashButton.setOnClickListener { startUpload() }

        // switch edit mode per la posizione delle immagini
        moveImageSwitch.setOnClickListener {
            isInEditMode = !isInEditMode
            when(isInEditMode){
                true -> {
                    editTitle.setGone()
                    editHint.setVisible() }
                false -> {
                    editHint.setGone()
                    editTitle.setVisible() }
            }
        }


        // camera.setOnClickListener { goToCameraWithPermissionCheck() }
        gallery.setOnClickListener { goToGalleryWithPermissionCheck() }


        imageRecyclerView.layoutManager = GridLayoutManager(activity as AppCompatActivity,3)
        imageRecyclerView.setHasFixedSize(true)
    }

    // accede alla galleria e popola il recyclerView con le immagini scelte
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun goToGallery(){
        val options = CroPicker.Options()
        options.setToolbarTitle("Aggiungi")
        options.setNotSelectedMessage("Per procedere seleziona almeno un immagine")
        options.setMessageViewType(CroPicker.MESSAGE_VIEW_TYPE_SNACKBAR)
        CroPicker.init(activity as AppCompatActivity).withOptions(options).start()
    }

    //accede alla camera e restituisce una singola foto
    @NeedsPermission(Manifest.permission.CAMERA)
    fun goToCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // start the image capture Intent
        startActivityForResult(intent, cameraRequestCode)
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
                    // rende gli elementi necessari visibili
                    editImageGroup.setVisible()
                    imageRecyclerView.setVisible()

                    // roba del recyclerView
                    val results = data?.getParcelableArrayListExtra<Media>(CroPicker.EXTRA_RESULT_IMAGES) as ArrayList<Media>
                    val imagePathList = results.map { it.imagePath }
                    val imageAdapter = UserImageAdapter(emptyList(), this)
                    imageAdapter.updateList(imagePathList)
                    imageAdapter.fallbackList = imagePathList
                    val touchCallback = SimpleItemTouchHelperCallback(imageAdapter)
                    mItemTouchHelper = ItemTouchHelper(touchCallback)
                    mItemTouchHelper?.attachToRecyclerView(imageRecyclerView)
                    imageRecyclerView.adapter = imageAdapter
                }else{
                    showError("Nessun file selezionato")
                }
            }

        }



    }

    override fun onStop() {
        super.onStop()
        imageRecyclerView.adapter = null
        mItemTouchHelper?.attachToRecyclerView(null)
    }

    inner class UserImageAdapter(imageList : List<String>, dragListener: OnStartDragListener): RecyclerView.Adapter<UserImageAdapter.UserImageViewHolder>(), ItemTouchHelperAdapter{
        var fallbackList = emptyList<String>()
        var images = imageList
        val dragger = dragListener

        override fun onItemMove(fromPosition: Int, toPosition: Int) : Boolean {
            Collections.swap(images, fromPosition, toPosition)
            notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onItemDismiss(position: Int) {
            // nulla ??
        }

        inner class UserImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelpedViewHolder{
            override fun onItemSelected() {
                itemView.moveToggle.setVisible()
            }

            override fun onItemClear() {
                itemView.moveToggle.setGone()
                for (i in 0 until imageRecyclerView.adapter.itemCount)
                    imageRecyclerView.layoutManager.findViewByPosition(i).findViewById<TextView>(R.id.pageNumber).text = (i+1).toString()
            }

            fun bindViews(path : String){
                itemView.moveToggle.setGone()
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
            holder.itemView.pageNumber.text = (position+1).toString()
            holder.itemView.setOnTouchListener { view, motionEvent ->
                Log.d("MOTION", motionEvent.action.toString())
                if(motionEvent.action == MotionEvent.ACTION_DOWN && isInEditMode) {
                    dragger.onStartDrag(holder)
                }
                false
            }
        }

        fun updateList(newList : List<String>){
            images =  newList
            notifyDataSetChanged()
        }



    }

    fun startUpload(){
        val storage = FirebaseStorage.getInstance().reference



        var lista = (imageRecyclerView.adapter as UserImageAdapter).images


        val compressdList = mutableListOf<File>()

        lista.map { File(it)  }.forEach {
            var i : File? = null

            val first = object : Consumer<File>{
                override fun accept(t: File?) {
                    i = t
                    if(t != null ){
                        Log.d("RTRTRTRTRT","working")
                        compressdList.add(t)
                    }
                    else Log.d("COMPRESSION_ERROR","Compressorone failed!")
                }

            }
            val second = object : Consumer<Throwable>{
                override fun accept(t: Throwable?) {
                    t?.printStackTrace()
                    showError("${t?.message}")
                }


            }

            Compressorone(activity)
                    .setQuality(50)
                    .setMaxWidth(1440f)
                    .setMaxHeight(1080f)
                    .compressToFileAsFlowable(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( first, second )


        }


        Handler().postDelayed({
            compressdList.forEach { Log.d("ELEMENT","Position : $it") }
            Log.d("UPLOAD_TASK", "upload started at ${System.currentTimeMillis()}")
            compressdList.forEach {
                storage.child(Uri.fromFile(it).lastPathSegment).putFile(Uri.fromFile(it)).addOnCompleteListener{
                    task ->
                        if(compressdList.indexOf(it) == compressdList.size-1){
                            Log.d("UPLOAD_TASK", "upload ended at ${System.currentTimeMillis()}")
                        }else{
                            Log.d("UPLOAD_TASK", "upload numero ${compressdList.indexOf(it)} ended at ${System.currentTimeMillis()}")
                        }
                }.addOnProgressListener {
                taskSnapshot ->
                val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                println("Upload is $progress% done")

                }


            }
            showInfo("done!")

        }, 10000)
        // compressdList.forEach { Log.d("ELEMENT","Position : $it") }
    }


}
// Required empty public constructor
