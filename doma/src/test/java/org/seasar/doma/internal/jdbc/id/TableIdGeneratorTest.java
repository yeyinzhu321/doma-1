/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.jdbc.id;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

import org.seasar.doma.internal.jdbc.id.IdGenerationConfig;
import org.seasar.doma.internal.jdbc.id.TableIdGenerator;
import org.seasar.doma.internal.jdbc.mock.MockConfig;
import org.seasar.doma.internal.jdbc.mock.MockConnection;
import org.seasar.doma.internal.jdbc.mock.MockDataSource;
import org.seasar.doma.internal.jdbc.mock.MockResultSet;
import org.seasar.doma.internal.jdbc.mock.RowData;
import org.seasar.doma.jdbc.dialect.PostgresDialect;

import junit.framework.TestCase;
import example.entity.Emp_;

/**
 * @author taedium
 * 
 */
public class TableIdGeneratorTest extends TestCase {

    public void test() throws Exception {
        MockConfig config = new MockConfig();
        config.setDialect(new PostgresDialect());
        MockConnection connection = new MockConnection();
        MockConnection connection2 = new MockConnection();
        MockResultSet resultSet2 = connection2.preparedStatement.resultSet;
        resultSet2.rows.add(new RowData(11L));
        final LinkedList<MockConnection> connections = new LinkedList<MockConnection>();
        connections.add(connection);
        connections.add(connection2);
        config.dataSource = new MockDataSource() {

            @Override
            public Connection getConnection() throws SQLException {
                return connections.pop();
            }
        };

        TableIdGenerator idGenerator = new TableIdGenerator("aaa", "PK",
                "VALUE", "EMP_ID", 1, 1);
        IdGenerationConfig idGenerationConfig = new IdGenerationConfig(config,
                new Emp_(), "EMP", "ID");
        Long value = idGenerator.generatePreInsert(idGenerationConfig);
        assertEquals(new Long(10), value);
        assertEquals("update aaa set VALUE = VALUE + ? where PK = ?", connection.preparedStatement.sql);
        assertEquals(2, connection.preparedStatement.bindValues.size());
        assertEquals("select VALUE from aaa where PK = ?", connection2.preparedStatement.sql);
        assertEquals(1, connection2.preparedStatement.bindValues.size());
    }
}