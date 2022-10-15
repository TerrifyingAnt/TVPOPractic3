package it.mirea.fieldofmiracleswithyakudorovich;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import it.mirea.fieldofmiracleswithyakudorovich.Util.Singletone;

public class SettingsActivity extends AppCompatActivity {

    TextView nickName;
    Button mainPageButton, exitButton;
    ImageView changeNicknameButton, changeAvatarButton, avatarImage;
    Uri filePath;
    final static int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        nickName = findViewById(R.id.nickName);
        mainPageButton = findViewById(R.id.mainPageButton);
        exitButton = findViewById(R.id.exitButton);
        changeNicknameButton = findViewById(R.id.changeNicknameButton);
        avatarImage = findViewById(R.id.avatarImage);
        changeAvatarButton = findViewById(R.id.changeAvatarButton);

        checkImage();

        nickName.setText(new Singletone().getInstance().nickname);

        mainPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, StartPageActivity.class);
                startActivity(intent);
                finish();
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences mSettings;
                SharedPreferences.Editor editor;
                new Singletone().getInstance().password = "";
                new Singletone().getInstance().email = "";
                new Singletone().getInstance().nickname = "";
                new Singletone().getInstance().user = null;
                mSettings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                editor = mSettings.edit();
                editor.putString("Email", "");
                editor.putString("Password", "");
                editor.putString("Email", "");
                editor.putString("Nickname", "");
                editor.apply();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        changeNicknameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeNicknameDialog();
            }

        });

        changeAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isStoragePermissionGranted()) {
                    DatabaseReference users;
                    users = FirebaseDatabase.getInstance().getReference("users");
                    users.child(new Singletone().getInstance().user.getUid()).child("image")
                            .setValue(new Singletone().getInstance().user.getUid())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Изменил аву, браво", Toast.LENGTH_SHORT).show();
                                }
                            });
                    chooseImage();
                }
            }
        });
    }

    private void changeNicknameDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Изменение никнейма");
        dialog.setMessage("Зачем - непонятно");

        LayoutInflater inflater = LayoutInflater.from(this);
        View changeNickname = inflater.inflate(R.layout.change_nickname_view, null);
        dialog.setView(changeNickname);

        final EditText editNickname = (EditText) changeNickname.findViewById(R.id.editNickname);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(!editNickname.getText().toString().equals("")) {
                    DatabaseReference users;
                    users = FirebaseDatabase.getInstance().getReference("users");

                    users.child(new Singletone().getInstance().user.getUid()).child("name")
                            .setValue(editNickname.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getApplicationContext(), "Изменил ник, браво", Toast.LENGTH_SHORT).show();
                                    new Singletone().getInstance().nickname = editNickname.getText().toString();
                                    SharedPreferences mSettings;
                                    SharedPreferences.Editor editor;
                                    mSettings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                                    editor = mSettings.edit();
                                    editor.putString("Nickname", editNickname.getText().toString());
                                }
                            });
                }
                nickName.setText(new Singletone().getInstance().nickname);
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

    private void checkImage(){
        final String[] imageUrl = {""};
        DatabaseReference users;
        users = FirebaseDatabase.getInstance().getReference("users");
        users.child(new Singletone().getInstance().user.getUid()).child("image").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    imageUrl[0] = String.valueOf(task.getResult().getValue());
                    if (!imageUrl[0].equals("")){
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        StorageReference storageReference = storage.getReference().child("Avatars").child(imageUrl[0]);
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageURL = uri.toString();
                                Glide.with(getApplicationContext()).load(imageURL).into(avatarImage);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                System.out.println("8===================D пиздец");
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                avatarImage.setImageBitmap(bitmap);
                uploadImage();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {
        if(filePath != null)
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference().child("Avatars");

            StorageReference ref = storageReference.child(new Singletone().getInstance().user.getUid().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(SettingsActivity.this, "Загружено", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SettingsActivity.this, "не загружено( "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            System.out.println("EBANINAAAAAAAAAAAAAA 8==========D " + filePath.toString());
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission is granted");
            return true;
        } else {

            Log.v(TAG,"Permission is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }

}

