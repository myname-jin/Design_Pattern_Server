/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public interface ClientState {
    // 모든 상태가 구현해야 할 공통 메서드
    void process(ClientHandler context, String msg, BufferedReader in, BufferedWriter out) throws IOException;
}