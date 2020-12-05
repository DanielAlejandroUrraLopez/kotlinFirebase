package com.example.firebasetotorial


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100
    private val callbackManage = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integracion de Firebase completa")
        analytics.logEvent("InitScreen",bundle)

        // seutup
        notification()
        setup()
        session()
    }

    override fun onStart() {
        super.onStart()
        authLayout.visibility = View.VISIBLE
    }

    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        var email: String? = prefs.getString("email",null)
        var provider: String? = prefs.getString("provider",null)

        if(email !=  null && provider != null){
            authLayout.visibility = View.INVISIBLE
            showHome(email,ProviderType.valueOf(provider))
        }
    }

    private fun notification() {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener{
            it.result?.token?.let {
                println("este es el valor del dispositivo: ${it}")
            }
        }
        // temas (topics)
        /*FirebaseMessaging.getInstance().subscribeToTopic("tutorial")*/
        // recupoerar informacion
        val url = intent.getStringExtra("url")
        url?.let{
            println("este es el valor push ${it}")
        }
    }

    private fun setup() {
        title = "Autenticacion"

        signUpButton.setOnClickListener{
            if (emailEditTextEmail.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailEditTextEmail.text.toString(), passwordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        loginButton.setOnClickListener {
            if (emailEditTextEmail.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(emailEditTextEmail.text.toString(), passwordEditText.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        googleButton.setOnClickListener {
            // configurar
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient  = GoogleSignIn.getClient(this,googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent,GOOGLE_SIGN_IN)
        }

        facebookButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

            LoginManager.getInstance().registerCallback(callbackManage,
                object :FacebookCallback<LoginResult>{
                    override fun onSuccess(result: LoginResult?) {
                        result?.let {
                            val token = it.accessToken

                            val credential = FacebookAuthProvider.getCredential(token.token)

                            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                                if(it.isSuccessful){
                                    showHome(it.result?.user?.email ?: "",ProviderType.FACEBOOK)
                                }else{
                                    showAlert()
                                }
                            }

                        }
                    }

                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException?) {
                        showAlert()
                    }
                }
            )
        }
    }

    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar",null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType){
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email",email)
            putExtra("provider", provider.name)

        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManage.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SIGN_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try{
                val account = task.getResult(ApiException::class.java)

                if(account != null) {
                    val credential = GoogleAuthProvider.getCredential(account.idToken,null)

                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener{
                        if(it.isSuccessful){
                            showHome(account.email ?: "",ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }
                }
            }catch(ex: ApiException){
                showAlert()
            }
        }
    }

}