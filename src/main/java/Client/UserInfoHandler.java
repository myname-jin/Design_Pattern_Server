/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *회원가입 했을때 예약에서 읽기
 * @author adsd3
 */
import java.io.*;
import java.net.Socket;

public class UserInfoHandler {
    private final BufferedWriter out;
    public UserInfoHandler(Socket s, BufferedWriter out) { this.out = out; }

    public void handle(String msg) throws IOException {
        String targetId = msg.substring("INFO_REQUEST:".length()).trim();
        // ★ 수정됨: member.txt 사용
        File file = new File("src/main/resources/member.txt");
        
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] p = line.split(",");
                    // 포맷: id(0), pw(1), name(2), dept(3), role(4)
                    if (p.length >= 5 && p[0].equals(targetId)) {
                        // 클라이언트에 보내줄 정보: id, name, dept, role
                        out.write(String.format("INFO_RESPONSE:%s,%s,%s,%s\n", p[0], p[2], p[3], p[4]));
                        out.flush();
                        return;
                    }
                }
            }
        }
        out.write("INFO_RESPONSE:NOT_FOUND\n"); out.flush();
    }
}