/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Client;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class RegisterHandlerTest {

    private static final String FILE_PATH = "src/main/resources/member.txt";
    private StringWriter stringWriter;
    private BufferedWriter bufferedWriter;
    private RegisterHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 전 기존 파일 삭제 및 초기화
        Files.deleteIfExists(Paths.get(FILE_PATH));
        
        // Mocking BufferedWriter using StringWriter
        stringWriter = new StringWriter();
        bufferedWriter = new BufferedWriter(stringWriter);
        handler = new RegisterHandler(bufferedWriter);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 테스트 후 파일 정리
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }

    @Test
    void testAdminRegistration() throws IOException {
        // Given: 관리자 등록 메시지 (형식: REGISTER:role:id:pw:name:dept)
        String msg = "REGISTER:Admin:admin1:1234:AdminName:Management";

        // When
        handler.handle(msg);

        // Then: 1. 응답 메시지 확인
        bufferedWriter.flush();
        assertTrue(stringWriter.toString().contains("REGISTER_SUCCESS"));

        // Then: 2. 파일 생성 및 내용 확인 (member.txt에 저장되었는지)
        File file = new File(FILE_PATH);
        assertTrue(file.exists(), "member.txt 파일이 생성되어야 합니다.");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            assertNotNull(line);
            // 저장 포맷: id,pw,name,dept,role
            assertEquals("admin1,1234,AdminName,Management,Admin", line);
        }
    }

    @Test
    void testUserRegistration() throws IOException {
        // Given: 일반 유저 등록 메시지
        String msg = "REGISTER:Student:user1:1234:UserName:CSE";

        // When
        handler.handle(msg);

        // Then
        bufferedWriter.flush();
        assertTrue(stringWriter.toString().contains("REGISTER_SUCCESS"));

        File file = new File(FILE_PATH);
        assertTrue(file.exists(), "member.txt 파일이 생성되어야 합니다.");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            // 저장 포맷: id,pw,name,dept,role
            assertEquals("user1,1234,UserName,CSE,Student", line);
        }
    }

    @Test
    void testDuplicateId() throws IOException {
        // Given: 이미 존재하는 ID 파일 생성
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            bw.write("existingUser,1234,OldName,Dept,Student");
            bw.newLine();
        }

        // When: 같은 ID로 가입 시도
        String msg = "REGISTER:Student:existingUser:9999:NewName:NewDept";
        handler.handle(msg);

        // Then: 실패 메시지 확인
        bufferedWriter.flush();
        assertTrue(stringWriter.toString().contains("REGISTER_FAIL:DUPLICATE_ID"));
    }
}