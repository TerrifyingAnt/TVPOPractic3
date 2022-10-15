package it.mirea.fieldofmiracleswithyakudorovich.Models;

import static org.junit.Assert.*;

import org.junit.Test;

public class GameTest {
    Game game = new Game(
            "1",
            "name_game",
            5,
            "123456",
            10
    );

    @Test
    public void testGetGamePassword(){
        assertEquals("123456", game.getGamePassword());
    }

    @Test
    public void testSetGetGamePassword(){
        game.setGamePassword("qwerty");
        assertEquals("qwerty", game.getGamePassword());
    }

    @Test
    public void testGetNumbOfPlayers(){
        assertEquals(5, game.getNumbOfPlayers());
    }

    @Test
    public void testSetGetNumbOfPlayers(){
        game.setNumbOfPlayers(3);
        assertEquals(3, game.getNumbOfPlayers());
    }

    @Test
    public void testGetAdminId(){
        assertEquals("1", game.getAdminId());
    }

    @Test
    public void  testSetGetAdminId(){
        game.setAdminId("2");
        assertEquals("2", game.getAdminId());
    }

    @Test
    public void testGetNameOfGame(){
        assertEquals("name_game", game.getNameOfGame());
    }

    @Test
    public void testSetGetNameOfGame(){
        game.setNameOfGame("name_of_game");
        assertEquals("name_of_game", game.getNameOfGame());
    }

    @Test
    public void testSetGetAnswer(){
        game.setAnswer("Answer");
        assertEquals("Answer", game.getAnswer());
    }

    @Test
    public void getNumbOfPlayers() {
    }

    @Test
    public void getGamePassword() {
    }

    @Test
    public void getAdminId(){
    }

    @Test
    public void getNameOfGame(){
    }

    @Test
    public void setNameOfGame(String name){
    }

    @Test
    public void setAdminId(String adminId){
    }

    @Test
    public void setGamePassword(String password){
    }

    @Test
    public void setNumbOfPlayers(Integer numbOfPlayers){
    }

    @Test
    public void setAnswer(String answer){
    }
}