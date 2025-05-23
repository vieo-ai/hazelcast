/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hazelcast.jet.cdc;

import org.testcontainers.containers.MySQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

@SuppressWarnings("SqlResolve")
public final class MySQLTestUtils {
    private MySQLTestUtils() {
    }

    public static void runQuery(MySQLContainer<?> container, String query) {
        try (Connection connection = getMySqlConnection(container.getJdbcUrl(), container.getUsername(),
                container.getPassword())) {
            connection.setSchema("inventory");
            try (Statement statement = connection.createStatement()) {
                //noinspection SqlSourceToSinkFlow
                statement.execute(query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getMySqlConnection(String url, String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("useSSL", "false");

        return DriverManager.getConnection(url, properties);
    }

    static void insertData(MySQLContainer<?> container) {
        try (Connection connection = getMySqlConnection(container.withDatabaseName("inventory").getJdbcUrl(),
                container.getUsername(), container.getPassword());
             Statement statement = connection.createStatement()
        ) {
            statement.addBatch("UPDATE customers SET first_name='Anne Marie' WHERE id=1004");
            statement.addBatch("INSERT INTO customers VALUES (1005, 'Jason', 'Bourne', 'jason@bourne.org')");
            statement.addBatch("DELETE FROM customers WHERE id=1005");
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
