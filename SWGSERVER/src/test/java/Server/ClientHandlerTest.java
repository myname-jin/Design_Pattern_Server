/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandlerTest {

    private ServerSocket serverSocket;
    private Socket clientSideSocket;
    private Socket serverSideSocket;
    private ClientHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        serverSocket = new ServerSocket(0);
        int port = serverSocket.getLocalPort();

        new Thread(() -> {
            try {
                clientSideSocket = new Socket("localhost", port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        serverSideSocket = serverSocket.accept();

       
        SessionManager mockSessionManager = new SessionManager(100) { 
            @Override
            public LoginDecision tryLogin(String userId, PendingClient client) {
                return LoginDecision.OK; // 무조건 로그인 성공 처리
            }
            @Override
            public void logout(String userId) {
            }
        };

      
        LoginAttemptManager realLoginManager = LoginAttemptManager.getInstance();

        
        handler = new ClientHandler(serverSideSocket, mockSessionManager, realLoginManager) {
            @Override
            public boolean validateLogin(String userId, String password, String role) {
                // 파일 읽기 로직만 테스트용으로 교체
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
    public void testGuestStateBlocksFileUpdate() throws IOException {
        System.out.println("[Test 1] 로그인 전 파일 전송 차단 검증");
        
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSideSocket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSideSocket.getInputStream()));

        writer.write("FILE_UPDATE:hack.txt");
        writer.newLine();
        writer.flush();

        String response = reader.readLine();
        System.out.println("응답: " + response);
        
        assertTrue(response != null && response.startsWith("ERROR"), "로그인 전에는 파일 전송이 차단되어야 합니다.");
    }

    @Test
    public void testLoginAndStateTransition() throws IOException {
        System.out.println("[Test 2] 로그인 성공 및 상태 전환 검증");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSideSocket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSideSocket.getInputStream()));

        writer.write("LOGIN:testUser,1234,student");
        writer.newLine();
        writer.flush();

        String loginResponse = reader.readLine();
        assertEquals("LOGIN_SUCCESS", loginResponse, "로그인이 성공해야 합니다.");

        writer.write("LOGIN:testUser,1234,student");
        writer.newLine();
        writer.flush();

        String dupResponse = reader.readLine();
        assertEquals("ALREADY_LOGGED_IN", dupResponse, "로그인 후에는 재로그인이 차단되어야 합니다.");
    }
}