package jp.techacademy.makoto.tokunaga.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private boolean mFavoriteFlag;

    private String myKey;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        //+-------------------------------------------------------------------------------------+

        //データベースの取得
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        //Favoriteネストを取得
        final DatabaseReference favRef = databaseReference.child(Const.FavoritePATH);
        //mAuthを利用してuserUidを取得
        FirebaseAuth fba = FirebaseAuth.getInstance();
        final String myUid = fba.getCurrentUser().getUid();

        //FavoriteネストからmAuthのUidを用いて自分のネストを探す
        favRef.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        myKey = dataSnapshot.getKey();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        //自分のネストから現在のQuestionUidを利用して、フラグを受け取り、代入する。
        final String questionUid = mQuestion.getQuestionUid();
        favRef.child(myUid).child(questionUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() == Const.FAVORITE){
                            mFavoriteFlag = true;
                        } else {
                            mFavoriteFlag = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

        //+-------------------------------------------------------------------------------------+

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // --- ここから ---
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                    // --- ここまで ---
                }
            }
        });

        // +----------------------------------------------------------------------------------------+

        final FloatingActionButton favstar = (FloatingActionButton) findViewById(R.id.favstar);
        findViewById(R.id.favstar).setVisibility(View.INVISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            findViewById(R.id.favstar).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.favstar).setVisibility(View.VISIBLE);
        }

        favstar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFavoriteFlag){
                    favstar.setImageResource(R.drawable.outline_star_border_white_24dp);
                    mFavoriteFlag = false;
                    favRef.child(myKey).setValue(questionUid,Const.NonFAVORITE);
                } else {
                    favstar.setImageResource(R.drawable.outline_star_white_24dp);
                    mFavoriteFlag = true;
                    favRef.child(myKey).setValue(questionUid,Const.FAVORITE);
                }
            }
        });

        // +----------------------------------------------------------------------------------------+

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }
}