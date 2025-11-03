/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Client;


import org.junit.jupiter.api.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileSyncManagerTest {

    private final FileSyncManager syncManager = new FileSyncManager();
    private final String testFileName = "test_sync_file.txt";
    private final String testContent = "동기화 테스트입니다.";
    private final String testFilePath = "src/main/resources/" + testFileName;

    @BeforeEach
    public void setup() {
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterEach
    public void cleanup() {
        File file = new File(testFilePath);
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testUpdateFileCreatesAndWritesContent() throws IOException {
        syncManager.updateFile(testFileName, testContent);

        File file = new File(testFilePath);
        assertTrue(file.exists(), "파일이 생성되어 있어야 합니다.");

        String content = new BufferedReader(new FileReader(file)).readLine();
        assertEquals(testContent, content, "파일 내용이 입력값과 일치해야 합니다.");
    }
}
