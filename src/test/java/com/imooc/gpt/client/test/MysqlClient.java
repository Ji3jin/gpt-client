package com.imooc.gpt.client.test;

import cn.hutool.json.JSONUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public String executeQuery(String sql) throws SQLException {
        Map<String, List<String>> resultMap = new HashMap<>();
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            ResultSetMetaData rsmd = resultSet.getMetaData();
            int columnCount = rsmd.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsmd.getColumnName(i);
                List<String> columnData = new ArrayList<>();
                while (resultSet.next()) {
                    String value = resultSet.getString(columnName);
                    columnData.add(value);
                }
                resultMap.put(columnName, columnData);
            }
        } catch (SQLException e) {
            throw e;
        }

        return JSONUtil.toJsonStr(resultMap);
    }


    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
