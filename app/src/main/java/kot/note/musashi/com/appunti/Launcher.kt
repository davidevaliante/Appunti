package kot.note.musashi.com.appunti

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kot.note.musashi.com.appunti.extensions.goToPage
import kot.note.musashi.com.appunti.extensions.showMessage
import kotlinx.android.synthetic.main.activity_launcher.*

class Launcher : AppCompatActivity() {


    var auth : FirebaseAuth? = null
    var mCallbackManager : CallbackManager?=null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        auth = FirebaseAuth.getInstance()
        mCallbackManager = CallbackManager.Factory.create()


        if (auth?.currentUser == null) goToPage<Registration>() else goToPage<Login>()

        reg.setOnClickListener { goToPage<Registration>() }

        log.setOnClickListener { goToPage<Login>() }

        logout.setOnClickListener { AuthUI.getInstance().signOut(this)
        LoginManager.getInstance().logOut()}


    }

}
