package com.example.firebasetotorial


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Analytics Event
        val analytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message","Integracion de Firebase completa")
        analytics.logEvent("InitScreen",bundle)

        // seutup
        setup()
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

}