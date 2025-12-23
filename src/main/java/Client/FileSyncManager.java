/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

/**
 *
 * @author adsd3
 */

import java.io.*;

public class FileSyncManager {
    private final String baseDir = "src/main/resources";

    public synchronized void updateFile(String filename, String content) throws IOException {
        File file = new File(baseDir, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
        System.out.println("[서버] 파일 업데이트 완료: " + filename);
    }
}