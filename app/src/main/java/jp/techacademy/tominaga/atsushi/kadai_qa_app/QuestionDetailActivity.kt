package jp.techacademy.tominaga.atsushi.kadai_qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mDataBaseReference: DatabaseReference

    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String,String?>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers){
                //同じAnswerUidのものが存在しているときは何もしない
                if(answerUid == answer.answerUid){
                    return
                }
            }

            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map ["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {


        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        //渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras?.get("question") as Question

        title = mQuestion.title



        //ListViewの準備
        mAdapter = QuestionDetailListAdapter(this,mQuestion)
       qdListView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        qdFab.setOnClickListener{
            //ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if(user == null){
                //ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext,LoginActivity::class.java)
                startActivity(intent)
            }else {
                //Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext,AnswerSendActivity::class.java)
                intent.putExtra("question",mQuestion)
                startActivity(intent)
            }
        }



        //ログイン済みのユーザーを取得する (ログインしていないとnull)
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null){
            qdFavoritesButton.visibility = View.GONE
        }else {
            qdFavoritesButton.visibility = View.VISIBLE

            qdFavoritesButton.setOnClickListener {
                if (qdFavoritesButton.text == "off") {
                    qdFavoritesButton.setBackgroundResource(R.drawable.ic_star_yellow_24dp)
                    qdFavoritesButton.text = "on"
                } else if (qdFavoritesButton.text == "on") {
                    qdFavoritesButton.setBackgroundResource(R.drawable.ic_star_black_24dp)
                    qdFavoritesButton.text = "off"
                }

                mDataBaseReference = FirebaseDatabase.getInstance().reference
                val userRef = mDataBaseReference.child(UsersPATH).child(user.uid)

                val qdFavoritesButtonText = HashMap<String, String>()

                qdFavoritesButtonText["favorits"] = qdFavoritesButton.text.toString()
                userRef.setPriority(qdFavoritesButtonText)




            }
        }

        val answerDataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = answerDataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }
}
