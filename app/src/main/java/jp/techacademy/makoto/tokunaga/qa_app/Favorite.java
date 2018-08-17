package jp.techacademy.makoto.tokunaga.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

public class Favorite implements Serializable{
    private ArrayList<Question> mArrayListFavorite;
    private ArrayList<Question> mArrayListNonFavorite;

    private String mFavoriteUid;
    private String mUid;

    public void setFavoriteUid(String favorite){
        this.mFavoriteUid = favorite;
    }

    public String getmFavoriteUid() {
        return mFavoriteUid;
    }

    public String setmUid(String uid) {
        return this.mUid = uid;
    }

    public String getmUid() {
        return mUid;
    }
}