package kot.note.musashi.com.appunti


import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import bolts.Task
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

import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.firebase.auth.GoogleAuthProvider


class RegistrationType : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    override fun onConnected(p0: Bundle?) {
        //funzione dello smartlock, da implementare dopo
        //smartLogLoginAttempt()
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    private var mCallbackManager : CallbackManager?=null
    private val MAIL_SIGN_IN = 99
    private val RC_READ = 123
    private val RC_HINT = 2
    private val RC_SAVE_CREDENTIALS = 11
    private val GOOGLE_SIGN_IN_CODE = 22
    var auth : FirebaseAuth?=null
    private var isRegistering = false
    private var mCredentialsApiClient : GoogleApiClient?=null
    private var mCredentialRequest : CredentialRequest?= null
    private var mGoogleSignInClient : GoogleSignInClient?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //exntension function dei Fragment
        val root = inflateInContainer(R.layout.fragment_registration_type, container)
        auth = FirebaseAuth.getInstance()
        mCallbackManager = CallbackManager.Factory.create() //gestisce la risposta di Facebook

        // builder GoogleApiClient
        mCredentialsApiClient = GoogleApiClient.Builder(this!!.activity!!)
                .addConnectionCallbacks(this)
                .enableAutoManage(this!!.activity!!, this)
                .addApi(Auth.CREDENTIALS_API)
                .build()

        // richiesta da effettuare
        mCredentialRequest = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .setAccountTypes(IdentityProviders.GOOGLE, IdentityProviders.FACEBOOK)
                .build()

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(resources.getString(R.string.web_client_id))
                .requestEmail()
                //.requestProfile()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this!!.activity!!, googleSignInOptions)

        return root
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // registrazione con facebook
        facebookRegistration.setOnClickListener {
            if(!isRegistering) {
                isRegistering = true //evita spam

                // da reimplementare
                fun saveFacebookCredentialInSmartLock(name : String, surname : String, profilePicture : Uri){
                    val credentialToSave = Credential.Builder("Facebook Account")
                            .setAccountType(IdentityProviders.FACEBOOK)
                            .setName("$name $surname")
                            .setProfilePictureUri(profilePicture)
                            .build()
                    Auth.CredentialsApi.save(mCredentialsApiClient, credentialToSave).setResultCallback {
                        val status = it.status
                        if (status.isSuccess){
                            activity?.showInfo("Credenziali di accesso salvate")
                        }else{
                            if (status.hasResolution()){
                                try {
                                    status.startResolutionForResult(activity, RC_SAVE_CREDENTIALS)
                                } catch (e :IntentSender.SendIntentException) {
                                    // Could not resolve the request
                                    activity?.showError("Salvataggio fallito")
                                }
                            }
                        }
                    }
                }

                // scambia il token di accesso dato da Facebook con uno di Firebase e scrive utente nel database
                fun writeUserFromFacebook(facebookToken: AccessToken) {
                    // roba per la maschera di dialogo



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
                            facebookUser.provider = "FACEBOOK"

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
                                        //saveFacebookCredentialInSmartLock(name, surname, profilePicture)

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
                        } else { showError("Login con Facebook fallito") }
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
            if(!isRegistering) {
                isRegistering = true
                val signInIntent = mGoogleSignInClient?.signInIntent
                activity?.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_CODE)
            }
        }

        mailRegistration.setOnClickListener {
            if(!isRegistering) {
                isRegistering = true
                startActivityForResult(
                        AuthUI.getInstance().createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true, true).build(), MAIL_SIGN_IN)
            }
        }

    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager?.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            RC_READ -> {
                if (resultCode == Activity.RESULT_OK) {
                    val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    activity?.showError("${credential.toString()}")
                } else {
                    Log.e("CREDENTIAL", "Credential Read: NOT OK")

                }
            }

            RC_HINT ->{
                if(resultCode == Activity.RESULT_OK){
                    val credential = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    Log.d("CHOICE", "credentials : ACCOUNT_TYPE : ${credential?.accountType}" +
                            "Name : ${credential?.name}, Pic : ${credential?.profilePictureUri} " +
                            "GENRATED PASS : ${credential?.generatedPassword} "+
                            "GIVEN_NAME$ :{credential?.givenName} "+
                            "FAMILY NAME :${credential?.familyName} "+
                            "Password : ${credential?.password} "+
                            "ID TOKENS : ${credential?.idTokens}"
                             )
                }else{
                }
            }

            RC_SAVE_CREDENTIALS -> {
                if(resultCode == Activity.RESULT_OK) activity?.showInfo("Credenziali Salvate !")
            }

            GOOGLE_SIGN_IN_CODE -> {
                if(data != null) {
                    Log.d("DATA","${data.extras}")
                    val loginTask = GoogleSignIn.getSignedInAccountFromIntent(data)

                    fun saveGoogleCredentialInSmartLock(googleCredential: GoogleSignInAccount){
                        val credential = Credential.Builder(googleCredential.email)
                                .setAccountType(IdentityProviders.GOOGLE)
                                .setName(googleCredential.displayName)
                                .setProfilePictureUri(googleCredential.photoUrl)
                                .build()

                        Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback {
                            result ->  val status = result.status
                            if (status.isSuccess) showSuccess("Credenziali salvate con successo")
                            else{
                                if(status.hasResolution()){
                                    try{
                                        status.startResolutionForResult(activity, RC_SAVE_CREDENTIALS)
                                    }catch (e : IntentSender.SendIntentException){
                                        showError("Salvataggio credenziali non riuscito")
                                    }
                                }
                            }
                        }

                    }

                    fun writeGoogleUserInFirebase(account: GoogleSignInAccount, id : String){
                        val name = account.displayName
                        val surname = account.familyName
                        val mail = account.email
                        val image = account.photoUrl
                        val provider = "GOOGLE"

                        val newUser = User(name=name, surname = surname,imageLink = image.toString(),
                                provider=provider,
                                mail = mail)

                        val db = FirebaseFirestore.getInstance()
                        db.collection("Users").document(id).set(newUser).addOnCompleteListener {
                            isRegistering = false
                        }
                    }

                    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
                        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                        if (auth != null) {
                            auth!!.signInWithCredential(credential).addOnCompleteListener {
                                if (it.isSuccessful)
                                    //saveGoogleCredentialInSmartLock(account)
                                    showInfo("logged with google")
                                    writeGoogleUserInFirebase(account, it.result.user.uid)

                            }
                        } else {
                            isRegistering = false
                            showError("auth is null") }
                    }

                    val account = loginTask.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account)
                }else{
                    isRegistering = false
                    activity?.showError("Registrazione fallita")
                }

            }

            MAIL_SIGN_IN -> {
                if(resultCode == Activity.RESULT_OK){
                    val id = FirebaseAuth.getInstance().currentUser?.uid

                    if(id != null){
                        val db = FirebaseFirestore.getInstance()

                        db.collection("Users").document(id).get().addOnCompleteListener {
                            if(!it.result.exists()) {
                                val idp = IdpResponse.fromResultIntent(data)
                                val newUser = User(
                                        mail = idp?.email,
                                        provider = "MAIL")

                                db.collection("Users").document(id).set(newUser).addOnCompleteListener {
                                    isRegistering = false

                                }
                            }

                        }
                        isRegistering = false

                    }

                }

            }
        }
    }


    // Prova prima di tutto ad effettuare un login automatico oppure a recuperare dati di login esistenti
    // Questo suppress è necessario perchè c'è un bug nell'utilizzo di startIntentSenderForResult nei Fragment
    @SuppressLint("RestrictedApi")
    private fun smartLogLoginAttempt(){

        //noinspection RestrictedApi
        fun showAvailableChoices(){
            val availableChoicesRequest = HintRequest.Builder()
                    .setHintPickerConfig(CredentialPickerConfig.Builder().setShowCancelButton(true).build())
                    .setEmailAddressIdentifierSupported(true)
                    .setAccountTypes(IdentityProviders.FACEBOOK, IdentityProviders.GOOGLE)
                    .build()

            val pendingIntent : PendingIntent = Auth.CredentialsApi.getHintPickerIntent(mCredentialsApiClient, availableChoicesRequest)

            activity?.startIntentSenderForResult(pendingIntent.intentSender, RC_HINT, null, 0, 0, 0, null)
        }

        fun handleMultipleChoice(status : Status){
            if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                try {
                    status.startResolutionForResult(activity, RC_READ)
                } catch (e : IntentSender.SendIntentException) {
                    Log.e("STATUS", "STATUS: Failed to send resolution.", e)
                }
            } else {
                // The user must create an account or sign in manually.
                Log.e("STATUS", "STATUS: Unsuccessful credential request.");
            }
        }

        fun handleSuccessfulResult(credential: Credential){
            // che tipo di account abbiamo ?
            val accountType = credential.accountType
            Log.d("ACCOUNT_TYPE", "$accountType ${credential.name}, ${credential.familyName}, " +
                    "${credential.generatedPassword}, ${credential.givenName}, ${credential.profilePictureUri}")
            //TODO switch in base alle credenziali
        }

        Auth.CredentialsApi.request(mCredentialsApiClient, mCredentialRequest).setResultCallback{
            credentialRequestResult ->
            val requestStatus = credentialRequestResult.status

            when {
                // solo un utente riconosciuto in smartlock
                requestStatus.isSuccess -> handleSuccessfulResult(credentialRequestResult.credential)

                //nessuna credenziale trovata, bisogna proporre una scelta
                requestStatus.statusCode == CommonStatusCodes.SIGN_IN_REQUIRED -> showAvailableChoices()

                //molto probabilmente sono presenti più scelte per lo stesso account e si deve scegliere
                else ->  handleMultipleChoice(credentialRequestResult.status)
            }
        }
    }



}// Required empty public constructor
