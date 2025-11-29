/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class StatePatternIntegrationTest {

    private ServerSocket serverSocket;
    private Socket clientSideSocket;
    private Socket serverSideSocket;
    private ClientHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        serverSocket = new ServerSocket(0); // 랜덤 포트
        int port = serverSocket.getLocalPort();

        new Thread(() -> {
            try {
                clientSideSocket = new Socket("localhost", port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        serverSideSocket = serverSocket.accept();

        // 1. SessionManager Mock (로그인 무조건 성공)
        SessionManager mockSessionManager = new SessionManager(100) { 
            @Override
            public LoginDecision tryLogin(String userId, PendingClient client) {
                return LoginDecision.OK; 
            }
            @Override
            public void logout(String userId) {
                System.out.println("[Test] 로그아웃 처리됨");
            }
        };

        // 2. LoginAttemptManager Mock (잠금 없음)
        // (싱글톤 생성자가 private이라도 이 테스트용으로 간단한 익명클래스나 null 처리가 필요할 수 있으나,
        //  기존 로직상 getInstance()를 쓰거나 여기서만 동작하게 우회)
        LoginAttemptManager realLoginManager = LoginAttemptManager.getInstance();

        // 3. ClientHandler 생성 (파일 읽기 로직 오버라이딩)
        handler = new ClientHandler(serverSideSocket, mockSessionManager, realLoginManager) {
            @Override
            public boolean validateLogin(String userId, String password, String role) {
                return "testUser".equals(userId) && "1234".equals(password);
            }
        };

        handler.start();
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (clientSideSocket != null) clientSideSocket.close();
        if (serverSideSocket != null) serverSideSocket.close();
        if (serverSocket != null) serverSocket.close();
    }

    @Test
    public void testFullStateLifecycle() throws IOException, InterruptedException {
        System.out.println("=== [통합 테스트] 상태 패턴 전체 수명주기 검증 ===");
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSideSocket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSideSocket.getInputStream()));

        // Step 1: [GuestState] 로그인 전 파일 전송 시도 -> 차단 예상
        System.out.println("Step 1: 비로그인 상태에서 파일 전송 시도");
        writer.write("FILE_UPDATE:hack.txt");
        writer.newLine();
        writer.flush();
        
        String res1 = reader.readLine();
        System.out.println("응답 1: " + res1);
        assertTrue(res1.startsWith("ERROR"), "초기 상태는 Guest여야 하며 파일 전송을 막아야 함");

        // Step 2: [Transition] 로그인 시도 -> 성공 및 MemberState 전환 예상
        System.out.println("\nStep 2: 로그인 시도 (State 전환 트리거)");
        writer.write("LOGIN:testUser,1234,student");
        writer.newLine();
        writer.flush();

        String res2 = reader.readLine();
        System.out.println("응답 2: " + res2);
        assertEquals("LOGIN_SUCCESS", res2, "로그인이 성공해야 함");

        // Step 3: [MemberState] 로그인 상태에서 재로그인 시도 -> 차단 예상
        System.out.println("\nStep 3: 로그인 상태에서 중복 로그인 시도");
        writer.write("LOGIN:testUser,1234,student");
        writer.newLine();
        writer.flush();

        String res3 = reader.readLine();
        System.out.println("응답 3: " + res3);
        assertEquals("ALREADY_LOGGED_IN", res3, "MemberState는 중복 로그인을 막아야 함");

        // Step 4: [Transition] 로그아웃 시도 -> GuestState 복귀 예상
        System.out.println("\nStep 4: 로그아웃 시도 (State 복귀 트리거)");
        writer.write("LOGOUT:testUser");
        writer.newLine();
        writer.flush();
        
        // 로그아웃은 서버가 응답을 안 보내는 구조일 수 있으므로 잠시 대기
        Thread.sleep(500); 

        // Step 5: [GuestState] 다시 파일 전송 시도 -> 차단 예상
        System.out.println("\nStep 5: 로그아웃 후 다시 파일 전송 시도");
        writer.write("FILE_UPDATE:hack_again.txt");
        writer.newLine();
        writer.flush();

        String res5 = reader.readLine();
        System.out.println("응답 5: " + res5);
        assertTrue(res5.startsWith("ERROR"), "로그아웃 후에는 다시 GuestState로 돌아와야 함");
        
        System.out.println("\n 모든 상태 전환(Guest <-> Member) 검증 완료!");
    }
}