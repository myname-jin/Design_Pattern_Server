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
    // 경로가 정확한지 다시 한번 확인하세요. 
    // NetBeans 프로젝트 루트 기준 상대 경로입니다.
    private static final String MEMBER_FILE = "src/main/resources/member.txt";

    public PasswordChangeHandler(BufferedWriter out) { this.out = out; }

    public void handle(String msg) {
        System.out.println("[DEBUG] 요청 수신: " + msg); // 1. 요청 잘 왔는지 확인

        try {
            String[] parts = msg.substring("PW_CHANGE:".length()).split(",", 3);
            if (parts.length < 3) { 
                System.out.println("[DEBUG] 포맷 에러");
                send("PW_CHANGE_FAIL:INVALID_FORMAT"); 
                return; 
            }
            
            String userId = parts[0].trim();
            String oldPassword = parts[1].trim();
            String newPassword = parts[2].trim();

            File file = new File(MEMBER_FILE);
            System.out.println("[DEBUG] 파일 절대 경로: " + file.getAbsolutePath()); // 2. 파일 경로 확인
            
            // 변경 로직 수행
            int result = changePassword(file, userId, oldPassword, newPassword);

            System.out.println("[DEBUG] 결과 코드: " + result); // 3. 결과 코드 확인

            if (result == 0) send("PW_CHANGE_SUCCESS");
            else if (result == 2) send("PW_CHANGE_FAIL:WRONG_OLD_PW");
            else send("PW_CHANGE_FAIL:NO_ID");

        } catch (Exception e) { 
            // ★★★ 여기가 제일 중요합니다. 에러 내용을 서버 콘솔에 찍습니다.
            System.out.println("[ERROR] 처리 중 예외 발생!");
            e.printStackTrace(); 
            send("PW_CHANGE_FAIL:SERVER_ERROR"); 
        }
    }

    private int changePassword(File file, String id, String oldPw, String newPw) throws IOException {
        if (!file.exists()) {
            System.out.println("[DEBUG] 파일이 존재하지 않음!");
            return 1;
        }

        List<String> lines = new ArrayList<>();
        boolean found = false;
        boolean pwMatch = false;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");

                // 데이터 검증 및 ID 비교
                if (p.length >= 5 && p[0].trim().equals(id)) {
                    System.out.println("[DEBUG] ID 찾음: " + id);
                    System.out.println("[DEBUG] 파일속 비번: [" + p[1].trim() + "] vs 입력한 비번: [" + oldPw + "]");
                    
                    found = true;
                    if (p[1].trim().equals(oldPw)) {
                        System.out.println("[DEBUG] 비밀번호 일치! 변경 진행");
                        p[1] = newPw; // 비밀번호 교체
                        
                        // 배열을 다시 문자열로 합침 (Java 8 이상)
                        lines.add(String.join(",", p));
                        pwMatch = true;
                    } else {
                        System.out.println("[DEBUG] 비밀번호 불일치");
                        lines.add(line); // 비번 틀림 -> 원본 유지
                    }
                } else {
                    lines.add(line); // 다른 사람 데이터 -> 유지
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] 파일 읽기 중 에러: " + e.getMessage());
            throw e; // 상위로 던짐
        }

        if (!found) {
            System.out.println("[DEBUG] 해당 ID를 파일에서 못 찾음");
            return 1;
        }
        if (!pwMatch) {
            return 2;
        }

        // 파일 덮어쓰기
        System.out.println("[DEBUG] 파일 덮어쓰기 시작...");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (String s : lines) { 
                bw.write(s); 
                bw.newLine(); 
            }
        } catch (IOException e) {
            System.out.println("[ERROR] 파일 쓰기 권한 에러! 파일이 열려있거나 경로 문제일 수 있음.");
            e.printStackTrace();
            throw e;
        }
        
        System.out.println("[DEBUG] 파일 변경 완료");
        return 0;
    }

    private void send(String s) { try { out.write(s); out.newLine(); out.flush(); } catch (IOException e) {} }
}