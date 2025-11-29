/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GuestState 단위 테스트
 * 목적: 로그인 전 상태에서 허용되지 않은 명령이 제대로 차단되는지 검증
 */
public class GuestStateTest {

    @Test
    public void testBlockFileUpdate() throws IOException {
        System.out.println("[Test] GuestState - 파일 전송 시도 차단 검증");

        // 1. 준비 (Mocking I/O)
        // GuestState는 차단 로직에서 Context를 사용하지 않으므로 null로 둡니다.
        ClientHandler context = null; 
        String msg = "FILE_UPDATE:test.txt";
        
        BufferedReader in = new BufferedReader(new StringReader(""));
        StringWriter outBuffer = new StringWriter();
        BufferedWriter out = new BufferedWriter(outBuffer);

        // 2. 실행
        GuestState instance = new GuestState();
        instance.process(context, msg, in, out);
        out.flush();

        // 3. 검증
        String result = outBuffer.toString().trim();
        System.out.println("결과: " + result);
        
        // "ERROR"로 시작하는 메시지가 와야 성공
        assertTrue(result.startsWith("ERROR"), "로그인 전 파일 전송은 차단되어야 합니다.");
    }

    @Test
    public void testBlockLogout() throws IOException {
        System.out.println("[Test] GuestState - 로그아웃 시도 차단 검증");

        ClientHandler context = null;
        String msg = "LOGOUT:user1";
        
        BufferedReader in = new BufferedReader(new StringReader(""));
        StringWriter outBuffer = new StringWriter();
        BufferedWriter out = new BufferedWriter(outBuffer);

        GuestState instance = new GuestState();
        instance.process(context, msg, in, out);
        out.flush();

        String result = outBuffer.toString().trim();
        System.out.println("결과: " + result);

        assertTrue(result.startsWith("ERROR"), "비회원은 로그아웃을 할 수 없습니다.");
    }
}