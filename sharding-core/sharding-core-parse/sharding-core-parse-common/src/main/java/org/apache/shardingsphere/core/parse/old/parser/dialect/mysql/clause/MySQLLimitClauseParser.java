/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.old.parser.dialect.mysql.clause;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OffsetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RowCountToken;
import org.apache.shardingsphere.core.parse.old.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.old.lexer.dialect.mysql.MySQLKeyword;
import org.apache.shardingsphere.core.parse.old.lexer.token.Literals;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.clause.SQLClauseParser;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;

/**
 * Limit clause parser for MySQL.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MySQLLimitClauseParser implements SQLClauseParser {
    
    private final LexerEngine lexerEngine;
    
    /**
     * Parse limit.
     * 
     * @param selectStatement select statement
     */
    public void parse(final SelectStatement selectStatement) {
        if (!lexerEngine.skipIfEqual(MySQLKeyword.LIMIT)) {
            return;
        }
        int valueIndex = -1;
        int valueBeginPosition = lexerEngine.getCurrentToken().getEndPosition();
        int value;
        boolean isParameterForValue = false;
        if (lexerEngine.equalAny(Literals.INT)) {
            value = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
            valueBeginPosition = valueBeginPosition - (value + "").length();
        } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
            valueIndex = selectStatement.getParametersIndex();
            value = -1;
            valueBeginPosition--;
            isParameterForValue = true;
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.COMMA)) {
            selectStatement.setLimit(getLimitWithComma(valueIndex, valueBeginPosition, value, isParameterForValue, selectStatement));
            return;
        }
        if (lexerEngine.skipIfEqual(MySQLKeyword.OFFSET)) {
            selectStatement.setLimit(getLimitWithOffset(valueIndex, valueBeginPosition, value, isParameterForValue, selectStatement));
            return;
        }
        if (isParameterForValue) {
            selectStatement.increaseParametersIndex();
        } else {
            selectStatement.addSQLToken(new RowCountToken(valueBeginPosition, value));
        }
        Limit limit = new Limit();
        limit.setRowCount(new LimitValue(value, valueIndex, false));
        selectStatement.setLimit(limit);
    }
    
    private Limit getLimitWithComma(final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue, final SelectStatement selectStatement) {
        int rowCountBeginPosition = lexerEngine.getCurrentToken().getEndPosition();
        int rowCountValue;
        int rowCountIndex = -1;
        boolean isParameterForRowCount = false;
        if (lexerEngine.equalAny(Literals.INT)) {
            rowCountValue = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
            rowCountBeginPosition = rowCountBeginPosition - (rowCountValue + "").length();
        } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
            rowCountIndex = -1 == index ? selectStatement.getParametersIndex() : index + 1;
            rowCountValue = -1;
            rowCountBeginPosition--;
            isParameterForRowCount = true;
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.nextToken();
        if (isParameterForValue) {
            selectStatement.increaseParametersIndex();
        } else {
            selectStatement.addSQLToken(new OffsetToken(valueBeginPosition, value));
        }
        if (isParameterForRowCount) {
            selectStatement.increaseParametersIndex();
        } else {
            selectStatement.addSQLToken(new RowCountToken(rowCountBeginPosition, rowCountValue));
        }
        Limit result = new Limit();
        result.setRowCount(new LimitValue(rowCountValue, rowCountIndex, false));
        result.setOffset(new LimitValue(value, index, true));
        return result;
    }
    
    private Limit getLimitWithOffset(final int index, final int valueBeginPosition, final int value, final boolean isParameterForValue, final SelectStatement selectStatement) {
        int offsetBeginPosition = lexerEngine.getCurrentToken().getEndPosition();
        int offsetValue = -1;
        int offsetIndex = -1;
        boolean isParameterForOffset = false;
        if (lexerEngine.equalAny(Literals.INT)) {
            offsetValue = Integer.parseInt(lexerEngine.getCurrentToken().getLiterals());
            offsetBeginPosition = offsetBeginPosition - (offsetValue + "").length();
        } else if (lexerEngine.equalAny(Symbol.QUESTION)) {
            offsetIndex = -1 == index ? selectStatement.getParametersIndex() : index + 1;
            offsetBeginPosition--;
            isParameterForOffset = true;
        } else {
            throw new SQLParsingException(lexerEngine);
        }
        lexerEngine.nextToken();
        if (isParameterForOffset) {
            selectStatement.increaseParametersIndex();
        } else {
            selectStatement.addSQLToken(new OffsetToken(offsetBeginPosition, offsetValue));
        }
        if (isParameterForValue) {
            selectStatement.increaseParametersIndex();
        } else {
            selectStatement.addSQLToken(new RowCountToken(valueBeginPosition, value));
        }
        Limit result = new Limit();
        result.setRowCount(new LimitValue(value, index, false));
        result.setOffset(new LimitValue(offsetValue, offsetIndex, true));
        return result;
    }
}
