package jp.techacademy.tominaga.atsushi.kadai_qa_app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.inputmethod.InputMethodManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.activity_setting.settingNameText

class SettingActivity : AppCompatActivity() {

    private lateinit var mDataBaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        //preferenceから表示名を取得してEditTextに反映させる
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val name = sp.getString(NameKEY,"")
        settingNameText.setText(name)


        mDataBaseReference = FirebaseDatabase.getInstance().reference

        //UIの初期設定
        title = "設定"

        settingChangeButton.setOnClickListener {v ->
            // キーボードが出ていたら閉じる
            val im = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            im.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null){
                //ログインしていない場合は何もしない
                Snackbar.make(v,"ログインしていません",Snackbar.LENGTH_LONG).show()
            }else{
                //変更した表示名をFirebaseに保存する
                val name = settingNameText.text.toString()
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)
                val data = HashMap<String, String>()
                data["name"] = name
                userRef.setValue(data)
            }

            //変更した表示名をPreferenceに保存する
            val sp = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val editor = sp.edit()
            editor.putString(NameKEY,name)
            editor.commit()

            Snackbar.make(v,"表示名を変更しました",Snackbar.LENGTH_LONG).show()
        }

        settingLogoutButton.setOnClickListener { v ->
            FirebaseAuth.getInstance().signOut()
            settingNameText.setText("")
            Snackbar.make(v,"ログアウトしました",Snackbar.LENGTH_LONG).show()
        }
    }
}
