/*
 * Copyright 2025 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl.expression.math;

import com.hazelcast.jet.sql.impl.JetSqlSerializerHook;
import com.hazelcast.sql.impl.QueryException;
import com.hazelcast.sql.impl.SqlErrorCode;
import com.hazelcast.sql.impl.expression.Expression;
import com.hazelcast.sql.impl.expression.ExpressionEvalContext;
import com.hazelcast.sql.impl.expression.UniExpressionWithType;
import com.hazelcast.sql.impl.row.Row;
import com.hazelcast.sql.impl.type.QueryDataType;
import com.hazelcast.sql.impl.type.QueryDataTypeFamily;

import java.math.BigDecimal;

import static com.hazelcast.sql.impl.type.QueryDataTypeUtils.DECIMAL_MATH_CONTEXT;

/**
 * Implements evaluation of SQL unary minus operator.
 */
public final class UnaryMinusFunction<T> extends UniExpressionWithType<T> {

    public UnaryMinusFunction() {
        // No-op.
    }

    private UnaryMinusFunction(Expression<?> operand, QueryDataType resultType) {
        super(operand, resultType);
    }

    public static UnaryMinusFunction<?> create(Expression<?> operand, QueryDataType resultType) {
        return new UnaryMinusFunction<>(operand, resultType);
    }

    @Override
    public int getClassId() {
        return JetSqlSerializerHook.EXPRESSION_UNARY_MINUS;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T eval(Row row, ExpressionEvalContext context) {
        Object value = operand.eval(row, context);
        if (value == null) {
            return null;
        }

        QueryDataTypeFamily family = resultType.getTypeFamily();
        return (T) evalNumeric((Number) value, family);
    }

    private static Object evalNumeric(Number number, QueryDataTypeFamily family) {
        switch (family) {
            case TINYINT:
                return (byte) -number.byteValue();

            case SMALLINT:
                return (short) -number.shortValue();

            case INTEGER:
                return -number.intValue();

            case BIGINT:
                try {
                    return Math.negateExact(number.longValue());
                } catch (ArithmeticException e) {
                    throw QueryException.error(SqlErrorCode.DATA_EXCEPTION,
                            "BIGINT overflow in unary '-' operator (consider adding explicit CAST to DECIMAL)");
                }

            case DECIMAL:
                return ((BigDecimal) number).negate(DECIMAL_MATH_CONTEXT);

            case REAL:
                return -number.floatValue();

            case DOUBLE:
                return -number.doubleValue();

            default:
                throw new IllegalArgumentException("unexpected result family: " + family);
        }
    }

}
