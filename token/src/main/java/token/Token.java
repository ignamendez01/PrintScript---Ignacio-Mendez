package token;

public class Token {
    private final String type;
    private final String value;

    public Token(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return switch (type) {
            case "STRING", "NUMBER", "IDENTIFIER", "BOOLEAN" -> type + "(" + value + ")";
            default -> type;
        };
    }
}
