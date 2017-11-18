package kot.note.musashi.com.appunti

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import kotlinx.android.synthetic.main.activity_registration.*
import com.google.firebase.auth.FirebaseAuth
import kot.note.musashi.com.appunti.extensions.addFragment
import com.google.android.gms.auth.api.credentials.IdentityProviders
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult


class Registration : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_registration)

        addFragment(registrationContainer.id, RegistrationType())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val reg = supportFragmentManager.fragments[0]
        reg.onActivityResult(requestCode, resultCode, data)
    }


}
