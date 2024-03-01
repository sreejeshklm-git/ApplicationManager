
package com.example.appblockr.ui.adduser
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.appblockr.AdminActivity
import com.example.appblockr.R
import com.example.appblockr.databinding.ActivityAdduserBinding
import com.example.appblockr.utils.Utils

import com.google.firebase.firestore.FirebaseFirestore
import io.born.applicationmanager.firestore.User


class AdduserActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdduserBinding
    private lateinit var db : FirebaseFirestore

    private val spinnerData : ArrayList<String> = arrayListOf("Please select User Type","User","Admin")
    private var user_type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.title_bar);
        supportActionBar?.elevation = 0F
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#5DB8E1")))
            this.window.statusBarColor = ContextCompat.getColor(this, R.color.appBackground)
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_adduser)

       db = FirebaseFirestore.getInstance()
        val ad: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            spinnerData
        )
        ad.setDropDownViewResource(android.R.layout
            .simple_spinner_dropdown_item)
        binding.spinnerUserType.adapter = ad

        binding.spinnerUserType.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                user_type = spinnerData[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }



      binding.btnAddUser.setOnClickListener{
          Utils.hideKeyboard(this@AdduserActivity)
          if (checkAllFields()) {
              val user = User(
                  email = binding.edtEmail.text.toString(),
                  user_name = binding.edtUserName.text.toString(),
                  password = binding.edtPassword.text.toString(),
                  user_type = user_type,
                  android_id = ""
              )
              user.user_type = user.assignUserType()

              user.email?.let { it1 -> db.collection("add_users").document(it1).set(user) }

              clearEditFields()
              val intent = Intent(applicationContext, AdminActivity::class.java)
              startActivity(intent)
              finish()
          }
      }

    }

    private fun clearEditFields() {
        binding.edtEmail.text?.clear()
        binding.edtPassword.text?.clear()
        binding.edtUserName.text?.clear()
    }

    private fun checkAllFields(): Boolean {
        if (binding.edtUserName.length() == 0) {
            binding.edtUserName.error = "User Name is required"
            return false
        }
        if (binding.edtEmail.length() == 0 || !isValidEmail(binding.edtEmail.text.toString())) {
            binding.edtEmail.error = "Email is required"
            return false
        }
        if (binding.edtPassword.length() == 0 || !isValidPassword(binding.edtPassword.text.toString())) {
            binding.edtPassword.error = "Password is required & minimum six digits"
            return false
        }
        if (user_type.isBlank() || user_type.equals("Pleas select User Type")) {
            Toast.makeText(this,"Pleas select User Type",Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}