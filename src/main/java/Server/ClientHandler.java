/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final SessionManager sessionManager;
    private final LoginAttemptManager loginManager;
    private BufferedReader in;
    private BufferedWriter out;
    
    // [State Pattern] 현재 상태 (초기값: Guest)
    private ClientState state = new GuestState();
    private String userId = null;

    public ClientHandler(Socket socket, SessionManager sessionManager, LoginAttemptManager loginManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
        this.loginManager = loginManager;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("스트림 초기화 실패: " + e.getMessage());
        }
    }

    // [State Pattern] 상태 변경 메서드 (State 객체가 호출함)
    public void setState(ClientState newState) {
        this.state = newState;
    }

    // State 객체들이 사용할 Getter/Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public SessionManager getSessionManager() { return sessionManager; }
    public LoginAttemptManager getLoginManager() { return loginManager; }
    public Socket getSocket() { return socket; }

    @Override
    public void run() {
        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                state.process(this, msg, in, out);
            }
        } catch (IOException e) {
            System.out.println("[서버] 클라이언트 연결 종료");
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (userId != null) {
                sessionManager.logout(userId);
                System.out.println("[서버] 세션 정리: " + userId);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // [로그인 검증 로직] GuestState에서 호출하기 위해 유지
    public boolean validateLogin(String userId, String password, String role) {
        File file = new File("src/main/resources/member.txt");
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String fileId = parts[0].trim();
                    String filePw = parts[1].trim();
                    String fileRole = parts[4].trim();

                    if (fileId.equals(userId) && filePw.equals(password)) {
                        if (role.equalsIgnoreCase("admin")) {
                            if (fileRole.equalsIgnoreCase("admin") || fileRole.equals("관리자")) return true;
                        } else if (role.equalsIgnoreCase("user")) {
                            if (fileRole.equalsIgnoreCase("user") || fileRole.equals("학생") || fileRole.equals("교수")) return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("로그인 검증 오류: " + e.getMessage());
        }
        return false;
    }
}