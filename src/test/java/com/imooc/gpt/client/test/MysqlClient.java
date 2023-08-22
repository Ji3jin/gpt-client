package com.imooc.gpt.client.test;

import java.sql.*;

public class MysqlClient {
    private static MysqlClient instance;
    private Connection conn;

    private MysqlClient() {

    }

    public static MysqlClient getInstance() {
        if (instance == null) {
            synchronized (MysqlClient.class) {
                if (instance == null) {
                    instance = new MysqlClient();
                }
            }
        }
        return instance;
    }

    public void open() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "test", "test");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the given SQL query and returns the result as a string.
     *
     * @param sql the SQL query to execute
     * @return a string representing the result of the query
     * @throws SQLException if an error occurs while executing the query
     */
    public String executeQuery(String sql) {
        StringBuilder result = new StringBuilder();
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                result.append(resultSet.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result.toString();
    }

    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
