package kot.note.musashi.com.appunti

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.auth.FirebaseAuth
import kot.note.musashi.com.appunti.extensions.addFragment


class Registration : AppCompatActivity() {


    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registration)
        auth = FirebaseAuth.getInstance()

        addFragment(registrationContainer.id, RegistrationType())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val reg = supportFragmentManager.fragments.get(0)
        reg.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStart() {
        super.onStart()
        val authStateListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(status: FirebaseAuth) {
                if (status.currentUser != null) {
                    val id = status.currentUser?.uid



                }
            }
        }
        auth?.addAuthStateListener(authStateListener)

    }
}
