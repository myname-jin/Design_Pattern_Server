/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandlerTest {
    private static Thread serverThread;

    @BeforeAll
    public static void startTestServer() {
        // 테스트용 스텁 서버: REGISTER 요청만 처리
        serverThread = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(ServerMain.PORT)) {
                while (true) {
                    try (Socket client = ss.accept();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()))) {
                        String msg = reader.readLine();
                        if (msg != null && msg.startsWith("REGISTER:")) {
                            String[] parts = msg.split(":", 6);
                            if (parts.length == 6) {
                                writer.write("REGISTER_SUCCESS");
                            } else {
                                writer.write("REGISTER_FAIL:INVALID_FORMAT");
                            }
                            writer.newLine();
                            writer.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @Test
    public void testInvalidRegisterFormat() throws Exception {
        try (Socket socket = new Socket("localhost", ServerMain.PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.write("REGISTER:학생");
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            Assertions.assertEquals("REGISTER_FAIL:INVALID_FORMAT", response);
        }
    }

    @Test
    public void testRegisterResponseFromServer() throws Exception {
        try (Socket socket = new Socket("localhost", ServerMain.PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            writer.write("REGISTER:학생:jane:pwd:Jane Doe:CS");
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            Assertions.assertEquals("REGISTER_SUCCESS", response);
        }
    }
}
