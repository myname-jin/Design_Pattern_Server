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
 * MemberState 단위 테스트
 * 목적: 로그인 후 상태에서 중복 로그인/가입 시도가 차단되는지 검증
 */
public class MemberStateTest {

    @Test
    public void testBlockDuplicateLogin() throws IOException {
        System.out.println("[Test] MemberState - 중복 로그인 시도 차단 검증");

        // 1. 준비
        ClientHandler context = null; // 차단 로직은 Context 불필요
        String msg = "LOGIN:user,pw,role";
        
        BufferedReader in = new BufferedReader(new StringReader(""));
        StringWriter outBuffer = new StringWriter();
        BufferedWriter out = new BufferedWriter(outBuffer);

        // 2. 실행
        MemberState instance = new MemberState();
        instance.process(context, msg, in, out);
        out.flush();

        // 3. 검증
        String result = outBuffer.toString().trim();
        System.out.println("결과: " + result);

        assertEquals("ALREADY_LOGGED_IN", result, "로그인 상태에서 재로그인은 차단되어야 합니다.");
    }

    @Test
    public void testBlockRegister() throws IOException {
        System.out.println("[Test] MemberState - 회원가입 시도 차단 검증");

        ClientHandler context = null;
        String msg = "REGISTER:student:id:pw:name:dept";
        
        BufferedReader in = new BufferedReader(new StringReader(""));
        StringWriter outBuffer = new StringWriter();
        BufferedWriter out = new BufferedWriter(outBuffer);

        MemberState instance = new MemberState();
        instance.process(context, msg, in, out);
        out.flush();

        String result = outBuffer.toString().trim();
        System.out.println("결과: " + result);

        assertEquals("ALREADY_LOGGED_IN", result, "로그인 상태에서 회원가입은 차단되어야 합니다.");
    }
}