/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
/*package Server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

public class LoginProcessorTest {

    private static final String USER_FILE = "src/main/resources/USER_LOGIN.txt";
    private static final String ADMIN_FILE = "src/main/resources/ADMIN_LOGIN.txt";

    @BeforeEach
    void setup() throws IOException {
        new File(USER_FILE).getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            writer.write("testuser,testpw\n");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ADMIN_FILE))) {
            writer.write("admin01,adminpw\n");
        }
    }

    @Test
    void testValidUserLogin() {
        LoginProcessor processor = new LoginProcessor(null); // SessionManager 없이 null 전달
        boolean result = processor.validateLogin("testuser", "testpw", "user");
        assertTrue(result);
    }

    @Test
    void testInvalidUserLogin() {
        LoginProcessor processor = new LoginProcessor(null);
        boolean result = processor.validateLogin("testuser", "wrongpw", "user");
        assertFalse(result);
    }

    @Test
    void testValidAdminLogin() {
        LoginProcessor processor = new LoginProcessor(null);
        boolean result = processor.validateLogin("admin01", "adminpw", "admin");
        assertTrue(result);
    }

    @Test
    void testInvalidAdminLogin() {
        LoginProcessor processor = new LoginProcessor(null);
        boolean result = processor.validateLogin("admin01", "wrongpw", "admin");
        assertFalse(result);
    }
}*/