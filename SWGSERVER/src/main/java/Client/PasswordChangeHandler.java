/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PasswordChangeHandler {
    private final BufferedWriter out;
    // ★ 수정됨: member.txt 사용
    private static final String MEMBER_FILE = "src/main/resources/member.txt";

    public PasswordChangeHandler(BufferedWriter out) { this.out = out; }

    public void handle(String msg) {
        try {
            String[] parts = msg.substring("PW_CHANGE:".length()).split(",", 3);
            if (parts.length < 3) { send("PW_CHANGE_FAIL:INVALID_FORMAT"); return; }
            
            String userId = parts[0].trim();
            String oldPassword = parts[1].trim();
            String newPassword = parts[2].trim();

            File file = new File(MEMBER_FILE);
            
            // 변경 로직 수행
            int result = changePassword(file, userId, oldPassword, newPassword);

            if (result == 0) send("PW_CHANGE_SUCCESS");
            else if (result == 2) send("PW_CHANGE_FAIL:WRONG_OLD_PW");
            else send("PW_CHANGE_FAIL:NO_ID");

        } catch (Exception e) { send("PW_CHANGE_FAIL:SERVER_ERROR"); }
    }

    private int changePassword(File file, String id, String oldPw, String newPw) throws IOException {
        if (!file.exists()) return 1;
        List<String> lines = new ArrayList<>();
        boolean found = false;
        boolean pwMatch = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                // 포맷: id(0), pw(1), name(2), dept(3), role(4)
                if (p.length >= 5 && p[0].equals(id)) {
                    found = true;
                    if (p[1].equals(oldPw)) {
                        p[1] = newPw; // 비밀번호 교체
                        lines.add(String.join(",", p));
                        pwMatch = true;
                    } else {
                        lines.add(line); // 비번 틀림 -> 원본 유지
                    }
                } else {
                    lines.add(line); // 다른 사람 데이터 -> 유지
                }
            }
        }

        if (!found) return 1;
        if (!pwMatch) return 2;

        // 파일 덮어쓰기
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String s : lines) { bw.write(s); bw.newLine(); }
        }
        return 0;
    }

    private void send(String s) { try { out.write(s); out.newLine(); out.flush(); } catch (IOException e) {} }
}