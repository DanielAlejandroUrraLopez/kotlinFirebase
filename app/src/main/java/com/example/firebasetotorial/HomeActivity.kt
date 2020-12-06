package com.example.firebasetotorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_home.*

enum class ProviderType {
    BASIC,
    GOOGLE,
    FACEBOOK
}
class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setup

        var bundle = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")

        setup(email ?: "", provider ?: "")

        // guardado de datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email",email)
        prefs.putString("provider",provider)
        prefs.apply()

    }

    private fun setup(email: String, provider: String) {

        title = "Inicio"

        emailTextView.text  = email
        providerTextView.text = provider

        logOutButton.setOnClickListener {
            // guardado de datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            if(provider == ProviderType.FACEBOOK.name) {
                LoginManager.getInstance().logOut()
            }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        saveButton.setOnClickListener{
            db.collection("users").document(email).set(
                hashMapOf("provider" to provider, "addres" to addressTextView.text.toString(), "phone" to phoneTextView.text.toString())
            )
        }

        getButton.setOnClickListener{
            db.collection("users").document(email).get().addOnSuccessListener {
                addressTextView.setText(it.get("addres") as String?)
                phoneTextView.setText(it.get("phone") as String?)
            }
        }

        deleteButton.setOnClickListener {
            db.collection("users").document(email).delete()
        }

    }
}