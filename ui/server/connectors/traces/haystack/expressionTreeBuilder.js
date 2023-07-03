/*
 * Copyright 2018 Expedia Group
 *
 *         Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 */

const expressionTreeBuilder = {};
const messages = require('../../../../static_codegen/traceReader_pb');

const reservedField = ['startTime', 'endTime', 'limit', 'spanLevelFilters', 'granularity'];

expressionTreeBuilder.createFieldFromKeyValue = (key, value) => {
    const field = new messages.Field();
    field.setName(key);
    let fieldValue = value;
    let operator = messages.Field.Operator.EQUAL;

    // check for custom operator at beginning of value string
    if (value[0] === '>' || value[0] === '<') {
        operator = value[0] === '>' ? messages.Field.Operator.GREATER_THAN : messages.Field.Operator.LESS_THAN;
        fieldValue = value.substr(1, value.length);
    }
    field.setValue(fieldValue);
    field.setOperator(operator);

    return field;
};

function createSpanLevelExpression(spanLevelFilters) {
    return spanLevelFilters.map((filterJson) => {
       const filter = JSON.parse(filterJson);
        const operand = new messages.Operand();
        const expressionTree = new messages.ExpressionTree();
        expressionTree.setOperator(messages.ExpressionTree.Operator.AND);
        expressionTree.setIsspanlevelexpression(false);

        const operands = Object.keys(filter)
        .map((key) => {
            const op = new messages.Operand();

            const field = expressionTreeBuilder.createFieldFromKeyValue(key, filter[key]);

            op.setField(field);

            return op;
        });

        expressionTree.setOperandsList(operands);
        operand.setExpression(expressionTree);

        return operand;
    });
}

function createTraceLevelOperands(query) {
    return Object.keys(query)
    .filter(key => query[key] && !reservedField.includes(key))
    .map((key) => {
        const operand = new messages.Operand();

        const field = expressionTreeBuilder.createFieldFromKeyValue(key, query[key]);

        operand.setField(field);

        return operand;
    });
}


expressionTreeBuilder.createFilterExpression = (query) => {
    const expressionTree = new messages.ExpressionTree();

    expressionTree.setOperator(messages.ExpressionTree.Operator.AND);
    expressionTree.setIsspanlevelexpression(false);

    const traceLevelOperands = createTraceLevelOperands(query);
    let spanLevelExpressions = [];
    if (query.spanLevelFilters) {
        spanLevelExpressions = createSpanLevelExpression(JSON.parse(query.spanLevelFilters));
    }

    expressionTree.setOperandsList([...traceLevelOperands, ...spanLevelExpressions]);

    return expressionTree;
};

module.exports = expressionTreeBuilder;
