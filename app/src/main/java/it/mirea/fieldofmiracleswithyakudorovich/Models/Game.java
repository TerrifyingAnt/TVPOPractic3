package it.mirea.fieldofmiracleswithyakudorovich.Models;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import it.mirea.fieldofmiracleswithyakudorovich.Util.Singletone;

public class Game {


    String nameOfGame; // название игры
    String adminId; // getUid() создателя игры
    String taskForGame, answer; // задание и ответ
    int numbOfPlayers, numbOfActivePlayers; // максимальное число игроков в игре, число игрков, которые подключены к игре
    List<String> players = new ArrayList<String>(); // getUid() игроков
    List<Integer> scores = new ArrayList<Integer>(); // число очков у каждого игрока
    String openedLetters; //буквы, которые есть в слове и их открыли
    String badLetters; // буквы, которых нет в слове, но которые были названы игроками
    int numbOfOpenedLetters, numbOfBadLetters; // число названных открытых букв, число букв, которые не в слове
    int numberOfTasks; // количество заданий в бд
    String tempTurn; // чей сейчас ход
    String gamePassword; // пароль от текущей игры

    public Game(String adminId, String nameOfGame, int numbOfPlayers, String gamePassword, int numberOfTasks) {
        getNumberOfTasks();
        this.numbOfActivePlayers = 1;
        this.numberOfTasks = Integer.parseInt(new Singletone().getInstance().numberOfTasks);
        this.adminId = adminId;
        this.nameOfGame = nameOfGame;
        this.gamePassword = gamePassword;
        this.numbOfPlayers = numbOfPlayers;
        this.players.add(adminId);
        this.tempTurn = adminId;
        for(int i = 0; i < this.numbOfPlayers; i++) {
            scores.add(0);
        }
        this.taskForGame = new Singletone().getInstance().task;
        this.answer = new Singletone().getInstance().answer;
        System.out.println(new Singletone().getInstance().numberOfTasks + new Singletone().getInstance().task);
        System.out.println(new Singletone().getInstance().answer);
        this.badLetters = "";
        this.openedLetters = "";


        //numberOfTasks = getNumberOfTasks();
        //System.out.println(numberOfTasks);
    }

    private String getTasks(int number) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tasks;
        final String[] taskForGameT = new String[1];
        tasks = db.getReference()
                .child("tasks")
                .child(String.valueOf(ThreadLocalRandom.current().nextInt(1, numberOfTasks)))
                .child("task");
        tasks.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    taskForGameT[0] = "8===D";
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    taskForGameT[0] = String.valueOf(task.getResult().getValue());

                }
            }
        });
        return taskForGameT[0];
    }

    private void getNumberOfTasks() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tasksTemp = db.getReference()
                .child("tasks")
                .child(String.valueOf(ThreadLocalRandom.current().nextInt(1, Integer.valueOf(new Singletone().getInstance().numberOfTasks) + 1)));
        DatabaseReference taskTask = tasksTemp.child("task");
        taskTask.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    new Singletone().getInstance().task = "8===D";
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    new Singletone().getInstance().task = String.valueOf(task.getResult().getValue());

                }
            }
        });
        DatabaseReference taskAnswer = tasksTemp.child("answer");
        taskAnswer.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                    new Singletone().getInstance().answer = "8===D";
                } else {
                    Log.d("firebase", String.valueOf(task.getResult().getValue()));
                    new Singletone().getInstance().answer = String.valueOf(task.getResult().getValue());
                }
            }
        });
        setTaskForGame(new Singletone().getInstance().task);
        setAnswer(new Singletone().getInstance().answer);
    }

    public String setupTask() {
        this.taskForGame = new Singletone().getInstance().task;
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = db.getReference().child("game");
        gameRef.child(new Singletone().getInstance().nameOfGame).child("task").setValue(this.taskForGame);
        return this.taskForGame;
    }

    public String setupAnswer() {
        this.answer = new Singletone().getInstance().answer;
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = db.getReference().child("game");
        gameRef.child(new Singletone().getInstance().nameOfGame).child("answer").setValue(this.answer);
        setupHiddenAnswer();
        return new Singletone().getInstance().answer;
    }

    public void setupHiddenAnswer() {
        String hiddenAnswer = "";
        for(int i = 0; i < new Singletone().getInstance().answer.length(); i++){
            if(Character.compare(new Singletone().getInstance().answer.charAt(i), ' ') != 0) {
                hiddenAnswer += "_ ";
            }
            else {
                hiddenAnswer += ' ';
            }
        }
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference gameRef = db.getReference().child("game");
        gameRef.child(new Singletone().getInstance().nameOfGame).child("hiddenAnswer").setValue(hiddenAnswer);
    }

    public String getNameOfGame() {
        return nameOfGame;
    }

    public void setNameOfGame(String nameOfGame) {
        this.nameOfGame = nameOfGame;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getTaskForGame() {
        return taskForGame;
    }

    public void setTaskForGame(String taskForGame) {
        this.taskForGame = taskForGame;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getNumbOfPlayers() {
        return numbOfPlayers;
    }

    public void setNumbOfPlayers(int numbOfPlayers) {
        this.numbOfPlayers = numbOfPlayers;
    }

    public int getNumbOfActivePlayers() {
        return numbOfActivePlayers;
    }

    public void setNumbOfActivePlayers(int numbOfActivePlayers) {
        this.numbOfActivePlayers = numbOfActivePlayers;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }

    public String getOpenedLetters() {
        return openedLetters;
    }

    public void setOpenedLetters(String openedLetters) {
        this.openedLetters = openedLetters;
    }

    public String getBadLetters() {
        return badLetters;
    }

    public void setBadLetters(String badLetters) {
        this.badLetters = badLetters;
    }

    public int getNumbOfOpenedLetters() {
        return numbOfOpenedLetters;
    }

    public void setNumbOfOpenedLetters(int numbOfOpenedLetters) {
        this.numbOfOpenedLetters = numbOfOpenedLetters;
    }

    public int getNumbOfBadLetters() {
        return numbOfBadLetters;
    }

    public void setNumbOfBadLetters(int numbOfBadLetters) {
        this.numbOfBadLetters = numbOfBadLetters;
    }

    public void setNumberOfTasks(int numberOfTasks) {
        this.numberOfTasks = numberOfTasks;
    }

    public String getTempTurn() {
        return tempTurn;
    }

    public void setTempTurn(String tempTurn) {
        this.tempTurn = tempTurn;
    }

    public String getGamePassword() {
        return gamePassword;
    }

    public void setGamePassword(String gamePassword) {
        this.gamePassword = gamePassword;
    }
}
