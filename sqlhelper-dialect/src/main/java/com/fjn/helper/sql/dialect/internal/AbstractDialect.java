
/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the LGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.gnu.org/licenses/lgpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fjn.helper.sql.dialect.internal;

import com.fjn.helper.sql.dialect.DatabaseInfo;
import com.fjn.helper.sql.dialect.Dialect;
import com.fjn.helper.sql.dialect.RowSelection;
import com.fjn.helper.sql.dialect.internal.limit.DefaultLimitHandler;
import com.fjn.helper.sql.dialect.internal.limit.LimitHandler;
import com.fjn.helper.sql.dialect.internal.urlparser.NoopUrlParser;
import com.fjn.helper.sql.dialect.internal.urlparser.UrlParser;

import java.sql.CallableStatement;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;


public abstract class AbstractDialect<T extends AbstractDialect> implements Dialect {
    protected AbstractDialect delegate = null;

    private UrlParser urlParser;
    private LimitHandler limitHandler;

    private final Properties properties = new Properties();


    public AbstractDialect() {
        setLimitHandler(new DefaultLimitHandler(this));
        setUrlParser(new NoopUrlParser());
    }

    public AbstractDialect(Driver driver) {
        this();
    }


    public final Properties getDefaultProperties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }


    protected LimitHandler getLimitHandler() {
        return this.delegate != null ? this.delegate.getLimitHandler() : this.limitHandler;
    }

    protected void setLimitHandler(LimitHandler limitHandler) {
        limitHandler.setDialect(this);
        getRealDialect().limitHandler = limitHandler;
    }

    protected void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    protected void setUrlParser(UrlParser urlParser) {
        getRealDialect().urlParser = urlParser;
    }

    @Override
    public boolean isSupportsLimit() {
        return this.delegate == null ? false : this.delegate.isSupportsLimit();
    }

    @Override
    public boolean isSupportsLimitOffset() {
        return this.delegate == null ? isSupportsLimit() : this.delegate.isSupportsLimitOffset();
    }

    @Override
    public boolean isSupportsVariableLimit() {
        return this.delegate == null ? isSupportsLimit() : this.delegate.isSupportsVariableLimit();
    }

    @Override
    public boolean isBindLimitParametersInReverseOrder() {
        return this.delegate == null ? false : this.delegate.isBindLimitParametersInReverseOrder();
    }

    @Override
    public boolean isBindLimitParametersFirst() {
        return this.delegate == null ? false : this.delegate.isBindLimitParametersFirst();
    }

    @Override
    public boolean isUseMaxForLimit() {
        return this.delegate == null ? false : this.delegate.isUseMaxForLimit();
    }

    @Override
    public boolean isForceLimitUsage() {
        return this.delegate == null ? false : this.delegate.isForceLimitUsage();
    }

    protected AbstractDialect getRealDialect() {
        if (delegate == null) {
            return this;
        } else {
            return delegate;
        }
    }

    @Override
    public int registerResultSetOutParameter(CallableStatement statement, int position)
            throws SQLException {
        throw new UnsupportedOperationException(getClass().getName() + " does not support resultsets via stored procedures");
    }

    @Override
    public String getLimitSql(String sql, RowSelection selection) {
        return getLimitHandler().processSql(sql, selection);
    }

    @Override
    public void setMaxRows(RowSelection selection, PreparedStatement statement) throws SQLException {
        getLimitHandler().setMaxRows(selection, statement);
    }

    @Override
    public int bindLimitParametersAtEndOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
        return getLimitHandler().bindLimitParametersAtEndOfQuery(selection, statement, index);
    }

    @Override
    public int bindLimitParametersAtStartOfQuery(RowSelection selection, PreparedStatement statement, int index) throws SQLException {
        return getLimitHandler().bindLimitParametersAtStartOfQuery(selection, statement, index);
    }

    protected UrlParser getUrlParser() {
        return getRealDialect().urlParser;
    }

    @Override
    public DatabaseInfo parse(String jdbcUrl) {
        return getUrlParser().parse(jdbcUrl);
    }

    @Override
    public List<String> getUrlSchemas() {
        return getUrlParser().getUrlSchemas();
    }
}