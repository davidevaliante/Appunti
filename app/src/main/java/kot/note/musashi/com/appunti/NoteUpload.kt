package kot.note.musashi.com.appunti

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kot.note.musashi.com.appunti.extensions.addFragment
import kot.note.musashi.com.appunti.upload.UploadDescription
import kot.note.musashi.com.appunti.upload.UploadPictures
import kotlinx.android.synthetic.main.activity_note_upload.*

class NoteUpload : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_upload)

        addFragment(uploadFragmentContainer.id, UploadPictures())


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //TODO verificare che manda effettivamente all'activityResult del Fragment
        val frag = supportFragmentManager.fragments[0]
        frag.onActivityResult(requestCode, resultCode, data)
    }
}
