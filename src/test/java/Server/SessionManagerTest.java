/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.Socket;

public class SessionManagerTest {

    @Test
    public void testLoginLimitReached() throws IOException {
        SessionManager manager = new SessionManager(1);
        Socket dummySocket = new Socket();
        BufferedWriter dummyOut = new BufferedWriter(new OutputStreamWriter(System.out));
        SessionManager.PendingClient dummy = new SessionManager.PendingClient(dummySocket, "user1", dummyOut);

        SessionManager.LoginDecision result1 = manager.tryLogin("user1", dummy);
        SessionManager.LoginDecision result2 = manager.tryLogin("user2", dummy); // Should go to waiting

        Assertions.assertEquals(SessionManager.LoginDecision.OK, result1);
        Assertions.assertEquals(SessionManager.LoginDecision.WAIT, result2);
    }

    @Test
    public void testDuplicateLogin() throws IOException {
        SessionManager manager = new SessionManager(3);
        Socket dummySocket = new Socket();
        BufferedWriter dummyOut = new BufferedWriter(new OutputStreamWriter(System.out));
        SessionManager.PendingClient dummy = new SessionManager.PendingClient(dummySocket, "user1", dummyOut);

        manager.tryLogin("user1", dummy);
        SessionManager.LoginDecision result = manager.tryLogin("user1", dummy);

        Assertions.assertEquals(SessionManager.LoginDecision.FAIL_DUP, result);
    }
}
