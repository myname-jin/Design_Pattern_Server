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
import Client.PasswordChangeHandler;
import Client.UserInfoHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemberState implements ClientState {

    private final Map<String, CommandAction> commandMap = new HashMap<>();

    public MemberState() {
        // [허용 명령]
        commandMap.put("INFO_REQUEST:", this::handleInfo);
        commandMap.put("PW_CHANGE:", this::handlePwChange);
        commandMap.put("FILE_UPDATE:", this::handleFileUpdate);
        commandMap.put("LOGOUT:", this::handleLogout);
        
        // [차단 명령] 이미 로그인된 상태임
        commandMap.put("LOGIN:", this::handleAlreadyLoggedIn);
        commandMap.put("REGISTER:", this::handleAlreadyLoggedIn);
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
            out.write("ERROR:알 수 없는 명령입니다.");
            out.newLine();
            out.flush();
        }
    }

    // ─── [상세 로직] ─────────────────────────────

    private void handleInfo(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        new UserInfoHandler(context.getSocket(), out).handle(msg);
    }

    private void handlePwChange(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        new PasswordChangeHandler(out).handle(msg);
    }

    private void handleFileUpdate(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        String filename = msg.substring("FILE_UPDATE:".length()).trim();
        StringBuilder content = new StringBuilder();
        String line;
        while (!(line = in.readLine()).equals("<<EOF>>")) {
            content.append(line).append("\n");
        }
        new FileSyncManager().updateFile(filename, content.toString());
        System.out.println("[MemberState] 파일 동기화 완료: " + filename);
    }

    private void handleLogout(ClientHandler context, String msg, 
            BufferedReader in, BufferedWriter out) throws IOException {
        String userId = context.getUserId();
        System.out.println("[MemberState] 로그아웃 요청: " + userId);
        context.getSessionManager().logout(userId);

        context.setUserId(null);
        context.setState(new GuestState());
        System.out.println("[State] 로그아웃 -> GuestState 전환");
    }

    private void handleAlreadyLoggedIn(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException {
        out.write("ALREADY_LOGGED_IN");
        out.newLine();
        out.flush();
    }

    private String getHeader(String msg) {
        int idx = msg.indexOf(":");
        if (idx != -1) return msg.substring(0, idx + 1);
        return msg;
    }
}