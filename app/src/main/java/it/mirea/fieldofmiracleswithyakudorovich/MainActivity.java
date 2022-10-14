package it.mirea.fieldofmiracleswithyakudorovich;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import it.mirea.fieldofmiracleswithyakudorovich.Models.User;
import it.mirea.fieldofmiracleswithyakudorovich.Util.Singletone;

public class MainActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_NAME = "Nickname";
    public static final String APP_PREFERENCES_EMAIL = "Email";
    public static final String APP_PREFERENCES_PASSWORD = "Password";

    Button loginButton, registerButton;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseUser user;
    SharedPreferences mSettings;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();


        editor = mSettings.edit();
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("users");
        user = FirebaseAuth.getInstance().getCurrentUser();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterWindow();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginWindow();
            }
        });
    }

    // Создание регистрационного диалого
    // Вывод регистрационного диалога
    private void showRegisterWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Регистрация");
        dialog.setMessage("Введите все данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.register_view, null);
        dialog.setView(registerWindow);

        final EditText editNickName = registerWindow.findViewById(R.id.editNickName);
        final EditText editEmail = registerWindow.findViewById(R.id.editEmail);
        final EditText editPassword = registerWindow.findViewById(R.id.editPassword);
        final EditText editPasswordAgain = registerWindow.findViewById(R.id.editPasswordAgain);


        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(editEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где почта", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(TextUtils.isEmpty(editNickName.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где ник", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(TextUtils.isEmpty(editPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где пароль", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if((editPassword.getText().toString()).length() < 8) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? тебя взломают", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(!editPassword.getText().toString().equals(editPasswordAgain.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? пароли не совпадают", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(editEmail.getText().toString(), editPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        User user = new User();
                        user.setEmail(editEmail.getText().toString());
                        user.setName(editNickName.getText().toString());
                        user.setPassword(editPassword.getText().toString());
                        user.setImage("");

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        FirebaseUser user = auth.getCurrentUser();
                                        new Singletone().getInstance().nickname = editNickName.getText().toString();
                                        new Singletone().getInstance().email = editEmail.getText().toString();
                                        new Singletone().getInstance().password = editPassword.getText().toString();
                                        new Singletone().getInstance().user = user;
                                        editor.putString(APP_PREFERENCES_NAME, editNickName.getText().toString());
                                        editor.putString(APP_PREFERENCES_EMAIL, editEmail.getText().toString());
                                        editor.putString(APP_PREFERENCES_PASSWORD, editPassword.getText().toString());
                                        editor.apply();
                                        Toast.makeText(getApplicationContext(), "Наконец-то зарегался", Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                });
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

    // Создание окна авторизации
    // Вывод окна авторизации
    private void showLoginWindow() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Логин");
        dialog.setMessage("Введите все данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.login_view, null);
        dialog.setView(registerWindow);

        final EditText editLoginEmail = registerWindow.findViewById(R.id.editLoginEmail);
        final EditText editLoginPassword = registerWindow.findViewById(R.id.editLoginPassword);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(TextUtils.isEmpty(editLoginEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где почта", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                if(TextUtils.isEmpty(editLoginPassword.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Ты тупой? где пароль", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.signInWithEmailAndPassword(editLoginEmail.getText().toString(), editLoginPassword.getText().toString()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                new Singletone().getInstance().email = editLoginEmail.getText().toString();
                                new Singletone().getInstance().password = editLoginPassword.getText().toString();
                                new Singletone().getInstance().user = user;
                                users.child(user.getUid()).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (!task.isSuccessful()) {
                                            Log.e("firebase", "Error getting data", task.getException());
                                        }
                                        else {
                                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                            new Singletone().getInstance().nickname = String.valueOf(task.getResult().getValue());
                                        }
                                    }
                                });
                                editor.putString(APP_PREFERENCES_EMAIL, editLoginEmail.getText().toString());
                                editor.putString(APP_PREFERENCES_PASSWORD, editLoginPassword.getText().toString());
                                editor.apply();
                                Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

    @Override
    public void onResume() {
        Intent intentToProfile;
        super.onResume();
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        if (mSettings != null) {
            if (!Objects.equals(mSettings.getString("Email", ""), "") && !mSettings.getString("Password", "").equals("")) {
                auth.signInWithEmailAndPassword(mSettings.getString("Email", ""), mSettings.getString("Password", "")).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            new Singletone().getInstance().email = mSettings.getString("Email", "");
                            new Singletone().getInstance().password = mSettings.getString("Password", "");
                            new Singletone().getInstance().user = user;
                            users.child(user.getUid()).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e("firebase", "Error getting data", task.getException());
                                    }
                                    else {
                                        Log.d("firebase", String.valueOf(task.getResult().getValue()));
                                        new Singletone().getInstance().nickname = String.valueOf(task.getResult().getValue());
                                    }
                                }
                            });
                            Intent intent = new Intent(MainActivity.this, StartPageActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        }
    }

}