package it.mirea.fieldofmiracleswithyakudorovich.Util;

import com.google.firebase.auth.FirebaseUser;

public class Singletone {
    private static final Singletone INSTANCE = new Singletone();


    public Singletone() {
        nickname = "";
        email = "";
        password = "";
        user = null;
    }
    public String nickname;
    public String email;
    public String password;
    public FirebaseUser user;
    public String numberOfTasks;
    public String task;
    public String answer;
    public String nameOfGame;

    public static Singletone getInstance() {
        return INSTANCE;
    }
}
