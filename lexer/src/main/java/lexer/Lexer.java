package lexer;

import token.Token;
import token.TokenCreator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class Lexer {
    private final TokenCreator tokenCreator;

    public Lexer(String tokenFile) {
        this.tokenCreator = new TokenCreator(tokenFile);
    }

    public List<Token> makeTokens(String inputText) {
        List<Token> tokens = new ArrayList<>();
        List<String> tokenValues = TokenValueExtractor.extractTokenValues(inputText);

        createTokenList(tokenValues, tokens);

        return tokens;
    }

    private void createTokenList(List<String> tokenValues, List<Token> tokens) {
        int row = 1;
        int column = 1;
        for (String tokenValue : tokenValues) {
            Token token = tokenCreator.createToken(tokenValue, row, column);
            if(Objects.equals(tokenValue, ";")){
                row++;
                column = 1;
            }else{
                column++;
            }
            tokens.add(token);
        }
    }

    public List<Token> makeTokens(InputStream fileInput) {
        BufferedReader br = new BufferedReader(new InputStreamReader(fileInput));
        String currentLine = readLine(br);

        StringBuilder statement = new StringBuilder();
        while (currentLine != null) {
            String line = currentLine.trim();
            statement.append(line).append("\n");
            currentLine = readLine(br);
        }

        return makeTokens(statement.toString());
    }

    private static String readLine(BufferedReader br) {
        try {
            return br.readLine();
        } catch (Exception e) {
            throw new RuntimeException("Error reading input", e);
        }
    }

}