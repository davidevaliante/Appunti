package kot.note.musashi.com.appunti.upload


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import kot.note.musashi.com.appunti.R
import kot.note.musashi.com.appunti.extensions.*
import kotlinx.android.synthetic.main.fragment_upload_description.*


/**
 * A simple [Fragment] subclass.
 */
class UploadDescription : Fragment() {

    var c : ViewGroup? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflateInContainer(R.layout.fragment_upload_description, container)
        c = container

        // Inflate the layout for this fragment
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nextFromDescription.setOnClickListener { goToNextFrag() }

    }

    fun goToNextFrag() {
        fun checkValues(): Boolean = !noteDescription.text.isEmpty() && !noteName.text.isEmpty() &&
                                        noteDescription.text.length > 8  && noteName.text.length > 8

        if(checkValues()){
            val db = FirebaseFirestore.getInstance()
            val newNote = Notes(name=noteName.text.toString(),
                                description = noteDescription.text.toString(),
                                latestUpdateTimeStamp = System.currentTimeMillis())
            db.collection("Notes").add(newNote).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    activity?.addAndCommitPreference("LAST_DRAFT", task.result.id)
                    val cont = c as ViewGroup
                    if (cont != null) activity?.replaceFrag(cont.id, UploadPictures())
                }else{
                    showError("Upload error")
                }
            }

        }else{
            showError("Fail")
        }

    }
}// Required empty public constructor
