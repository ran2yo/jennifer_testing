package com.example.jennifer_adapter.service;

import org.hibernate.jdbc.BatchFailedException;
import org.hibernate.service.spi.ServiceException;


import java.net.SocketException;
import java.util.Arrays;

public enum EventType {
    HTTP_IO_EXCEPTION("HTTP_IO_EXCEPTION", EventService::triggerHttpIOException, java.io.IOException.class),
    HTTP_404_ERROR("HTTP_404_ERROR", EventService::triggerHttp404, java.io.IOException.class),
    OUT_OF_MEMORY("OUT_OF_MEMORY", EventService::triggerOutOfMemory, OutOfMemoryError.class),
    SERVICE_EXCEPTION("SERVICE_EXCEPTION", EventService::throwServiceException, ServiceException.class),
    SERVICE_ERROR("SERVICE_ERROR", EventService::triggerServiceError, Error.class),
   // DB_CONNECTION_UNCLOSED("DB_CONNECTION_UNCLOSED", EventService::triggerDBConnectionUnclose, java.sql.SQLException.class),
   // DB_STATEMENT_UNCLOSED("DB_STATEMENT_UNCLOSED", EventService::triggerDbStatementUnclosed, java.sql.SQLException.class),
   // DB_RESULTSET_UNCLOSED("DB_RESULTSET_UNCLOSED", EventService::triggerDbResultSetUnclosed, java.sql.SQLException.class),
    DEADLOCK("DEADLOCK", EventService::triggerDeadlock, Exception.class),
    METHOD_EXCEPTION("METHOD_EXCEPTION", EventService::triggerMethodException, UnsupportedOperationException.class),
   /* RECURSIVE_CALL("RECURSIVE_CALL",
            (svc, req, resp) -> EventService.triggerRecursiveCall(req, resp),
            StackOverflowError.class),*/

    SQL_EXCEPTION("SQL_EXCEPTION", EventService::triggerSqlException, java.sql.SQLException.class),
    EXTERNALCALL_EXCEPTION("EXTERNALCALL_EXCEPTION", EventService::triggerExternalCallException, java.io.IOException.class),
 //   DB_CONNECTION_FAIL("DB_CONNECTION_FAIL", EventService::triggerDbConnectionFail, java.sql.SQLException.class),
  //  DB_UN_COMMIT_ROLLBACK("DB_UN_COMMIT_ROLLBACK", EventService::triggerDbUnCommitRollback, java.sql.SQLException.class),
   // DB_CONNECTION_ILLEGAL_ACCESS("DB_CONNECTION_ILLEGAL_ACCESS", EventService::triggerIllegalDbAccess, java.sql.SQLException.class),
    SOCKET_EXCEPTION("SOCKET_EXCEPTION", EventService::triggerSocketException, SocketException.class);
 //   BATCH_EXCEPTION("BATCH_EXCEPTION", EventService::triggerBatchException, BatchFailedException.class);

    private final String key;
    private final ThrowingConsumer<EventService> simpleAction;
    //private final ThrowingBiConsumer<EventService, HttpServletRequest, HttpServletResponse> servletAction;

    private final Class<? extends Throwable> exceptionClass;

    // 단순 이벤트용
    EventType(String key,
              ThrowingConsumer<EventService> action,
              Class<? extends Throwable> exceptionClass) {
        this.key = key;
        this.simpleAction = action; // ✅ 필드 이름 일치!
     //   this.servletAction = null;  // 명시적으로 null 할당
        this.exceptionClass = exceptionClass;
    }

/*    //서블릿 요청 포함 이벤트용
    EventType(String key, ThrowingBiConsumer<EventService, HttpServletRequest, HttpServletResponse> action, Class<? extends Throwable> ex) {
        this.key = key;
        this.simpleAction = null;
        this.servletAction = action;
        this.exceptionClass = ex;
    }*/

    public static EventType fromKey(String key) {
        return Arrays.stream(values())
                .filter(e -> e.key.equalsIgnoreCase(key))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 예외 타입: " + key));
    }

    public String getKey() {
        return key;
    }

    /*public void execute(EventService service) throws Throwable {
        action.accept(service);
    }*/

    public void execute(EventService service) throws Throwable {
        if (simpleAction != null) {
            simpleAction.accept(service);
        } else {
            throw new IllegalStateException("이벤트는 HttpServletRequest/Response가 필요합니다.");
        }
    }

/*    public void execute(EventService service, HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        if (servletAction != null) {
            servletAction.accept(service, req, resp);
        } else {
            throw new IllegalStateException("이벤트는 단순 실행만 가능합니다.");
        }
    }*/

    public Class<? extends Throwable> getExceptionClass() {
        return exceptionClass;
    }
}

@FunctionalInterface
interface ThrowingConsumer<T> {
    void accept(T t) throws Throwable;
}

@FunctionalInterface
interface ThrowingBiConsumer<T, U, E> {
    void accept(T t, U u, E e) throws Throwable;
}