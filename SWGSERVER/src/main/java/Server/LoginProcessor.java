/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class LoginProcessor {
    private final SessionManager sessionManager;

    public LoginProcessor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public boolean validateLogin(String userId, String password, String role) {
        String fileName = role.equals("admin") ? "src/main/resources/ADMIN_LOGIN.txt" : "src/main/resources/USER_LOGIN.txt";
        try (var reader = new java.io.BufferedReader(new java.io.FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2 &&
                    tokens[0].trim().equals(userId) &&
                    tokens[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("로그인 파일 오류: " + e.getMessage());
        }
        return false;
    }

    public SessionManager.LoginDecision tryUserLogin(String userId, Socket socket, BufferedWriter out) {
        SessionManager.PendingClient pending = new SessionManager.PendingClient(socket, userId, out);
        return sessionManager.tryLogin(userId, pending);
    }

    public void logout(String userId) {
        sessionManager.logout(userId);
    }
}