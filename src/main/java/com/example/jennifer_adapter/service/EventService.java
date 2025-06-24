package com.example.jennifer_adapter.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EventService {

    public void trigger(String type) throws SQLException {
        switch (type.toLowerCase()) {
            case "serviceexception" -> throwServiceException();
            case "out_of_memory" -> triggerOutOfMemory();
            case "db_connection_unclosed" -> triggerDBConnectionUnclose();
            case "db_statement_unclosed" -> triggerDbStatementUnclosed();
            case "db_resultset_unclosed" -> triggerDbResultSetUnclosed();
            case "http_id_exception" -> triggerHttpIdException();
            case "service_error" -> triggerServiceError();
            case "pjc_rejected" -> triggerThreadRejected();
            case "dbadlock" -> triggerDbDeadlock();
            case "method_exception" -> triggerMethodException();
            case "socket_exception" -> triggerSocketException();
            case "sql_toomany_fetch" -> triggerSqlTooManyFetch();
            case "externalcall_exception" -> triggerExternalCallException();
            case "db_connection_fail" -> triggerDbConnectionFail();
            case "db_un_commit_rollback" -> triggerUncommittedTransaction();
            case "db_connection_illegal_access" -> triggerIllegalDbAccess();
            case "recursive_call" -> triggerRecursiveCall();
            case "sql_exception" -> triggerSqlException();
            case "http_404_error" -> triggerHttp404();
            case "batch_exception" -> triggerBatchException();
            case "thread_kill_manual" -> triggerThreadKillManual();
            case "thread_kill_auto" -> triggerThreadKillAuto();
            default -> throw new IllegalArgumentException("지원하지 않는 예외 타입입니다: " + type);
        }
    }

    private void throwServiceException() {
        throw new RuntimeException("ServiceException: 일반적인 서비스 예외가 발생했습니다");
    }

    private void triggerOutOfMemory() {
        // 실제 OutOfMemoryError 발생
        List<byte[]> memoryEater = new ArrayList<>();
        try {
            while (true) {
                // 큰 배열을 계속 생성하여 힙 메모리 고갈
                memoryEater.add(new byte[10 * 1024 * 1024]); // 10MB씩
            }
        } catch (OutOfMemoryError e) {
            throw e; // 실제 OutOfMemoryError 재던지기
        }
    }

    private void triggerDBConnectionUnclose() {
        // 실제 DB 커넥션을 열고 닫지 않아서 리소스 누수 시뮬레이션
        try {
            for (int i = 0; i < 100; i++) {
                // H2 인메모리 DB 연결 (닫지 않음)
                Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
                // 의도적으로 close() 하지 않음
                if (i == 99) {
                    throw new SQLException("DB 커넥션 100개가 닫히지 않고 누수되었습니다");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("DB 커넥션 누수 발생", e);
        }
    }

    private void triggerDbStatementUnclosed() {
        // 실제 Statement를 열고 닫지 않는 시뮬레이션
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            for (int i = 0; i < 50; i++) {
                Statement stmt = conn.createStatement();
                // 의도적으로 close() 하지 않음
            }
            conn.close();
            throw new SQLException("Statement 50개가 닫히지 않고 누수되었습니다");
        } catch (SQLException e) {
            throw new RuntimeException("Statement 누수 발생", e);
        }
    }

    private void triggerDbResultSetUnclosed() {
        // 실제 ResultSet을 열고 닫지 않는 시뮬레이션
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS test (id INT)");
            stmt.execute("INSERT INTO test VALUES (1)");

            for (int i = 0; i < 30; i++) {
                ResultSet rs = stmt.executeQuery("SELECT * FROM test");
                // 의도적으로 close() 하지 않음
            }
            stmt.close();
            conn.close();
            throw new SQLException("ResultSet 30개가 닫히지 않고 누수되었습니다");
        } catch (SQLException e) {
            throw new RuntimeException("ResultSet 누수 발생", e);
        }
    }

    private void triggerHttpIdException() {
        // 실제 HTTP 파라미터 파싱 에러
        String invalidId = "abc123!@#";
        try {
            Integer.parseInt(invalidId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("HTTP 파라미터 ID 변환 실패: " + invalidId, e);
        }
    }

    private void triggerServiceError() {
        // 실제 서비스 로직 에러 시뮬레이션
        throw new RuntimeException("서비스 내부 처리 중 치명적 오류 발생: 데이터 무결성 위반");
    }

    private void triggerThreadRejected() {
        // 실제 ThreadPool 거부 상황 만들기
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), // 대기열 크기 0
                new ThreadPoolExecutor.AbortPolicy() // 거부 정책
        );

        try {
            // 첫 번째 작업으로 스레드 풀 점유
            executor.submit(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            // 두 번째 작업은 거부됨 (RejectedExecutionException 발생)
            executor.submit(() -> System.out.println("이 작업은 실행되지 않습니다"));

        } finally {
            executor.shutdown();
        }
    }

    private void triggerDbDeadlock() throws SQLException {
        // 실제 데드락 시뮬레이션
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(2);
        AtomicReference<Exception> deadlockException = new AtomicReference<>();

        try {
            // 트랜잭션 1
            Future<?> task1 = executor.submit(() -> {
                try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")) {
                    conn.setAutoCommit(false);
                    Statement stmt = conn.createStatement();
                    stmt.execute("CREATE TABLE IF NOT EXISTS table_a (id INT PRIMARY KEY, value INT)");
                    stmt.execute("CREATE TABLE IF NOT EXISTS table_b (id INT PRIMARY KEY, value INT)");
                    stmt.execute("INSERT INTO table_a VALUES (1, 0) ON DUPLICATE KEY UPDATE value = value");
                    stmt.execute("INSERT INTO table_b VALUES (1, 0) ON DUPLICATE KEY UPDATE value = value");

                    stmt.executeUpdate("UPDATE table_a SET value = 1 WHERE id = 1");
                    startLatch.countDown();
                    startLatch.await();
                    Thread.sleep(100);
                    stmt.executeUpdate("UPDATE table_b SET value = 1 WHERE id = 1");
                    conn.commit();
                } catch (Exception e) {
                    deadlockException.set(e);
                }
            });

            // 트랜잭션 2
            Future<?> task2 = executor.submit(() -> {
                try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")) {
                    conn.setAutoCommit(false);
                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("UPDATE table_b SET value = 2 WHERE id = 1");
                    startLatch.countDown();
                    startLatch.await();
                    Thread.sleep(100);
                    stmt.executeUpdate("UPDATE table_a SET value = 2 WHERE id = 1");
                    conn.commit();
                } catch (Exception e) {
                    deadlockException.set(e);
                }
            });

            task1.get(3, TimeUnit.SECONDS);
            task2.get(3, TimeUnit.SECONDS);

        } catch (TimeoutException e) {
            throw new SQLException("데이터베이스 데드락이 발생했습니다 (타임아웃)", "40001");
        } catch (Exception e) {
            if (deadlockException.get() != null) {
                throw new RuntimeException("데드락 발생", deadlockException.get());
            }
            throw new RuntimeException("데드락 시뮬레이션 실패", e);
        } finally {
            executor.shutdownNow();
        }
    }

    private void triggerMethodException() {
        // 실제 지원되지 않는 메서드 호출
        List<String> unmodifiableList = List.of("item1", "item2");
        try {
            unmodifiableList.add("item3"); // UnsupportedOperationException 발생
        } catch (UnsupportedOperationException e) {
            throw e;
        }
    }

    private void triggerSocketException() {
        // 실제 소켓 연결 실패
        try {
            Socket socket = new Socket("192.0.2.1", 80); // 존재하지 않는 IP
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("소켓 연결 실패: " + e.getMessage(), e);
        }
    }

    private void triggerSqlTooManyFetch() {
        // 실제 대량 데이터 fetch 시뮬레이션
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")) {
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS big_table (id INT, data VARCHAR(1000))");

            // 대량 데이터 삽입
            for (int i = 0; i < 10000; i++) {
                stmt.execute("INSERT INTO big_table VALUES (" + i + ", '" + "x".repeat(1000) + "')");
            }

            // 한 번에 모든 데이터 fetch (메모리 부족 유발)
            ResultSet rs = stmt.executeQuery("SELECT * FROM big_table");
            List<String> data = new ArrayList<>();
            while (rs.next()) {
                data.add(rs.getString("data"));
            }

            throw new SQLException("SQL에서 " + data.size() + "건의 대용량 데이터를 fetch했습니다");

        } catch (SQLException e) {
            throw new RuntimeException("SQL 대량 fetch 오류", e);
        }
    }

    private void triggerExternalCallException() {
        // 실제 외부 API 호출 실패
        try {
            URL url = new URL("http://httpstat.us/500"); // 500 에러 반환하는 테스트 서비스
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                throw new IOException("외부 API 호출 실패: HTTP " + responseCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("외부 서비스 호출 실패", e);
        }
    }

    private void triggerDbConnectionFail() {
        // 실제 DB 연결 실패
        try {
            DriverManager.getConnection("jdbc:mysql://nonexistent-host:3306/db", "user", "pass");
        } catch (SQLException e) {
            throw new RuntimeException("데이터베이스 연결 실패", e);
        }
    }

    private void triggerUncommittedTransaction() {
        // 실제 트랜잭션 커밋 누락
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")) {
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS transaction_test (id INT, value VARCHAR(50))");
            stmt.executeUpdate("INSERT INTO transaction_test VALUES (1, 'uncommitted_data')");

            // 의도적으로 commit() 하지 않고 종료
            // 연결이 닫히면서 자동 rollback됨
            throw new SQLException("트랜잭션이 커밋되지 않아 롤백되었습니다");

        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 커밋 누락", e);
        }
    }

    private void triggerIllegalDbAccess() {
        // 실제 권한 없는 DB 접근 시뮬레이션
        try {
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            Statement stmt = conn.createStatement();

            // 존재하지 않는 테이블 접근
            stmt.executeQuery("SELECT * FROM non_existent_secure_table");

        } catch (SQLException e) {
            throw new SecurityException("데이터베이스 불법 접근 감지: " + e.getMessage(), e);
        }
    }

    private void triggerRecursiveCall() {
        // 실제 무한 재귀 호출로 StackOverflowError 발생
        triggerRecursiveCall();
    }

    private void triggerSqlException() {
        // 실제 SQL 문법 오류
        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "")) {
            Statement stmt = conn.createStatement();
            stmt.executeQuery("SELCT * FORM invalid_syntax_table"); // 의도적 문법 오류
        } catch (SQLException e) {
            throw new RuntimeException("SQL 문법 오류", e);
        }
    }

    private void triggerHttp404() {
        // 실제 404 에러 발생
        try {
            URL url = new URL("http://httpstat.us/404");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();

            if (responseCode == 404) {
                throw new RuntimeException("HTTP 404 Not Found 오류가 발생했습니다");
            }
        } catch (IOException e) {
            throw new RuntimeException("HTTP 404 시뮬레이션 중 오류", e);
        }
    }

    private void triggerBatchException() {
        // 실제 배치 처리 중 데이터 포맷 오류
        String[] invalidData = {"123", "abc", "456", "def"};
        List<Integer> processedData = new ArrayList<>();

        try {
            for (String data : invalidData) {
                processedData.add(Integer.parseInt(data)); // NumberFormatException 발생
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("배치 처리 중 데이터 포맷 오류: " + e.getMessage(), e);
        }
    }

    private void triggerThreadKillManual() {
        // 실제 스레드 인터럽트
        Thread currentThread = Thread.currentThread();
        currentThread.interrupt();

        try {
            Thread.sleep(1000); // InterruptedException 발생
        } catch (InterruptedException e) {
            throw new RuntimeException("스레드가 수동으로 중단되었습니다", e);
        }
    }

    private void triggerThreadKillAuto() {
        // JVM 강제 종료 (실제 서버 종료됨!)
        Runtime.getRuntime().halt(1);
    }
}
