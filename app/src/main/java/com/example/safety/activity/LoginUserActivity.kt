package com.example.safety.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.example.safety.api.RetrofitInstance
import com.example.safety.common.Constants
import com.example.safety.common.SharedPrefFile
import com.example.safety.databinding.ActivityLoginUserBinding
import com.example.safety.models.LoginInfoModel
import com.example.safety.models.VerifiedUserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginUserActivity : AppCompatActivity() {

    private val TAG = "LoginUserActivity"

    private lateinit var binding: ActivityLoginUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val retrofit = RetrofitInstance.initialize()
        val sharedpref = SharedPrefFile
        sharedpref.init(this)
        binding = ActivityLoginUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }
        }
        this.onBackPressedDispatcher.addCallback(this, callback)

        binding.tVNewUserRegister.setOnClickListener {
            val intent = Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.tilemailLogin.editText?.text.toString()
            val password = binding.tilpasswordLogin.editText?.text.toString()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Enter email and password",Toast.LENGTH_SHORT).show()
            }else{
                retrofit.verifyUser(LoginInfoModel(email,password))
                    .enqueue(object :Callback<VerifiedUserModel>{
                        override fun onResponse(
                            call: Call<VerifiedUserModel>,
                            response: Response<VerifiedUserModel>,
                        ) {
                            Log.d("Rupam $TAG", "Response: ${response.body()?.userData}")
                            when(response.body()?.status){
                                "verified"-> {
                                sharedpref.putLoggedInfo(Constants.SP_LOGGED_INFO, true)
                                sharedpref.putUserData(
                                    Constants.SP_USERDATA,
                                    response.body()!!.userData
                                )
                                binding.progressBar.visibility = View.VISIBLE
                                binding.mainContent.visibility = View.GONE

                                binding.progressBar.postDelayed({
                                    binding.progressBar.visibility = View.GONE
                                    binding.mainContent.visibility = View.VISIBLE
                                }, 1500)
                                startActivity(Intent(baseContext, MainActivity::class.java))
                            }
                                "password_wrong"->{
                                    Toast.makeText(baseContext,"Wrong Password",Toast.LENGTH_SHORT).show()
                                }

                                "user_not_found"->{
                                    Toast.makeText(baseContext,"No User Found, Recheck Email or Register as New User",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<VerifiedUserModel>, t: Throwable) {
                            Log.e("Rupam $TAG", "Response: ${t.message}")
                        }
                    })

            }

        }
    }

}