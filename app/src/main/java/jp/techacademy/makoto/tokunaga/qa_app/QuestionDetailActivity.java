package jp.techacademy.makoto.tokunaga.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    private boolean mFavoriteFlag = false;

    //+------------------------------------------------------------------------------------------+
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mFavRef = databaseReference.child(Const.FavoritePATH);
    FirebaseAuth fba = FirebaseAuth.getInstance();
    private String myUid = fba.getCurrentUser().getUid();

    private FloatingActionButton mFavstar;
    //+------------------------------------------------------------------------------------------+

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
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { }
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) { }
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }
        @Override
        public void onCancelled(DatabaseError databaseError) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        mFavstar = (FloatingActionButton)findViewById(R.id.favstar);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");
        //+-------------------------------------------------------------------------------------+

        //FavoriteネストからmAuthのUidを用いて自分のネストを探す
        mFavRef.orderByChild("userUid")
                .equalTo(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String key = (String) childSnapshot.getKey();

                            DatabaseReference flagRef = mFavRef.child(key).child(mQuestion.getQuestionUid());

                            Log.d("metaandroid", String.valueOf(flagRef));

                            Log.d("metaandroid", mQuestion.getQuestionUid());

                            flagRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("metaandroid", String.valueOf(dataSnapshot));
                                    if (dataSnapshot.getValue().equals(Const.FAVORITE)) {
                                        mFavoriteFlag = true;
                                        mFavstar.setImageResource(R.drawable.outline_star_white_24dp);
                                    } else {
                                        mFavoriteFlag = false;
                                        mFavstar.setImageResource(R.drawable.outline_star_border_white_24dp);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
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

        final FloatingActionButton mFavstar = (FloatingActionButton)findViewById(R.id.favstar);
        findViewById(R.id.favstar).setVisibility(View.INVISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            findViewById(R.id.favstar).setVisibility(View.INVISIBLE);
        } else {
            findViewById(R.id.favstar).setVisibility(View.VISIBLE);
        }

        mFavstar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFavoriteFlag){
                    mFavstar.setImageResource(R.drawable.outline_star_border_white_24dp);
                    mFavoriteFlag = false;
                    mFavRef.orderByChild("userUid")
                            .equalTo(myUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                                        String key = (String) childSnapshot.getKey();
                                        mFavRef.child(key).child(mQuestion.getQuestionUid()).setValue(Const.NonFAVORITE);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });

                } else {
                   mFavstar.setImageResource(R.drawable.outline_star_white_24dp);
                    mFavoriteFlag = true;
                    mFavRef.orderByChild("userUid")
                            .equalTo(myUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()){
                                        String key = (String) childSnapshot.getKey();
                                        mFavRef.child(key).child(mQuestion.getQuestionUid()).setValue(Const.FAVORITE);
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                }
            }
        });

        // +----------------------------------------------------------------------------------------+

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);

    }

}