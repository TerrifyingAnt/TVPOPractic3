package it.mirea.fieldofmiracleswithyakudorovich;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.mirea.fieldofmiracleswithyakudorovich.Models.Game;
import it.mirea.fieldofmiracleswithyakudorovich.Models.User;
import it.mirea.fieldofmiracleswithyakudorovich.Util.Singletone;

public class StartPageActivity extends AppCompatActivity {

    Button createGame, joinGame, settings, info;
    int numberOfPlayers;
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_page_activity);

        createGame = findViewById(R.id.createGame);
        //joinGame = findViewById(R.id.joinGame);
        settings = findViewById(R.id.settings);
        info = findViewById(R.id.info);

        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseGameSettings();
            }
        });

//        joinGame.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chooseGame();
//            }
//        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartPageActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        DatabaseReference tasks = db.getReference().child("tasks").child("number").child("numb");
        tasks.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    new Singletone().getInstance().numberOfTasks = String.valueOf(task.getResult().getValue());
                }
            }
        });
    }

    private void chooseGameSettings() {
        Integer[] data = {1, 2, 3, 4};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Создание игры");
        dialog.setMessage("Введите все данные для создания игры");

        LayoutInflater inflater = LayoutInflater.from(this);
        View createGameView = inflater.inflate(R.layout.create_game_view, null);
        dialog.setView(createGameView);

        final EditText editGameName = createGameView.findViewById(R.id.editGameName);
        final EditText editGamePassword = createGameView.findViewById(R.id.editGamePassword);
        final Spinner spinner = createGameView.findViewById(R.id.spinner);

        // адаптер

        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Title");
        // выделяем элемент
        spinner.setSelection(2);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // показываем позиция нажатого элемента
                if(data[position] != 1) {
                    Toast.makeText(getBaseContext(), "Гений, мультиплеер не работает, написано же было" + position, Toast.LENGTH_SHORT).show();
                }
                numberOfPlayers = data[position];
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                numberOfPlayers = data[0];
            }
        });

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(StartPageActivity.this, GameActivity.class);
                        if (TextUtils.isEmpty(editGameName.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "Ты тупой? где почта", Toast.LENGTH_LONG).show();
                            return;
                        }
                        else
                            if (TextUtils.isEmpty(editGamePassword.getText().toString())) {
                            Toast.makeText(getApplicationContext(), "Ты тупой? где пароль", Toast.LENGTH_LONG).show();
                            return;
                        }
                            else
                                if ((editGamePassword.getText().toString()).length() < 8) {
                            Toast.makeText(getApplicationContext(), "Ты тупой? тебя взломают", Toast.LENGTH_LONG).show();
                            return;
                        }
                        FirebaseDatabase db = FirebaseDatabase.getInstance();
                        DatabaseReference gameRef = db.getReference().child("game");
                        System.out.println(new Singletone().getInstance().numberOfTasks);

                        new Singletone().getInstance().nameOfGame = editGameName.getText().toString();
                        Game game = new Game(new Singletone().getInstance().user.getUid(),
                                editGameName.getText().toString(),
                                numberOfPlayers,
                                editGamePassword.getText().toString(),
                                Integer.parseInt(new Singletone().getInstance().numberOfTasks));
                        gameRef.child(editGameName.getText().toString()).setValue(game)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Игра создана", Toast.LENGTH_LONG).show();
                                        String task = game.setupTask();
                                        String answ = game.setupAnswer();
                                        //game.setupHiddenAnswer();
                                        intent.putExtra("task", task);
                                        intent.putExtra("answer", answ);
                                        System.out.println(task + " " + answ + "8==========D");
                                        startActivity(intent);
                                        finish();
                                    }
                                });

                        // TODO: сделать бесконечную загрузку до момента, пока не наберется нужное кол-во игроков
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

    private void chooseGame() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Найти игру");
        dialog.setMessage("Введите данные для подключения к игре");

        LayoutInflater inflater = LayoutInflater.from(this);
        View joinGameView = inflater.inflate(R.layout.join_game_view, null);
        dialog.setView(joinGameView);

        final EditText joinGameName = joinGameView.findViewById(R.id.joinGameName);
        final EditText joinGamePassword = joinGameView.findViewById(R.id.joinGamePassword);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(StartPageActivity.this, GameActivity.class);
                if (TextUtils.isEmpty(joinGameName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где почта", Toast.LENGTH_LONG).show();
                    return;
                } else if (TextUtils.isEmpty(joinGamePassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где пароль", Toast.LENGTH_LONG).show();
                    return;
                } else if ((joinGamePassword.getText().toString()).length() < 8) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? тебя взломают", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                    DatabaseReference gameRef = db.getReference().child("game")
                            .child(joinGameName.getText().toString())
                            .child("gamePassword");
                    gameRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                String gamePassword = String.valueOf(task.getResult().getValue());
                                if (joinGamePassword.getText().toString().equals(gamePassword)) {
                                    FirebaseDatabase db = FirebaseDatabase.getInstance();
                                    new Singletone().getInstance().nameOfGame = joinGameName.getText().toString();
                                    DatabaseReference gameR = db.getReference().child("game")
                                            .child(joinGameName.getText().toString())
                                            .child("numbOfActivePlayers");
                                    gameR.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            } else {
                                                Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                                int numbOfActivePlayers = Integer.valueOf(String.valueOf(task.getResult().getValue()));
                                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                                DatabaseReference gamePlayers = db.getReference().child("game");
                                                gamePlayers = db.getReference()
                                                        .child("game")
                                                        .child(joinGameName.getText().toString())
                                                        .child("numbOfPlayers");
                                                gamePlayers.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                        if (!task.isSuccessful()) {
                                                            Log.e("firebase", "Error getting data", task.getException());
                                                        } else {
                                                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                                            int numberOfPlayers = Integer.valueOf(String.valueOf(task.getResult().getValue()));
                                                            if(numberOfPlayers > numbOfActivePlayers) {
                                                                DatabaseReference gP = db.getReference()
                                                                        .child("game")
                                                                        .child(joinGameName.getText().toString())
                                                                        .child("players")
                                                                        .child(String.valueOf(numbOfActivePlayers));
                                                                gP.setValue(new Singletone().getInstance().user.getUid());
                                                                gP = db.getReference().child("game")
                                                                        .child(joinGameName.getText().toString())
                                                                        .child("numbOfActivePlayers");
                                                                gP.setValue(numbOfActivePlayers + 1);
                                                                Intent intent = new Intent(StartPageActivity.this, GameActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }

                    });

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

//    @Override
//    public void onResume() {
//        super.onResume();
//        if(new Singletone().getInstance().nameOfGame != null){
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        DatabaseReference delRef = db.getReference().child("game").child(new Singletone().getInstance().nameOfGame);
//        delRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                System.out.println("8==D");
//                new Singletone().getInstance().nameOfGame = null;
//            }
//        });
//        }
//    }

}
