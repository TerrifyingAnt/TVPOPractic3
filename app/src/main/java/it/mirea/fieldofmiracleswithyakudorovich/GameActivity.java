package it.mirea.fieldofmiracleswithyakudorovich;

import static android.content.ContentValues.TAG;
import static java.lang.Math.abs;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import it.mirea.fieldofmiracleswithyakudorovich.Util.Singletone;

public class GameActivity extends AppCompatActivity {
    public static final Random sRandom = new Random();
    private ImageView mBottleImageView;
    private float lastAngle = -1;
    private List<Float> angles = new ArrayList<Float>();
    private int[] scores = new int[] {0, 300, -1000, 50, 1, 250, 350, 500, 2, 150, 3, 4, 400, 450, 100, 200};
    String nameOfGame = new Singletone().getInstance().nameOfGame;
    List<String> players = new ArrayList<String>();
    int numberOfPlayers;
    String temp, answer, hiddenAnswer, openedLetters, badLetters; // openedLetters + badLetters == все ебаные буквы
    int intAngle = 0;
    int myScores = 0;
    TextView taskView, answView;
    int f = 0;
    int k = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hiddenAnswer = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            answer = bundle.getString("answer");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        FirebaseDatabase db = FirebaseDatabase.getInstance();

        for(int i = 0; i < 16; i++){
            angles.add((float) (i * 22.5));
        }

        getPlayers();
        getTemp();

        taskView = findViewById(R.id.taskView);
        taskView.setText(new Singletone().getInstance().task);


        answView = findViewById(R.id.answView);

        if(k > 0) {
            listen();
        }
        listenHiddenAnswer();
        k+=1;
        DatabaseReference nPlayers = db.getReference().child("game").child(nameOfGame).child("numbOfPlayers");
        nPlayers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfPlayers = Integer.valueOf(String.valueOf(snapshot.getValue()));
                DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("players");
                for(int i = 0; i < numberOfPlayers; i++) {
                    game.child(String.valueOf(i)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                players.add(String.valueOf(task.getResult().getValue()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.printf("8+d");
            }
        });


        System.out.println(players.toString() + " " + temp);

        setTempSpinValue();
        getTemp();
        mBottleImageView = (ImageView) findViewById(R.id.imageview_bottle);
        mBottleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hiddenAnswer.replace(" ", "").toUpperCase(Locale.ROOT).equals(answer)) {
                    showWinDialog();
                } else {
                    getTemp();
                    if (temp.equals(new Singletone().getInstance().user.getUid())) {
                        showDialog();
                        spinBottle();
                        db.getReference().child("game").child(nameOfGame).child("tempSpinValue").setValue(lastAngle);
                        // TODO заменить на кол-во игроков после их добавления
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference tempTurn = db.getReference()
                                .child("game")
                                .child(new Singletone().getInstance().nameOfGame)
                                .child("tempTurn");
                        for (int k = 0; k < numberOfPlayers; k++) {
                            if (players.get(k).equals(new Singletone().getInstance().user.getUid())) {
                                if (k == numberOfPlayers - 1) {
                                    tempTurn.setValue(players.get(0));
                                } else {
                                    tempTurn.setValue(players.get(k + 1));
                                }
                            }
                        }
                    }
                }
            }
        });
        workWithAnswer();
    }

    private void spinBottle() {
        double angle = sRandom.nextInt(15) + 1;
        intAngle = (int) (angle);
        checkSpinner(intAngle);
        // Центр вращения
        float pivotX = mBottleImageView.getWidth() / 2;
        float pivotY = mBottleImageView.getHeight() / 2;
        angle = angle * 22.5 + (sRandom.nextInt(15) + 1) * 360;
        final Animation animation = new RotateAnimation(lastAngle, (float) angle, pivotX, pivotY);
        lastAngle = (float) angle;
        animation.setDuration(2500);
        animation.setFillAfter(true);

        mBottleImageView.startAnimation(animation);

    }

    public void checkSpinner(int d){
        int score = scores[d];
        switch (score) {
            // минус все
            case -1000:
                myScores = 0;
                break;

            // еще один ход
            case 1:
                myScores += 1;
                break;

            // умножить очки на два
            case 2:
                myScores *= 2;
                break;

            // подарок в виде рандомного числа бонусов
            case 3:
                myScores += sRandom.nextInt(500);
                break;

            // +- рандомное кол-во бонусов
            case 4:
                myScores += sRandom.nextInt(1000) - 500;
                break;

            // прибавление очков
            default:
                myScores += scores[d];
                break;
        }

        int playerNumber = -1;
        // TODO: заменить на количество игроков после подключения
        for(int i = 0; i < numberOfPlayers; i++){
            if(players.get(i).equals(new Singletone().getInstance().user.getUid())) {
                playerNumber = i;
            }
        }

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference mScore = db.getReference().child("game")
                .child(new Singletone().getInstance().nameOfGame)
                .child("scores")
                .child(String.valueOf(playerNumber));
        mScore.setValue(String.valueOf(myScores)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", "nice");
                }
            }
        });


    }

    public void getPlayers(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("players");
        game.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (int i = 0; i < numberOfPlayers; i++) {
                    game.child(String.valueOf(i)).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                players.add(String.valueOf(task.getResult().getValue()));
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("8=D");
            }
        });
    }

    public void getTemp(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("tempTurn");
        game.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp = String.valueOf(snapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("8=d");
            }
        });
    }

    public void listen() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("tempSpinValue");
        game.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String ang = snapshot.getValue().toString();
                System.out.println(ang);
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("tempTurn");
                game.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                            temp = String.valueOf(task.getResult().getValue());
                            if(!temp.equals(new Singletone().getInstance().user.getUid())) {
                                // Центр вращения
                                float pivotX = mBottleImageView.getWidth() / 2;
                                float pivotY = mBottleImageView.getHeight() / 2;
                                final Animation animation = new RotateAnimation(lastAngle, Float.valueOf(ang), pivotX, pivotY);
                                lastAngle = Float.valueOf(ang);
                                animation.setDuration(2500);
                                animation.setFillAfter(true);

                                mBottleImageView.startAnimation(animation);
                            }
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("8=========================D");
            }
        });

    }

    // TODO listenHiddenAnswer
    public void listenHiddenAnswer() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference hA = db.getReference().child("game").child(nameOfGame).child("hiddenAnswer");
        hA.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    hiddenAnswer = snapshot.getValue().toString();
                    System.out.println(hiddenAnswer);
                    answView.setText(hiddenAnswer);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("8======D");
            }
        });
    }

    public void setTempSpinValue(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference game = db.getReference().child("game").child(nameOfGame).child("tempSpinValue");
        game.setValue("-1");
    }

    // TODO
    public void workWithAnswer() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            answer = bundle.getString("answer");
        }
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference letters = db
                .getReference()
                .child("game")
                .child(new Singletone().getInstance().nameOfGame)
                .child("openedLetters");
        letters.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    openedLetters = String.valueOf(task.getResult().getValue());
                }
            }
        });
        letters = db
                .getReference()
                .child("game")
                .child(new Singletone().getInstance().nameOfGame)
                .child("badLetters");
        letters.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    badLetters = String.valueOf(task.getResult().getValue());
                }
            }
        });

        //System.out.println(answer.length());
        DatabaseReference gameRef = db.getReference().child("game").child(nameOfGame).child("hiddenAnswer");
        gameRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    answView.setText(String.valueOf(task.getResult().getValue()));
                    hiddenAnswer = String.valueOf(task.getResult().getValue());
                }
            }
        });
    }

    public void showDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Введи букву");
        dialog.setMessage("одну букву, которой нет в списке...");

        LayoutInflater inflater = LayoutInflater.from(this);
        View enterLetter = inflater.inflate(R.layout.enter_symbol_view, null);
        dialog.setView(enterLetter);

        final EditText editLetter = enterLetter.findViewById(R.id.editLetter);
        final TextView badLettersView = enterLetter.findViewById(R.id.badLettersView);
        badLettersView.setText(openedLetters + badLetters);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newHiddenAnswer = "";
                String allLetters = openedLetters + badLetters;
                if(TextUtils.isEmpty(editLetter.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где буква", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(editLetter.getText().toString().length() > 1) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? одну букву", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(allLetters.toUpperCase(Locale.ROOT).indexOf(editLetter.getText().toString().toUpperCase(Locale.ROOT).charAt(0)) != -1) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? ее называли", Toast.LENGTH_LONG).show();
                    return;
                }
                else {
                    for(int j = 0; j < answer.length(); j++){
                        if(Character.compare(answer.toUpperCase(new Locale("ru", "RU")).charAt(j), editLetter.getText().toString().toUpperCase(new Locale("ru", "RU")).charAt(0)) == 0) {
                            StringBuilder nHA = new StringBuilder(hiddenAnswer);
                            nHA.setCharAt(j * 2, editLetter.getText().toString().charAt(0));
                            hiddenAnswer = nHA.toString();
                            f+=1;
                        }
/*                        else
                            if(Character.compare(hiddenAnswer.charAt(j * 2), '_') != 0){
                                newHiddenAnswer = hiddenAnswer.charAt(j * 2) + " ";
                            }
                            else {
                                newHiddenAnswer += "_ ";
                            }*/
                    }
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference hA = db.getReference().child("game")
                            .child(nameOfGame)
                            .child("hiddenAnswer");
                    hA.setValue(hiddenAnswer);
                    if(f > 0){
                        openedLetters += editLetter.getText().toString().charAt(0) + "; ";
                        DatabaseReference letters = db.getReference()
                                .child("game")
                                .child(new Singletone().getInstance().nameOfGame)
                                .child("openedLetters");
                        letters.setValue(openedLetters);

                    }
                    else
                    if(f == 0){
                        badLetters += editLetter.getText().toString().charAt(0) + "; ";
                        DatabaseReference letters = db.getReference()
                                .child("game")
                                .child(new Singletone().getInstance().nameOfGame)
                                .child("badLetters");
                        letters.setValue(badLetters);
                    }
                    f = 0;
                }
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
        Window view = alert.getWindow();
        view.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.light_background)));
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        //Set negative button background
        nbutton.setBackgroundColor(getResources().getColor(R.color.dark_background));
        //Set negative button text color
        nbutton.setTextColor(getResources().getColor(R.color.light_background));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        //Set positive button background
        pbutton.setBackgroundColor(getResources().getColor(R.color.dark_background));
        //Set positive button text color
        pbutton.setTextColor(getResources().getColor(R.color.light_background));
    }

    public void showWinDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ЕееЕЕе, ты угадал слово");
        dialog.setMessage("молодец...");

        dialog.setPositiveButton("Выход", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(GameActivity.this, StartPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog alert = dialog.create();
        alert.show();
        Window view = alert.getWindow();
        view.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.light_background)));
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        //Set negative button background
        nbutton.setBackgroundColor(getResources().getColor(R.color.dark_background));
        //Set negative button text color
        nbutton.setTextColor(getResources().getColor(R.color.light_background));
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        //Set positive button background
        pbutton.setBackgroundColor(getResources().getColor(R.color.dark_background));
        //Set positive button text color
        pbutton.setTextColor(getResources().getColor(R.color.light_background));
    }

}
