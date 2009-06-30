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
package org.seasar.doma.internal.jdbc.sql;

import static org.seasar.doma.internal.util.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.seasar.doma.domain.Domain;
import org.seasar.doma.domain.DomainVisitor;


/**
 * @author taedium
 * 
 */
public class PreparedSqlBuilder {

    protected final List<BindParameter> parameters = new ArrayList<BindParameter>();

    protected final StringBuilder rawSql = new StringBuilder(200);

    protected final StringBuilder formattedSql = new StringBuilder(200);

    protected final DomainVisitor<String, Void, RuntimeException> sqlLogFormattingVisitor;

    public PreparedSqlBuilder(
            DomainVisitor<String, Void, RuntimeException> sqlLogFormattingVisitor) {
        assertNotNull(sqlLogFormattingVisitor);
        this.sqlLogFormattingVisitor = sqlLogFormattingVisitor;
    }

    public void appendSql(String sql) {
        rawSql.append(sql);
        formattedSql.append(sql);
    }

    public void cutBackSql(int length) {
        rawSql.setLength(rawSql.length() - length);
        formattedSql.setLength(formattedSql.length() - length);
    }

    public void appendDomain(Domain<?, ?> domain) {
        rawSql.append("?");
        formattedSql.append(domain.accept(sqlLogFormattingVisitor, null));
        parameters.add(new BindParameter(domain));
    }

    public PreparedSql build() {
        return new PreparedSql(rawSql, formattedSql, parameters);
    }
}