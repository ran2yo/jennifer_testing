package com.example.jennifer_adapter.service;



import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hibernate.service.spi.ServiceException;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


import javax.sql.DataSource;
import java.io.IOException;
import java.net.ConnectException;

import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@DependsOnDatabaseInitialization
@Service
public class EventService {

    private final DataSource dataSource;


    public EventService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

   /* public static void triggerRecursiveCall(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().println("Helper starts<br>");
        RequestDispatcher dispatcher = req.getRequestDispatcher("/WEB-INF/jsp/recursive.jsp");
        dispatcher.include(req, resp);
        resp.getWriter().println("Helper ends<br>");
    }*/

    public void trigger(String type) throws Throwable {
        EventType.fromKey(type).execute(this);
    }

    public void triggerHttpIOException() throws IOException {
        throw new IOException("HTTP IO Exception 발생");
    }

    public void triggerHttp404() throws Exception {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:8090/APP/V1/inbound/jennifer/ec9999");
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if(statusCode == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "http Not Found 404" +request.getURI()
                );
            }


        }
    }

    public void triggerOutOfMemory() {
        List<byte[]> list = new ArrayList<>();
        while (true) {
            list.add(new byte[10 * 1024 * 1024]);
        }
    }

    public void throwServiceException() {
        throw new ServiceException("Service Exception 발생");
    }

    public void triggerServiceError() throws Throwable {
        throw new Error("Service Error 발생");
    }

/*
    public void triggerDBConnectionUnclose() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        for (int i = 0; i < 1; i++) {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            //Thread.sleep(1000);
            rs = stmt.executeQuery("SELECT \n" +
                    "    u.name AS user_name,\n" +
                    "    o.id AS order_id,\n" +
                    "    o.order_status,\n" +
                    "    oi.product_id,\n" +
                    "    p.name AS product_name,\n" +
                    "    oi.quantity,\n" +
                    "    oi.price\n" +
                    "FROM user u\n" +
                    "JOIN orders o ON u.id = o.user_id\n" +
                    "JOIN order_items oi ON o.id = oi.order_id\n" +
                    "JOIN products p ON oi.product_id = p.id;\n");

        }


    }
*/

/*    public void triggerDbStatementUnclosed() throws SQLException, InterruptedException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        //  for(int i =0; i<10; i++) {
        conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        stmt = conn.createStatement();
        //Thread.sleep(1000);
        rs = stmt.executeQuery("SELECT \n" +
                "    u.name AS user_name,\n" +
                "    o.id AS order_id,\n" +
                "    o.order_status,\n" +
                "    oi.product_id,\n" +
                "    p.name AS product_name,\n" +
                "    oi.quantity,\n" +
                "    oi.price\n" +
                "FROM user u\n" +
                "JOIN orders o ON u.id = o.user_id\n" +
                "JOIN order_items oi ON o.id = oi.order_id\n" +
                "JOIN products p ON oi.product_id = p.id;\n");
        //   for (int j = 0; j < 5; j++) {
        System.gc();
        Thread.sleep(1000);
        System.runFinalization();*/
        //      }

        //  }
//        long start = System.currentTimeMillis();
//          while(stmt.wait()){
//              long end = System.currentTimeMillis();
//              System.out.println("User Name: "+ rs.getString("user_name") + (end - start));
//          }

//
//          rs.close();
//          conn.close();


 //   }

 /*   public void triggerDbResultSetUnclosed() throws Exception {
        boolean closed;
        boolean checkClosed = false;
        boolean isFinalize;
        Connection conn;
        Statement stmt;
        ResultSet rs;
        conn = DriverManager.getConnection(
                "jdbc:mysql://192.168.1.89:3306/jennifer_db", "ryyoun", "Dlqslek12!@"
        );
        conn.setAutoCommit(false);
        stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT \n" +
                "    u.name AS user_name,\n" +
                "    o.id AS order_id,\n" +
                "    o.order_status,\n" +
                "    oi.product_id,\n" +
                "    p.name AS product_name,\n" +
                "    oi.quantity,\n" +
                "    oi.price\n" +
                "FROM user u\n" +
                "JOIN orders o ON u.id = o.user_id\n" +
                "JOIN order_items oi ON o.id = oi.order_id\n" +
                "JOIN products p ON oi.product_id = p.id;\n");



//            @Override
//            protected void finalize() throws Throwable {
//                System.out.println("🔥 finalize() called for LeakyWrapper!");
//                // close는 의도적으로 안 함
//                super.finalize();
//            }


        System.gc();
        Thread.sleep(1000);
        System.runFinalization();


        System.out.println("🧙‍♂️ GC 유도 완료");


    }*/

    public void triggerDeadlock() throws Exception {
        final String jdbcUrl = "jdbc:h2:mem:testdb,DB_CLOSE_DELAY=-1,MVCC=TRUN;LOCK_TIMEOUT=5000";

        //latch로 두 스레드 준비 동기화
        CountDownLatch latch = new CountDownLatch(2);
        AtomicReference<Exception> exRef = new AtomicReference<>();
        // 트랜잭션1: table_a → table_b
        Runnable task1 = () -> {
            try (Connection c = DriverManager.getConnection(jdbcUrl, "sa", "")) {
                c.setAutoCommit(false);
                // 1) table_a lock
                c.createStatement().executeUpdate("UPDATE table_a SET val = val + 1 WHERE id = 1");
                // 2) 데드락 유발 전까지 대기
                latch.await();
                // 3) table_b lock (여기서 데드락)
                c.createStatement().executeUpdate("UPDATE table_b SET val = val + 1 WHERE id = 1");
                c.commit();
            } catch (Exception e) {
                exRef.set(e);
            }
        };

        // 트랜잭션2: table_b → table_a
        Runnable task2 = () -> {
            try (Connection c = DriverManager.getConnection(jdbcUrl, "sa", "")) {
                c.setAutoCommit(false);
                c.createStatement().executeUpdate("UPDATE table_b SET val = val + 1 WHERE id = 1");
                // 트랜잭션1이 table_a lock 후 대기 시작
                latch.countDown();
                c.createStatement().executeUpdate("UPDATE table_a SET val = val + 1 WHERE id = 1");
                c.commit();
            } catch (Exception e) {
                exRef.set(e);
            }
        };

        Thread t1 = new Thread(task1), t2 = new Thread(task2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // 데드락 예외가 생겼는지 던져줌
        if (exRef.get() != null) throw exRef.get();
    }

    public void triggerMethodException() throws Exception {
      //  throw new MethodNotFoundException("Method Exception 시뮬레이션");

    }

    public void triggerSqlException() throws SQLException {
        // 직접 DriverManager로 Connection 획득
        String jdbcUrl = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "sa", "");
             Statement stmt = conn.createStatement()) {
            // 잘못된 테이블명 실행
            stmt.executeQuery("SELECT * FROM non_existent_table");
        }
    }

    public void triggerExternalCallException() throws Exception {
        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://httpbin.org/dad");
            try(var response = httpClient.execute(request)) {
                String result = EntityUtils.toString(response.getEntity());
                System.out.println("응답내용:" + result);
            }

        }
    }

/*    public void triggerDbConnectionFail() throws SQLException, InterruptedException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        conn = dataSource.getConnection();
        conn.setAutoCommit(false);
        stmt = conn.createStatement();
        //Thread.sleep(1000);
        rs = stmt.executeQuery("SELECT \n" +
                "    u.name AS user_name,\n" +
                "    o.id AS order_id,\n" +
                "    o.order_status,\n" +
                "    oi.product_id,\n" +
                "    p.name AS product_name,\n" +
                "    oi.quantity,\n" +
                "    oi.price\n" +
                "FROM user u\n" +
                "JOIN orders o ON u.id = o.user_id\n" +
                "JOIN order_items oi ON o.id = oi.order_id\n" +
                "JOIN products p ON oi.product_id = p.id;\n");
        Thread.sleep(10000);

//        long start = System.currentTimeMillis();
//          while(rs.next()){
//              long end = System.currentTimeMillis();
//              System.out.println("User Name: "+ rs.getString("user_name") + (end - start));
//
//
//          }
        stmt.close();
        rs.close();
        conn.close();


    }*/
/*
    public void triggerDbUnCommitRollback() throws SQLException {
        throw new SQLException("DB Uncommit/Rollback 시뮬레이션");
    }

    public void triggerIllegalDbAccess() throws SQLException {
        throw new SQLException("DB Connection Illegal Access 시뮬레이션");
    }*/

    public void triggerSocketException() throws SocketException {
        throw new SocketException("Socket Exception !");
    }

/*    public void triggerBatchException() {
    }*/
}