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
package org.seasar.doma.internal.jdbc.dialect;

import org.seasar.doma.internal.jdbc.dialect.OraclePagingTransformer;
import org.seasar.doma.internal.jdbc.sql.NodePreparedSqlBuilder;
import org.seasar.doma.internal.jdbc.sql.PreparedSql;
import org.seasar.doma.internal.jdbc.sql.SqlParser;
import org.seasar.doma.jdbc.SqlLogFormattingVisitor;
import org.seasar.doma.jdbc.SqlNode;

import junit.framework.TestCase;

/**
 * @author taedium
 * 
 */
public class OraclePagingTransformerTest extends TestCase {

    public void testOffsetLimit() throws Exception {
        String expected = "select * from ( select temp_.*, rownum rownumber_ from ( select * from emp order by emp.id ) temp_ ) where rownumber_ > 5 and rownumber_ <= 15";
        OraclePagingTransformer transformer = new OraclePagingTransformer(5, 10);
        SqlParser parser = new SqlParser("select * from emp order by emp.id");
        SqlNode sqlNode = transformer.transform(parser.parse());
        NodePreparedSqlBuilder sqlBuilder = new NodePreparedSqlBuilder(
                new SqlLogFormattingVisitor());
        PreparedSql sql = sqlBuilder.build(sqlNode);
        assertEquals(expected, sql.getRawSql());
    }

    public void testOffsetLimit_forUpdate() throws Exception {
        String expected = "select * from ( select temp_.*, rownum rownumber_ from ( select * from emp order by emp.id  ) temp_ ) where rownumber_ > 5 and rownumber_ <= 15 for update";
        OraclePagingTransformer transformer = new OraclePagingTransformer(5, 10);
        SqlParser parser = new SqlParser(
                "select * from emp order by emp.id for update");
        SqlNode sqlNode = transformer.transform(parser.parse());
        NodePreparedSqlBuilder sqlBuilder = new NodePreparedSqlBuilder(
                new SqlLogFormattingVisitor());
        PreparedSql sql = sqlBuilder.build(sqlNode);
        assertEquals(expected, sql.getRawSql());
    }

    public void testOffsetOnly() throws Exception {
        String expected = "select * from ( select temp_.*, rownum rownumber_ from ( select * from emp order by emp.id ) temp_ ) where rownumber_ > 5";
        OraclePagingTransformer transformer = new OraclePagingTransformer(5, -1);
        SqlParser parser = new SqlParser("select * from emp order by emp.id");
        SqlNode sqlNode = transformer.transform(parser.parse());
        NodePreparedSqlBuilder sqlBuilder = new NodePreparedSqlBuilder(
                new SqlLogFormattingVisitor());
        PreparedSql sql = sqlBuilder.build(sqlNode);
        assertEquals(expected, sql.getRawSql());
    }

    public void testLimitOnly() throws Exception {
        String expected = "select * from ( select temp_.*, rownum rownumber_ from ( select * from emp order by emp.id ) temp_ ) where rownumber_ <= 10";
        OraclePagingTransformer transformer = new OraclePagingTransformer(-1,
                10);
        SqlParser parser = new SqlParser("select * from emp order by emp.id");
        SqlNode sqlNode = transformer.transform(parser.parse());
        NodePreparedSqlBuilder sqlBuilder = new NodePreparedSqlBuilder(
                new SqlLogFormattingVisitor());
        PreparedSql sql = sqlBuilder.build(sqlNode);
        assertEquals(expected, sql.getRawSql());
    }
}