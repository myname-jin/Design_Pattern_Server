/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class ServerMainTest {

    static Thread serverThread;

    @BeforeAll
    static void startServer() throws InterruptedException {
        serverThread = new Thread(() -> {
            try {
                ServerMain.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true); // JVM 종료 시 함께 종료
        serverThread.start();
        
        // 서버가 정상적으로 시작될 시간을 기다림
        Thread.sleep(2000);
    }

    @AfterAll
    static void stopServer() {
        serverThread.interrupt();  // 종료 요청
    }

    @Test
    void testServerAcceptsConnection() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", 5000)) {
            assertTrue(socket.isConnected(), "서버 연결에 실패하였습니다.");
        }
    }
}