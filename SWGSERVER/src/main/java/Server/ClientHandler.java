/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import Client.FileSyncManager;
import java.io.*;
import java.net.Socket;

import Client.RegisterHandler;
import Client.UserInfoHandler;
import Client.PasswordChangeHandler; // 1. 임포트 추가

public class ClientHandler extends Thread {

    private final Socket socket;
    private final SessionManager sessionManager;
    private final LoginAttemptManager loginManager;
    private BufferedReader in;
    private BufferedWriter out;
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

    @Override
    public void run() {
        try {
            String msg;
            // String input = null; // (로그아웃 로직에서 msg로 변경되었으므로 제거 가능)

            while ((msg = in.readLine()) != null) {
                // ─── 회원가입 처리 ───────────────────────────────────
                if (msg.startsWith("REGISTER:")) {
                    RegisterHandler regHandler = new RegisterHandler(out);
                    regHandler.handle(msg);
                    continue;
                }

                // ─── 사용자 정보 요청 처리 ────────────────────────────
                if (msg.startsWith("INFO_REQUEST:")) {
                    UserInfoHandler infoHandler = new UserInfoHandler(socket, out);
                    infoHandler.handle(msg);
                    continue;
                }

                // ─── 로그인 처리 ─────────────────────────────────────
                if (msg.startsWith("LOGIN:")) {
                    // ... (로그인 잠금 기능이 적용된 로직) ...
                    System.out.println("[서버] 로그인 요청: " + msg);
                    String[] parts = msg.substring("LOGIN:".length()).split(",");
                    if (parts.length < 3) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    userId = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();

                    if (loginManager.isLocked(userId)) {
                        out.write("LOGIN_LOCKED");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_LOCKED");
                        continue;
                    }

                    boolean valid = validateLogin(userId, password, role);
                    System.out.println("[서버] 로그인 검증 결과: " + valid);
                    
                    if (!valid) {
                        int count = loginManager.recordFailure(userId);
                        out.write("FAIL:" + count);
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: FAIL:" + count);
                        continue;
                    }

                    loginManager.recordSuccess(userId);

                    if ("admin".equalsIgnoreCase(role)) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_SUCCESS (admin)");
                        continue;
                    }

                    SessionManager.PendingClient pending
                            = new SessionManager.PendingClient(socket, userId, out);
                    SessionManager.LoginDecision result
                            = sessionManager.tryLogin(userId, pending);

                    if (result == SessionManager.LoginDecision.OK) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_SUCCESS (user)");
                    } else if (result == SessionManager.LoginDecision.WAIT) {
                        out.write("WAIT");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: WAIT (queued)");
                    } else {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: FAIL");
                    }
                    continue;
                }
                
                // 2. [비밀번호 변경 처리] (신규 추가)
                if (msg.startsWith("PW_CHANGE:")) {
                    PasswordChangeHandler pwHandler = new PasswordChangeHandler(out);
                    pwHandler.handle(msg);
                    continue;
                }
                
                // ─── 텍스트 파일 동기화 처리 ──────────────────────────
                if (msg.startsWith("FILE_UPDATE:")) {
                    String filename = msg.substring("FILE_UPDATE:".length()).trim();
                    StringBuilder content = new StringBuilder();
                    String line;
                    while (!(line = in.readLine()).equals("<<EOF>>")) {
                        content.append(line).append("\n");
                    }

                    FileSyncManager manager = new FileSyncManager();
                    try {
                        manager.updateFile(filename, content.toString());
                        System.out.println("[서버] 파일 동기화 완료: " + filename);
                    } catch (IOException e) {
                        System.err.println("[서버] 파일 동기화 실패: " + filename);
                        e.printStackTrace();
                    }
                    continue;
                }
                // ─── 로그아웃 처리 ────────────────────────────────────
                if (msg.startsWith("LOGOUT:")) {
                    System.out.println("로그아웃 요청 수신: " + userId);
                    sessionManager.logout(userId);
                    break; // 스레드 종료
                }
            }
        } catch (IOException e) {
            System.out.println("[서버] 강제종료");
        } finally {
            try {
                if (userId != null) {
                    sessionManager.logout(userId);
                    System.out.println("[서버] 세션 정리: " + userId);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("[서버] 종료 ");
            }
        }
    }

    private boolean validateLogin(String userId, String password, String role) {
        String filePath = role.equalsIgnoreCase("admin")
                ? "src/main/resources/ADMIN_LOGIN.txt"
                : "src/main/resources/USER_LOGIN.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2
                        && parts[0].trim().equals(userId)
                        && parts[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("[서버] 로그인 검증 오류: " + e.getMessage());
        }
        return false;
    }
}