package com.baitapnhom.quanlyktx.service;

import com.baitapnhom.quanlyktx.exception.AppException;
import java.sql.*;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// Tuần tự hóa giao dịch ghi của ứng dụng, không cần thêm bảng vào CSDL.
@Component
public class WriteGuard {
    private final DataSource dataSource;
    public WriteGuard(DataSource dataSource) { this.dataSource = dataSource; }
    public void acquire() {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("WriteGuard requires a transaction");
        if (TransactionSynchronizationManager.hasResource(this)) return;
        Connection c = DataSourceUtils.getConnection(dataSource);
        try {
            String lockName;
            try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT CONCAT(LEFT(DATABASE(),40),':ktx-write')")) {
                r.next(); lockName = r.getString(1);
            }
            try (PreparedStatement s = c.prepareStatement("SELECT GET_LOCK(?,10)")) {
                s.setString(1,lockName);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next() || r.getInt(1) != 1) throw AppException.rule("Có thao tác khác đang xử lý. Vui lòng thử lại");
                }
            }
            TransactionSynchronizationManager.bindResource(this, Boolean.TRUE);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    try (PreparedStatement s = c.prepareStatement("SELECT RELEASE_LOCK(?)")) {
                        s.setString(1,lockName); s.execute();
                    } catch (SQLException e) {
                        try { c.abort(Runnable::run); } catch (SQLException ignored) { }
                    } finally { TransactionSynchronizationManager.unbindResourceIfPossible(WriteGuard.this); }
                }
            });
        } catch (SQLException e) { throw new IllegalStateException("Không lấy được khóa giao dịch",e); }
    }
}
