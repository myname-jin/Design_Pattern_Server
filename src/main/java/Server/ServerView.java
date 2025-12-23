/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.net.InetAddress;

public class ServerView {
    public void printServerInfo(int port) {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            System.out.println("서버 실행됨. IP 주소: " + ip + ", 포트: " + port);
        } catch (Exception e) {
            System.out.println("IP 확인 실패: " + e.getMessage());
        }
    }

    public void printClientConnected(String ip) {
        System.out.println("클라이언트 접속: " + ip);
    }
}
