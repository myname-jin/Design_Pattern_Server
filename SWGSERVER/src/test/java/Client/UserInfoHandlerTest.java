/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Client;

import org.junit.jupiter.api.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UserInfoHandlerTest {
    private static final String RESOURCE_DIR = "src/main/resources";
    private static final String INFO_FILE = RESOURCE_DIR + "/USER_INFO.txt";

    private ByteArrayOutputStream baos;
    private BufferedWriter out;
    private UserInfoHandler handler;

    @BeforeEach
    void setUp() throws IOException {
        // 리소스 폴더 준비
        Path dir = Path.of(RESOURCE_DIR);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // out 스트림 및 핸들러 초기화 (Socket은 쓰이지 않으므로 null 전달)
        baos = new ByteArrayOutputStream();
        out  = new BufferedWriter(new OutputStreamWriter(baos));
        handler = new UserInfoHandler(null, out);
    }

    @AfterEach
    void tearDown() {
        // 테스트 중 생성된 파일 삭제
        new File(INFO_FILE).delete();
    }

    @Test
    void whenUserExists_thenReturnsInfoResponse() throws IOException {
        // GIVEN: USER_INFO.txt 에 사용자 정보 한 줄 작성
        String line = "student1,passwd,홍길동,컴퓨터공학,학생";
        Files.writeString(Path.of(INFO_FILE), line + System.lineSeparator());

        // WHEN: 해당 ID로 요청
        handler.handle("INFO_REQUEST:student1");

        // THEN: INFO_RESPONSE:<id>,<name>,<dept>,<role>\n 반환
        out.flush();
        String resp = baos.toString();
        assertEquals("INFO_RESPONSE:student1,홍길동,컴퓨터공학,학생\n", resp);
    }

    @Test
    void whenUserNotFound_thenReturnsNotFound() throws IOException {
        // GIVEN: USER_INFO.txt 에 다른 사용자만
        String line = "other,xxx,Kim,EE,교수";
        Files.writeString(Path.of(INFO_FILE), line + System.lineSeparator());

        // WHEN: 존재하지 않는 ID 요청
        handler.handle("INFO_REQUEST:student1");

        // THEN: INFO_RESPONSE:NOT_FOUND\n 반환
        out.flush();
        String resp = baos.toString();
        assertEquals("INFO_RESPONSE:NOT_FOUND\n", resp);
    }

    @Test
    void whenFileMissing_thenReturnsNotFound() throws IOException {
        // GIVEN: 파일이 아예 없는 상태
        new File(INFO_FILE).delete();

        // WHEN: 어떤 ID 요청
        handler.handle("INFO_REQUEST:anyone");

        // THEN: INFO_RESPONSE:NOT_FOUND\n 반환
        out.flush();
        String resp = baos.toString();
        assertEquals("INFO_RESPONSE:NOT_FOUND\n", resp);
    }

    @Test
    void whenBadMessage_thenNothingSent() throws IOException {
        // WHEN: 잘못된 메시지
        handler.handle("BAD_REQUEST");

        // THEN: 스트림에 아무것도 기록되지 않아야 함
        out.flush();
        assertTrue(baos.toString().isEmpty());
    }
}