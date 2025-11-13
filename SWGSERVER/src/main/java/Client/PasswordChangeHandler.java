/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * "PW_CHANGE:id,oldPw,newPw" 요청을 처리합니다.
 * @author adsd3
 */
public class PasswordChangeHandler {
    private static final String RESOURCE_DIR = "src/main/resources";
    private final BufferedWriter out;

    public PasswordChangeHandler(BufferedWriter out) {
        this.out = out;
    }

    public void handle(String msg) {
        try {
            // 1. 프로토콜 파싱 변경 (id,oldPw,newPw)
            String[] parts = msg.substring("PW_CHANGE:".length()).split(",", 3);
            if (parts.length < 3) {
                send("PW_CHANGE_FAIL:INVALID_FORMAT");
                return;
            }
            
            String userId = parts[0].trim();
            String oldPassword = parts[1].trim();
            String newPassword = parts[2].trim();

            File adminFile = new File(RESOURCE_DIR, "ADMIN_LOGIN.txt");
            File userFile = new File(RESOURCE_DIR, "USER_LOGIN.txt");

            // 2. 두 파일 중 하나에서 ID와 기존 비밀번호를 검증하고 변경
            int resultAdmin = findAndReplacePassword(adminFile, userId, oldPassword, newPassword);
            int resultUser = findAndReplacePassword(userFile, userId, oldPassword, newPassword);

            if (resultAdmin == 0 || resultUser == 0) {
                send("PW_CHANGE_SUCCESS");
            } else if (resultAdmin == 2 || resultUser == 2) {
                send("PW_CHANGE_FAIL:WRONG_OLD_PW"); // 3. 기존 비밀번호 오류
            } else {
                send("PW_CHANGE_FAIL:NO_ID"); // 4. ID 없음
            }

        } catch (IOException e) {
            e.printStackTrace();
            send("PW_CHANGE_FAIL:SERVER_ERROR");
        }
    }

    /**
     * 파일을 읽어 ID와 기존 PW를 검증하고, 라인을 교체한 뒤, 파일을 덮어씁니다.
     * @return 0=성공, 1=ID없음, 2=PW틀림
     */
    private int findAndReplacePassword(File file, String userId, String oldPassword, String newPassword) throws IOException {
        if (!file.exists()) return 1; // ID 없음

        List<String> lines = new ArrayList<>();
        boolean idFound = false;
        boolean passwordMatch = false;

        // 1. 파일을 읽어 메모리에 저장
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].trim().equals(userId)) {
                    idFound = true;
                    if (parts[1].trim().equals(oldPassword)) {
                        // ID와 기존 PW 모두 일치
                        lines.add(userId + "," + newPassword); // 새 비밀번호로 교체
                        passwordMatch = true;
                    } else {
                        // ID는 맞는데 PW가 틀림
                        lines.add(line); // 원본 라인 유지
                    }
                } else {
                    lines.add(line);
                }
            }
        }

        if (!idFound) {
            return 1; // 이 파일에서 ID를 못 찾음
        }
        if (!passwordMatch) {
            return 2; // ID는 찾았으나 PW가 틀림
        }

        // 2. 파일 전체를 덮어쓰기 (성공한 경우에만)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) { // append=false
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        
        return 0; // 성공
    }

    private void send(String code) {
        try {
            out.write(code);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}