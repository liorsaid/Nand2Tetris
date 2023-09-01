public class Symbol {
    public static enum KIND {STATIC, FIELD, ARG, VAR, NONE};
    private String type;
    private KIND kind;
    private int index;

    // A helper module - designed to work efficiently with symbols

    // Symbol Constructor
    public Symbol(String type, KIND kind, int index) {
        this.type = type;
        this.kind = kind;
        this.index = index;
    }

    // Returns the type of a given symbol
    public String getType() {
        return type;
    }

    // Returns the kind of a given symbol
    public KIND getKind() {
        return kind;
    }

    // Returns the index of a given symbol
    public int getIndex() {
        return index;
    }

    // Prints the information of a symbol
    public String toString() {
        return "Symbol{" +
                "type='" + type + '\'' +
                ", kind=" + kind +
                ", index=" + index +
                '}';
    }
}