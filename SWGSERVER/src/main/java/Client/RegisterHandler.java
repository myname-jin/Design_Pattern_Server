/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author adsd3
 */
import java.io.*;

public class RegisterHandler {
    private final BufferedWriter out;
    // ★ 수정됨: 파일명이 소문자 'member.txt'로 변경됨
    private static final String MEMBER_FILE = "src/main/resources/member.txt";

    public RegisterHandler(BufferedWriter out) {
        this.out = out;
    }

    public void handle(String msg) {
        // msg 구조: "REGISTER:role:id:pw:name:dept"
        String[] parts = msg.split(":", 6);
        if (parts.length < 3) {
            sendFail("INVALID_FORMAT");
            return;
        }

        String role = parts[1].trim();
        String id   = parts[2].trim();
        String pw   = parts.length > 3 ? parts[3].trim() : "";
        String name = parts.length > 4 ? parts[4].trim() : "";
        String dept = parts.length > 5 ? parts[5].trim() : "";

        File file = new File(MEMBER_FILE);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        // 1. 중복 ID 검사 (member.txt 하나만 뒤지면 됨)
        if (isIdDuplicate(id, file)) {
            send("REGISTER_FAIL:DUPLICATE_ID");
            return;
        }

        // 2. 파일 쓰기 (형식: id,pw,name,dept,role)
        String line = String.join(",", id, pw, name, dept, role);
        
        if (writeLine(file, line)) {
            send("REGISTER_SUCCESS");
        } else {
            sendFail("FILE_WRITE_ERROR");
        }
    }

    private boolean isIdDuplicate(String id, File file) {
        if (!file.exists()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].trim().equals(id)) {
                    return true;
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        return false;
    }

    private boolean writeLine(File file, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) { e.printStackTrace(); return false; }
    }

    private void send(String code) {
        try { out.write(code); out.newLine(); out.flush(); } catch (IOException e) {}
    }
    private void sendFail(String reason) { send("REGISTER_FAIL:" + reason); }
}