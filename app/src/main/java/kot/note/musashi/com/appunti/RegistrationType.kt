package kot.note.musashi.com.appunti


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kot.note.musashi.com.appunti.extensions.*
import kotlinx.android.synthetic.main.fragment_registration_type.*
import org.json.JSONException

import cn.pedant.SweetAlert.SweetAlertDialog



class RegistrationType : Fragment() {

    private var mCallbackManager : CallbackManager?=null
    var auth : FirebaseAuth?=null
    var isRegistering = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //exntension function dei Fragment
        val root = inflateInContainer(R.layout.fragment_registration_type, container)
        auth = FirebaseAuth.getInstance()
        mCallbackManager = CallbackManager.Factory.create() //gestisce la risposta di Facebook





        return root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // registrazione con facebook
        facebookRegistration.setOnClickListener {
            if(!isRegistering) {
                isRegistering = true //evita spam


                // scambia il token di accesso dato da Facebook con uno di Firebase e scrive utente nel database
                fun writeUserFromFacebook(facebookToken: AccessToken) {
                    // roba per la maschera di dialogo
                    var waitDialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
                    waitDialog.progressHelper.barColor = Color.parseColor("#26A69A")
                    waitDialog.titleText = "Attendere"
                    waitDialog.contentText = "Stiamo effettuando la registrazione con Facebook"
                    waitDialog.setCancelable(false)
                    waitDialog.show()


                    /* funzione interna, tramite il token di accesso di Facebook effettua
                    *  la richiesta all'API di Facebook per ottenere dati.
                    *  Restituisce un User (quello definito da noi) con i dati presi da Facebook
                    */

                    val facebookUser = User() //oggetto vuoto

                    //parametri della richiesta
                    val parameters = Bundle()
                    parameters.putString("fields", "id,first_name,last_name,gender,link")

                    // cosa devo fare quando facebook risponde alla richiesta ?
                    val facebookResponse = GraphRequest.GraphJSONObjectCallback { `object`, response ->
                        try {
                            val name = response.jsonObject.getString("first_name")
                            facebookUser.name = name
                            val surname = response.jsonObject.getString("last_name")
                            facebookUser.surname = surname
                            val gender = response.jsonObject.getString("gender")
                            facebookUser.gender = gender

                            //profilo dal quale fetchare altri dati
                            val profile = Profile.getCurrentProfile()
                            val linkUri = profile.linkUri
                            if (linkUri != null) facebookUser.facebookPageLink = linkUri.toString()
                            val profilePicture = profile.getProfilePictureUri(200, 200) //immagine 200x200
                            if (profilePicture != null) facebookUser.imageLink = profilePicture.toString()

                            /* a questo punto abbiamo tutti i dati che ci servono e possiamo passare scambiare
                           il token con quello di Firebase
                        */
                            val facebookCredential: AuthCredential = FacebookAuthProvider.getCredential(facebookToken.token)
                            auth?.signInWithCredential(facebookCredential)?.addOnCompleteListener {
                                if (it.isSuccessful) {
                                    //utente loggato, possiamo scrivere nel db
                                    val db = FirebaseFirestore.getInstance()

                                    //id utente da utilizzare come nome del documento
                                    val userId = it.result.user.uid
                                    db.collection("Users").document(userId).set(facebookUser).addOnCompleteListener {
                                        //toglie il dialogo di attesa
                                        waitDialog.dismiss()

                                        //se tutto è andato bene mostriamo messaggio di successo e mandiamo il tizio da qualche parte
                                        activity?.showSuccess("Registrazione effettuata")
                                        isRegistering = false
                                    }
                                } else {
                                    //qualcosa è andato storto
                                    activity?.showError("Registrazione fallita")
                                    isRegistering = false
                                }
                            }
                        } catch (exception: JSONException) {
                            activity?.showError("Errore in fase di registrazione, riprovare")
                            isRegistering = false
                            // se c'è stato un errore prova a sloggare per poter riprovare
                            LoginManager.getInstance().logOut()
                        }

                    }

                    // imposta la richiesta (e cosa fare in caso di risposta) rispetto all'access Token
                    val request = GraphRequest.newMeRequest(facebookToken, facebookResponse)
                    request.parameters = parameters // setta i parametri della richiesta
                    request.executeAsync()          // fa la richiesta asincronamente

                }
                // registerCallback prende il callbackManager ed il risultato del login su Facebook
                LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        if (result != null) {
                            // scambia il token di accesso dato da Facebook con uno di Firebase
                            writeUserFromFacebook(result.accessToken)
                        } else {
                            activity?.showError("Login con Facebook fallito")
                        }
                    }

                    override fun onCancel() {
                    }

                    override fun onError(error: FacebookException?) {
                        activity?.showError("Login con Facebook fallito")
                    }

                })
                // permessi a richiedere a Facebook
                LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email", "public_profile"))
            }
        }

        googleRegistration.setOnClickListener {

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)

    }


}// Required empty public constructor
