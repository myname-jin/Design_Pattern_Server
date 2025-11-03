/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author adsd3
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RegisterHandler {
    // resources 폴더 기준 상대 경로
    private static final String RESOURCE_DIR = "src/main/resources";
    private final BufferedWriter out;

    public RegisterHandler(BufferedWriter out) {
        this.out = out;
    }

    /**
     * 메시지 포맷: "REGISTER:role:id:pw:name:dept"
     *  - 관리자(admin)인 경우 name, dept는 없어도 됨
     */
    public void handle(String msg) {
        // 디버깅 로그: 작업 디렉토리 및 원본 메시지
        System.out.println("Working dir: " + new File(".").getAbsolutePath());
        System.out.println("회원가입 요청: " + msg);

        // 1) 콜론으로 파싱
        String[] parts = msg.split(":", 6);
        if (parts.length < 3 || !"REGISTER".equals(parts[0])) {
            sendFail("INVALID_FORMAT");
            return;
        }

        String role = parts[1].trim();            // 역할
        String id   = parts[2].trim();            // 아이디
        String pw   = parts.length > 3 ? parts[3].trim() : "";    // 비번
        String name = parts.length > 4 ? parts[4].trim() : "";    // 이름
        String dept = parts.length > 5 ? parts[5].trim() : "";    // 학과

        boolean success;
        // 2) resources 폴더 보장
        File dir = new File(RESOURCE_DIR);
        if (!dir.exists()) dir.mkdirs();

        // 3) 역할별 파일 쓰기
        if ("admin".equalsIgnoreCase(role)) {
            // 관리자: ADMIN_LOGIN.txt 에만 저장
            success = writeLine(new File(dir, "ADMIN_LOGIN.txt"), id + "," + pw);
        } else {
            // 학생/교수: USER_LOGIN + USER_INFO
            boolean a = writeLine(new File(dir, "USER_LOGIN.txt"), id + "," + pw);
            boolean b = writeLine(new File(dir, "USER_INFO.txt"),
                                  String.join(",", id, pw, name, dept, role));
            success = a && b;
        }

        // 4) 클라이언트에 결과 응답
        if (success) send("REGISTER_SUCCESS");
        else        sendFail("FILE_WRITE_ERROR");
    }

    /** 한 파일에 한 줄을 append 모드로 쓰고 성공 여부 반환 */
    private boolean writeLine(File file, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(line);
            bw.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 성공 코드만 전송 */
    private void send(String code) {
        try {
            out.write(code);
            out.newLine();
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 실패 코드와 이유 전송 */
    private void sendFail(String reason) {
        send("REGISTER_FAIL:" + reason);
    }
}