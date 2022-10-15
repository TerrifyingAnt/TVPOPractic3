package it.mirea.fieldofmiracleswithyakudorovich.Models;

import static org.junit.Assert.*;

import org.junit.Test;

public class UserTest {

    User user = new User();

    @Test
    public void testUserName(){
        user.setName("User");
        assertEquals("User", user.getName());
    }

    @Test
    public void testUserEmail(){
        user.setEmail("email@email.com");
        assertEquals("email@email.com", user.getEmail());
    }

    @Test
    public void testUserPassword(){
        user.setPassword("123456");
        assertEquals("123456", user.getPassword());
    }

    @Test
    public void getName() {
    }

    @Test
    public void setName(String name) {
    }

    @Test
    public void getEmail() {
    }

    @Test
    public void setEmail(String email) {
    }

    @Test
    public void getPassword() {
    }

    @Test
    public void setPassword(String password) {
    }
}