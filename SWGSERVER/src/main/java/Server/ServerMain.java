/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.io.*;
import java.net.*;
import java.util.*;
import Server.SessionManager;
import Server.ClientHandler;

public class ServerMain {

    public static final int PORT = 5000;

    public static void main(String[] args) {
        // 외부 IP 출력
        try (InputStream is = new URL("https://api.ipify.org").openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            System.out.println("외부 접속용 공인 IP: " + br.readLine());
        } catch (IOException e) {
            System.out.println("공인 IP를 가져올 수 없습니다.");
        }

        // 내부 IP 출력
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress inet = addr.getAddress();
                    if (inet instanceof Inet4Address) {
                        System.out.println("내부 IP: " + inet.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // 세션 매니저 생성 (최대 3명)
        SessionManager sessionManager = new SessionManager(3);

        // ServerSocket 바인딩 시도
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("서버 시작됨. 포트 " + PORT + " 대기 중...");
        } catch (BindException e) {
            // 포트가 이미 열려 있으면 기존 서버 사용
            System.out.println("포트 " + PORT + " 이미 사용 중, 기존 서버 유지");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 클라이언트 연결 처리 루프
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, sessionManager)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}