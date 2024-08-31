package parser.builder;

import ast.*;
import ast.BooleanOperator;
import ast.interfaces.ValueNode;
import token.Token;

import java.util.*;

public class ValueASTBuilder implements ASTBuilder<ValueNode> {
    private final List<String> forbidden = Arrays.asList("ASSIGN", "DECLARE", "KEYWORD", "TYPE",
            "METHOD", "IF", "ELSE", "RKEY", "LKEY");

    private final Map<String, Integer> precedence = Map.of(
            "+", 1,
            "-", 1,
            "*", 2,
            "/", 2
    );

    @Override
    public boolean verify(List<Token> statement) {
        boolean result = true;
        if (statement.isEmpty()) {
            result = false;
        }else {
            for (Token token : statement) {
                if (forbidden.contains(token.getType())){
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public ValueNode build(List<Token> statement) {
        List<Token> reorganizedTokens = reorganize(statement);
        Deque<ValueNode> nodeStack = new ArrayDeque<>();

        for (int i = 0; i < reorganizedTokens.size(); i++) {
            Token token = reorganizedTokens.get(i);
            switch (token.getType()) {
                case "NUMBER" -> {
                    double numberValue = Double.parseDouble(token.getValue());
                    if (numberValue % 1 == 0) {
                        nodeStack.addLast(new NumberOperator((int) numberValue));
                    } else {
                        nodeStack.addLast(new NumberOperator(numberValue));
                    }
                }
                case "STRING" -> nodeStack.addLast(new StringOperator(token.getValue().substring(1, token.getValue().length() - 1)));
                case "IDENTIFIER" -> nodeStack.addLast(new IdentifierOperator(token.getValue()));
                case "BOOLEAN" -> nodeStack.addLast(new BooleanOperator(token.getValue()));
                case "OPERATOR" -> {
                    ValueNode rightNode = nodeStack.removeLast();
                    ValueNode leftNode = nodeStack.removeLast();
                    if (leftNode instanceof StringOperator && rightNode instanceof NumberOperator) {
                        nodeStack.addLast(new BinaryOperation(leftNode, token.getValue(), new StringOperator(((NumberOperator) rightNode).getValue().toString())));
                    } else if (leftNode instanceof NumberOperator && rightNode instanceof StringOperator) {
                        nodeStack.addLast(new BinaryOperation(new StringOperator(((NumberOperator) leftNode).getValue().toString()), token.getValue(), rightNode));
                    } else {
                        nodeStack.addLast(new BinaryOperation(leftNode, token.getValue(), rightNode));
                    }
                }
                case "FUNCTION" -> {
                    String functionName = token.getValue();
                    i += 2;
                    String argumentValue = reorganizedTokens.get(i).getValue();
                    nodeStack.addLast(new Function(functionName, argumentValue));
                    i += 2;

                }
                default ->
                        throw new RuntimeException("Unexpected token type: " + token.getType() + " at " + token.getPosition().x() + ":" + token.getPosition().y());
            }
        }

        if (nodeStack.size() != 1) {
            throw new RuntimeException("Invalid expression: more than one node remaining in stack after parsing");
        }

        return nodeStack.getFirst();
    }

    private List<Token> reorganize(List<Token> tokens) {
        List<Token> outputList = new ArrayList<>();
        Deque<Token> operatorStack = new ArrayDeque<>();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            switch (token.getType()) {
                case "NUMBER", "STRING", "IDENTIFIER", "BOOLEAN" -> outputList.add(token);
                case "OPERATOR" -> {
                    while (!operatorStack.isEmpty() && !Objects.equals(operatorStack.getLast().getType(), "LPAR") &&
                            precedence.get(operatorStack.getLast().getValue()) >= precedence.get(token.getValue())) {
                        outputList.add(operatorStack.removeLast());
                    }
                    operatorStack.add(token);
                }
                case "LPAR" -> operatorStack.add(token);
                case "RPAR" -> {
                    while (!operatorStack.isEmpty() && !Objects.equals(operatorStack.getLast().getType(), "LPAR")) {
                        outputList.add(operatorStack.removeLast());
                    }
                    if (operatorStack.isEmpty() || !Objects.equals(operatorStack.removeLast().getType(), "LPAR")) {
                        throw new RuntimeException("Mismatched parentheses in expression");
                    }
                }
                case "FUNCTION" ->{
                    outputList.addLast(token);
                    i++;
                    while (!Objects.equals(tokens.get(i).getType(), "RPAR")) {
                        outputList.addLast(tokens.get(i));
                        i++;
                    }
                    outputList.addLast(tokens.get(i));
                }
                default ->
                        throw new RuntimeException("Unexpected token type: " + token.getType() + " at " + token.getPosition().x() + ":" + token.getPosition().y());
            }
        }

        while (!operatorStack.isEmpty()) {
            Token operator = operatorStack.removeLast();
            if (Objects.equals(operator.getType(), "LPAR") || Objects.equals(operator.getType(), "RPAR")) {
                throw new RuntimeException("Mismatched parentheses in expression");
            }
            outputList.add(operator);
        }

        return outputList;
    }
}

