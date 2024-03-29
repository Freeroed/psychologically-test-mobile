package com.example.cursach

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.cursach.rest.ApiClient
import com.example.cursach.rest.response.AccountDto
import com.example.cursach.utils.SessionManager
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_personal.*
import kotlinx.android.synthetic.main.activity_personal.goBack
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Personal : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var userId = 0
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        lateinit var apiClient: ApiClient
        sessionManager = SessionManager(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)

        apiClient = ApiClient()
        val context = this
        apiClient.getApiService(this).getAccount()
            .enqueue(object : Callback<AccountDto> {
                override fun onFailure(call: Call<AccountDto>, t: Throwable) {
                    Toast.makeText( context,"FAIL", Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<AccountDto>, response: Response<AccountDto>) {
                    val userInfo = response.body()
                    userEmail.setText(userInfo?.email)
                    userName.setText(userInfo?.firstName)
                    userBirtday.setText(userInfo?.birthDate)
                    userGender.setText(userInfo?.gender)
                    if (userInfo?.gender == "MALE") {
                        userGender.setText("Мужской")
                    } else {
                        userGender.setText("Женский")
                    }

                    sessionManager.saveUserRoles(userInfo?.authorities)
                    if (userInfo?.authorities?.indexOf("ROLE_ADMIN") !== -1) {
                        isAdmin = true
                    }
                    println(sessionManager.getRoles())
                    userId = userInfo?.id!!
                }
            })

        // выход из личного кабинета
        logout.setOnClickListener {
            val context = this
            val quitDialog = AlertDialog.Builder(this)
            quitDialog.setTitle("Вы уверены, что хотите выйти из личного кабинета?")
            quitDialog.setPositiveButton("Да", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    sessionManager.cleanAuthToken()
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                }
            })
            quitDialog.setNegativeButton("Нет", object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    dialog?.cancel()  //закрыть диалоговое окно
                }
            })
            quitDialog.show()
        }

        // клик по кнопке "назад"
        goBack.setOnClickListener {
            onBackPressed()
        }

        // клик по кнопке "статистика"
        getStat.setOnClickListener {
            val intent = Intent(this, StatActivity::class.java)
            intent.putExtra("userId", userId.toString())
            intent.putExtra("isAdmin", isAdmin)
            startActivity(intent)
        }

        // клик по кнопке "редактировать"
        editUserData.setOnClickListener {
            val intent = Intent(this, EditUserActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}