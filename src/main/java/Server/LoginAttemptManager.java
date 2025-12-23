/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그인 시도 및 계정 잠금을 관리하는 싱글톤 클래스 (스레드 안전)
 */
public class LoginAttemptManager {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_MINUTES = 5;

    private final ConcurrentHashMap<String, Integer> failureCounts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> lockoutTimestamps = new ConcurrentHashMap<>();

    // 싱글톤 구현
    private static class InstanceHolder {
        private static final LoginAttemptManager INSTANCE = new LoginAttemptManager();
    }

    public static LoginAttemptManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private LoginAttemptManager() {}

    /**
     * 계정이 현재 잠겨있는지 확인합니다.
     * 잠겨있지만 5분이 지났다면, 잠금을 해제합니다.
     */
    public boolean isLocked(String userId) {
        LocalDateTime lockoutTime = lockoutTimestamps.get(userId);
        if (lockoutTime == null) {
            return false; // 잠겨있지 않음
        }

        if (lockoutTime.plusMinutes(LOCKOUT_MINUTES).isBefore(LocalDateTime.now())) {
            // 5분이 지나 잠금이 풀림
            lockoutTimestamps.remove(userId);
            failureCounts.remove(userId);
            return false;
        }

        return true; // 아직 5분 안지남 (잠겨있음)
    }

    /**
     * 로그인 실패를 기록합니다. 5회 실패 시 계정을 잠급니다.
     */
    public int recordFailure(String userId) { // 1. void를 int로 변경
    int count = failureCounts.compute(userId, (key, val) -> (val == null) ? 1 : val + 1);

    if (count >= MAX_ATTEMPTS) {
        lockoutTimestamps.put(userId, LocalDateTime.now());
        failureCounts.remove(userId);
        System.out.println("[LoginAttemptManager] 계정 잠김: " + userId);
    }

    return count; // 2. 현재 횟수 반환
}

    /**
     * 로그인 성공을 기록합니다. 실패 횟수를 초기화합니다.
     */
    public void recordSuccess(String userId) {
        failureCounts.remove(userId);
        lockoutTimestamps.remove(userId); // 혹시 모를 잠금도 해제
    }
}