/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Client;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegisterHandlerTest {
    private static final String RESOURCE_DIR = "src/main/resources";
    private BufferedWriter out;
    private ByteArrayOutputStream baos;
    private RegisterHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        // 리소스 폴더 준비
        File dir = new File(RESOURCE_DIR);
        if (!dir.exists()) dir.mkdirs();

        // out 스트림과 handler 초기화
        baos = new ByteArrayOutputStream();
        out  = new BufferedWriter(new OutputStreamWriter(baos));
        handler = new RegisterHandler(out);
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 생성된 파일 삭제
        deleteQuietly("ADMIN_LOGIN.txt");
        deleteQuietly("USER_LOGIN.txt");
        deleteQuietly("USER_INFO.txt");
    }

    @Test
    void testAdminRegistration() throws IOException {
        // 관리자 등록 메시지
        handler.handle("REGISTER:admin:john:pass");
        out.flush();

        // 응답 확인
        String resp = baos.toString().trim();
        assertEquals("REGISTER_SUCCESS", resp);

        // ADMIN_LOGIN.txt에 기록되었는지 확인
        File adminFile = new File(RESOURCE_DIR, "ADMIN_LOGIN.txt");
        assertTrue(adminFile.exists(), "ADMIN_LOGIN.txt가 생성되어야 합니다.");
        List<String> lines = Files.readAllLines(adminFile.toPath());
        assertTrue(lines.contains("john,pass"));
    }

    @Test
    void testUserRegistration() throws IOException {
        // 학생(또는 교수) 등록 메시지
        handler.handle("REGISTER:학생:jane:pwd:Jane Doe:CS");
        out.flush();

        // 응답 확인
        String resp = baos.toString().trim();
        assertEquals("REGISTER_SUCCESS", resp);

        // USER_LOGIN.txt에 기록
        File loginFile = new File(RESOURCE_DIR, "USER_LOGIN.txt");
        assertTrue(loginFile.exists(), "USER_LOGIN.txt가 생성되어야 합니다.");
        List<String> loginLines = Files.readAllLines(loginFile.toPath());
        assertTrue(loginLines.contains("jane,pwd"));

        // USER_INFO.txt에 기록
        File infoFile = new File(RESOURCE_DIR, "USER_INFO.txt");
        assertTrue(infoFile.exists(), "USER_INFO.txt가 생성되어야 합니다.");
        List<String> infoLines = Files.readAllLines(infoFile.toPath());
        assertTrue(infoLines.contains("jane,pwd,Jane Doe,CS,학생"));
    }

    @Test
    void testInvalidFormat() throws IOException {
        handler.handle("BAD_FORMAT");
        out.flush();

        String resp = baos.toString().trim();
        assertTrue(resp.startsWith("REGISTER_FAIL:"), "INVALID_FORMAT 에러를 반환해야 합니다.");
    }

    // 파일 삭제 헬퍼
    private void deleteQuietly(String fileName) {
        File f = new File(RESOURCE_DIR, fileName);
        if (f.exists()) f.delete();
    }
}