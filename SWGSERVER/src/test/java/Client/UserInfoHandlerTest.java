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

class UserInfoHandlerTest {

    private static final String FILE_PATH = "src/main/resources/member.txt";
    private StringWriter stringWriter;
    private BufferedWriter out;
    private UserInfoHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        // 1. 테스트 데이터 파일 생성 (member.txt)
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // 테스트용 더미 데이터: id,pw,name,dept,role
            bw.write("student1,1234,홍길동,컴퓨터공학,학생");
            bw.newLine();
        }

        // 2. 결과 검증을 위한 Writer 설정
        stringWriter = new StringWriter();
        out = new BufferedWriter(stringWriter);
        
        
        handler = new UserInfoHandler(null, out);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 테스트 후 파일 정리
        Files.deleteIfExists(Paths.get(FILE_PATH));
    }

    @Test
    void whenUserExists_thenReturnsInfoResponse() throws IOException {
        // Given: 올바른 요청 (학생1 정보 요청)
        String msg = "INFO_REQUEST:student1";

        // When
        handler.handle(msg);
        out.flush();

        // Then: 응답 포맷 확인 (INFO_RESPONSE:id,name,dept,role)
        String response = stringWriter.toString().trim();
        assertEquals("INFO_RESPONSE:student1,홍길동,컴퓨터공학,학생", response);
    }

    @Test
    void whenUserNotFound_thenReturnsNotFound() throws IOException {
        // Given: 없는 ID 요청
        String msg = "INFO_REQUEST:unknownUser";

        // When
        handler.handle(msg);
        out.flush();

        // Then: NOT_FOUND 응답 확인
        String response = stringWriter.toString().trim();
        assertEquals("INFO_RESPONSE:NOT_FOUND", response);
    }
    
    @Test
    void whenBadMessage_thenHandleGracefully() {
        // Given: 형식이 잘못된 메시지 (공백 등)
        String msg = "INFO_REQUEST:   "; 
        
        // When & Then: 에러가 발생하지 않는지 확인
        assertDoesNotThrow(() -> {
            handler.handle(msg);
            out.flush();
        });
    }
}