import java.util.HashMap;

public class SymbolTable {

    private HashMap<String, Symbol> classSymbols;
    private HashMap<String, Symbol> subroutineSymbols;
    private HashMap<Symbol.KIND, Integer> indices;

    // SymbolTable Constructor
    public SymbolTable() { 
        classSymbols = new HashMap<String, Symbol>();
        subroutineSymbols = new HashMap<String, Symbol>();

        indices = new HashMap<Symbol.KIND, Integer>();
        indices.put(Symbol.KIND.ARG, 0);
        indices.put(Symbol.KIND.FIELD, 0);
        indices.put(Symbol.KIND.STATIC, 0);
        indices.put(Symbol.KIND.VAR, 0);
    }

    // Starts a new subroutine declaration, by resetting its symbol table
    public void reset() {
        subroutineSymbols.clear();
        indices.put(Symbol.KIND.VAR, 0);
        indices.put(Symbol.KIND.ARG, 0);
    }

    // Defines a new variable of the given name, type and kind
    public void define(String name, String type, Symbol.KIND kind) { 
        if (kind == Symbol.KIND.ARG || kind == Symbol.KIND.VAR){
            int index = indices.get(kind);
            Symbol symbol = new Symbol(type ,kind, index);
            indices.put(kind, index + 1);
            subroutineSymbols.put(name,symbol);

        } else if (kind == Symbol.KIND.STATIC || kind == Symbol.KIND.FIELD) {
            int index = indices.get(kind);
            Symbol symbol = new Symbol(type, kind, index);
            indices.put(kind, index + 1);
            classSymbols.put(name, symbol);
        }
    }

    // Returns the number of variables of the given kind, that are already defined in the table
    public int varCount(Symbol.KIND kind) {
        return indices.get(kind);
    }

    // Returns the kind of the given identifier, if given
    public Symbol.KIND kindOf(String name) {
        Symbol symbol = checkSymbol(name);
        if (symbol != null) {
            return symbol.getKind();
        } 
        return Symbol.KIND.NONE;
    }

    // Returns the kind of the type identifier, if given
    public String typeOf(String name) {
        Symbol symbol = checkSymbol(name);
        if (symbol != null) {
            return symbol.getType();
        }
        return "";
    }

    // Returns the kind of the index identifier, if given
    public int indexOf(String name) {
        Symbol symbol = checkSymbol(name);
        if (symbol != null) {
            return symbol.getIndex();
        }
        return -1;
    }

    // A helper method - Checks if the required symbol exists
    private Symbol checkSymbol(String name) {
        if (classSymbols.get(name) != null) {
            return classSymbols.get(name);
        } else if (subroutineSymbols.get(name) != null) {
            return subroutineSymbols.get(name);
        } else {
            return null;
        }
    }
}