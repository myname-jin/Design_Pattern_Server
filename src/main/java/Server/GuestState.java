/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import Client.RegisterHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import Client.PasswordChangeHandler;

public class GuestState implements ClientState {

    private final Map<String, CommandAction> commandMap = new HashMap<>();

    public GuestState() {
        // [허용 명령]
        commandMap.put("REGISTER:", this::handleRegister);
        commandMap.put("LOGIN:", this::handleLogin);
        commandMap.put("PW_CHANGE:", this::handlePwChange);
       
    }

    @FunctionalInterface
    interface CommandAction {
        void execute(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException;
    }

    @Override
    public void process(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        String header = getHeader(msg);

        if (commandMap.containsKey(header)) {
            commandMap.get(header).execute(context, msg, in, out);
        } else {
            // 그 외 모든 명령(파일전송, 로그아웃 등) 차단
            out.write("ERROR:로그인이 필요하거나 잘못된 명령입니다.");
            out.newLine();
            out.flush();
            System.out.println("[GuestState] 비인가 요청 차단: " + header);
        }
    }

    // ─── [상세 로직] ─────────────────────────────

    private void handleRegister(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        RegisterHandler regHandler = new RegisterHandler(out);
        regHandler.handle(msg);
    }
    private void handlePwChange(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
    System.out.println("[GuestState] 비밀번호 변경 요청 수신");
    new PasswordChangeHandler(out).handle(msg);
    }
    private void handleLogin(ClientHandler context, String msg, 
            BufferedReader in, BufferedWriter out) throws IOException {
       
        
        
        System.out.println("[GuestState] 로그인 시도: " + msg);
        String[] parts = msg.substring("LOGIN:".length()).split(",");

        if (parts.length < 3) {
            out.write("FAIL"); out.newLine(); out.flush(); return;
        }

        String userId = parts[0].trim();
        String password = parts[1].trim();
        String role = parts[2].trim();

        // 1. 잠금 확인
        if (context.getLoginManager().isLocked(userId)) {
            out.write("LOGIN_LOCKED"); out.newLine(); out.flush(); return;
        }

        // 2. 유효성 검증 (ClientHandler의 메서드 호출)
        boolean valid = context.validateLogin(userId, password, role);

        if (!valid) {
            int count = context.getLoginManager().recordFailure(userId);
            out.write("FAIL:" + count); out.newLine(); out.flush();
            return;
        }

        // 3. 로그인 성공 처리
        context.getLoginManager().recordSuccess(userId);

        if ("admin".equalsIgnoreCase(role)) {
            out.write("LOGIN_SUCCESS");
        } else {
            SessionManager.PendingClient pending = new SessionManager.PendingClient(context.getSocket(), userId, out);
            SessionManager.LoginDecision result = context.getSessionManager().tryLogin(userId, pending);

            if (result == SessionManager.LoginDecision.OK) out.write("LOGIN_SUCCESS");
            else if (result == SessionManager.LoginDecision.WAIT) out.write("WAIT");
            else {
                out.write("FAIL"); out.newLine(); out.flush(); return;
            }
        }
        out.newLine(); out.flush();

        // ★ [핵심] 상태 전환 (Guest -> Member)
        context.setUserId(userId);
        context.setState(new MemberState()); 
        System.out.println("[State] " + userId + " 로그인 성공 -> MemberState 전환");
    }

    private String getHeader(String msg) {
        int idx = msg.indexOf(":");
        if (idx != -1) return msg.substring(0, idx + 1);
        return msg;
    }
}