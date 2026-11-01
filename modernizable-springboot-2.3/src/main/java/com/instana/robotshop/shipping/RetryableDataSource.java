package com.instana.robotshop.shipping;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.resilience.annotation.Retryable;

class RetryableDataSource extends AbstractDataSource {
    private DataSource delegate;

    public RetryableDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    @Retryable(maxRetries = 9, multiplier = 2.3, maxDelay = 30000)
    public Connection getConnection() throws SQLException {
        return delegate.getConnection();
    }

    @Override
    @Retryable(maxRetries = 9, multiplier = 2.3, maxDelay = 30000)
    public Connection getConnection(String username, String password) throws SQLException {
        return delegate.getConnection(username, password);
    }
}

