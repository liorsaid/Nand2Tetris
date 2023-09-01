import java.io.File;

public class CompilationEngine {

    private VMWriter vmWriter;
    private JackTokenizer tokenizer;
    private SymbolTable symbolTable;
    private String currentClass;
    private String currentSubroutine;
    private int labelIndex;

    // CompiltaionEngine Constructor - Creates a vm file
    public CompilationEngine(File fileIn, File fileOut) { 
        tokenizer = new JackTokenizer(fileIn);
        vmWriter = new VMWriter(fileOut);
        symbolTable = new SymbolTable();
        labelIndex = 0;
    }

    // A helper method - Returns the name of the current function
    private String currentFunction() {
        if (currentClass.length() != 0 && currentSubroutine.length() != 0){
            return currentClass + "." + currentSubroutine;
        }
        return "";
    }

    // A helper method - Compiles a general type
    private String compileType() {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.INT || 
            tokenizer.keyWord() == JackTokenizer.KEYWORD.CHAR || tokenizer.keyWord() == JackTokenizer.KEYWORD.BOOLEAN)){
            return tokenizer.getCurrentToken();
        }
        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER){
            return tokenizer.identifier();
        }
        error("Required input of type int/char/boolean/className");
        return "";
    }

    // Compiles a complete class
    public void compileClass() {
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.CLASS){
            System.out.println(tokenizer.getCurrentToken());
            error("Class required");
        }

        // Handles the class name
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("ClassName required");
        }

        currentClass = tokenizer.identifier();
        symIsNecessary('{');

        // Handles class variable declaration and subroutine declaration
        compileClassVarDec();
        compileSubroutine();
        symIsNecessary('}');

        if (tokenizer.hasMoreTokens()){
            throw new IllegalStateException("Unexpected tokens");
        }

        // Saves file
        vmWriter.close();
    }

    // Compiles a static variable declaration or a field declaration
    private void compileClassVarDec() {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        // Subroutine declaration or class variable declaration, both start with a keyword
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD){
            error("Keywords required");
        }

        // Subroutine declaration
        if (tokenizer.keyWord() == JackTokenizer.KEYWORD.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.KEYWORD.FUNCTION || tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            tokenizer.pointerBack();
            return;
        }

        // Checks if a class variable declaration exists
        if (tokenizer.keyWord() != JackTokenizer.KEYWORD.STATIC && tokenizer.keyWord() != JackTokenizer.KEYWORD.FIELD){
            error("static or field required");
        }

        Symbol.KIND kind = null;
        String type = "";
        String name = "";

        switch (tokenizer.keyWord()) {
            case STATIC : kind = Symbol.KIND.STATIC;
            break;
            case FIELD : kind = Symbol.KIND.FIELD;
            break;
        }

        type = compileType();

        do {
            // Handles the Variable Name
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
                error("identifier required");
            }

            name = tokenizer.identifier();
            symbolTable.define(name,type,kind);
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';' required");
            }

            if (tokenizer.symbol() == ';') {
                break;
            }
        } while (true);
        compileClassVarDec();
    }

    // Compiles a complete subroutine - Method, Function or Constructor
    private void compileSubroutine() {
        tokenizer.advance();

        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        // A subroutine's start
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || (tokenizer.keyWord() != JackTokenizer.KEYWORD.CONSTRUCTOR && tokenizer.keyWord() != JackTokenizer.KEYWORD.FUNCTION && tokenizer.keyWord() != JackTokenizer.KEYWORD.METHOD)){
            error("Required constructor/function/method");
        }

        JackTokenizer.KEYWORD keyword = tokenizer.keyWord();

        symbolTable.reset();
        if (tokenizer.keyWord() == JackTokenizer.KEYWORD.METHOD){
            symbolTable.define("this",currentClass, Symbol.KIND.ARG);
        }

        String type = "";

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.VOID){
            type = "void";
        }else {
            tokenizer.pointerBack();
            type = compileType();
        }

        // A subroutine name which is an identifier
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("subroutineName required");
        }

        // Handles a parameter list and a subroutine's body
        currentSubroutine = tokenizer.identifier();
        symIsNecessary('(');
        compileParameterList();
        symIsNecessary(')');
        compileSubroutineBody(keyword);
        compileSubroutine();
    }

    // Compiles a parameter list, possibly empty - Without the enclosing () tokens
    private void compileParameterList() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.pointerBack();
            return;
        }

        String type = "";
        tokenizer.pointerBack();
        do {
            type = compileType();

            // Handles a variable name
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
                error("identifier required");
            }

            symbolTable.define(tokenizer.identifier(), type, Symbol.KIND.ARG);
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')) {
                error("',' or ')'");
            }

            if (tokenizer.symbol() == ')') {
                tokenizer.pointerBack();
                break;
            }

        } while (true);
    }

    // Compiles a subroutine's body 
    private void compileSubroutineBody(JackTokenizer.KEYWORD keyword) { 
        symIsNecessary('{');
        compileVarDec();
        writeFunctionDec(keyword);
        compileStatements();
        symIsNecessary('}');
    }

    // A helper method - Compiles a subroutine call
    private void compileSubroutineCall() {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
            error("identifier required");
        }

        String name = tokenizer.identifier();
        int nArgs = 0;

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '(') {
            vmWriter.writePush(VMWriter.SEGMENT.POINTER,0); 

            // Sets the number of arguments in the call
            nArgs = compileExpressionList() + 1;

            symIsNecessary(')');
            vmWriter.writeCall(currentClass + '.' + name, nArgs);

        } else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '.') {
            String objectName = name;
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
                error("identifier required");
            }

            name = tokenizer.identifier();
            String type = symbolTable.typeOf(objectName);
            if (type.equals("int")||type.equals("boolean") || 
                type.equals("char") || type.equals("void")) {
                error("Not an expected type");
            } else if (type.equals("")) {
                name = objectName + "." + name;
            } else {
                nArgs = 1;
                vmWriter.writePush(getSeg(symbolTable.kindOf(objectName)), symbolTable.indexOf(objectName));
                name = symbolTable.typeOf(objectName) + "." + name;
            }

            symIsNecessary('(');
            nArgs += compileExpressionList();
            symIsNecessary(')');
            vmWriter.writeCall(name, nArgs);
        } else {
            error("'(' or '.' required");
        }
    }

    // Compiles a variable declaration
    private void compileVarDec() {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD || tokenizer.keyWord() != JackTokenizer.KEYWORD.VAR) {
            tokenizer.pointerBack();
            return;
        }

        String type = compileType();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER) {
                error("identifier required");
            }

            symbolTable.define(tokenizer.identifier(), type, Symbol.KIND.VAR);
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')) { 
                error("',' or ';' required");
            }

            if (tokenizer.symbol() == ';') {
                break;
            }
        } while (true);
        compileVarDec();
    }

    // Compiles a sequence of statements
    private void compileStatements() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '}') {
            tokenizer.pointerBack();
            return;
        }

        if (tokenizer.tokenType() != JackTokenizer.TYPE.KEYWORD) {
            error("Keyword required");
        } else {
            switch (tokenizer.keyWord()) {
                case LET : compileLet(); break;
                case IF : compileIf(); break;
                case WHILE : compileWhile(); break;
                case DO : compileDo(); break;
                case RETURN : compileReturn(); break;
                default : error("Required 'let'/'if'/'while'/'do'/'return'");
            }
        }
        compileStatements();
    }

    // Compiles a let statement
    private void compileLet() { 
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.IDENTIFIER){
            error("variable name required");
        }

        String varName = tokenizer.identifier();
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'[' or '=' required");
        }
        boolean isArray = false;
        if (tokenizer.symbol() == '[') {
            isArray = true;
            vmWriter.writePush(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
            compileExpression();
            symIsNecessary(']');
            vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
        }

        if (isArray) {
            tokenizer.advance();
        } 
        compileExpression();
        symIsNecessary(';');

        if (isArray) {
            vmWriter.writePop(VMWriter.SEGMENT.TEMP, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER, 1);
            vmWriter.writePush(VMWriter.SEGMENT.TEMP, 0);
            vmWriter.writePop(VMWriter.SEGMENT.THAT, 0);
        } else {
            vmWriter.writePop(getSeg(symbolTable.kindOf(varName)), symbolTable.indexOf(varName));
        }
    }

    // Compiles an if statement
    private void compileIf() { 
        String elseLabel = newLabel();
        String endLabel = newLabel();

        symIsNecessary('(');
        compileExpression();
        symIsNecessary(')');
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(elseLabel);
        symIsNecessary('{');
        compileStatements();
        symIsNecessary('}');
        vmWriter.writeGoto(endLabel);
        vmWriter.writeLabel(elseLabel);

        // If there is an else statement
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.ELSE){
            symIsNecessary('{');
            compileStatements();
            symIsNecessary('}');
        } else {
            tokenizer.pointerBack();
        }
        vmWriter.writeLabel(endLabel);
    }

    // Compiles a while statement
    private void compileWhile() { 
        String continueLabel = newLabel();
        String topLabel = newLabel();
        vmWriter.writeLabel(topLabel);
        symIsNecessary('(');
        compileExpression();
        symIsNecessary(')');
        vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
        vmWriter.writeIf(continueLabel);
        symIsNecessary('{'); 
        compileStatements(); 
        symIsNecessary('}'); 
        vmWriter.writeGoto(topLabel);
        vmWriter.writeLabel(continueLabel);
    }

    // Compiles a do statement
    private void compileDo() { 
        compileSubroutineCall();
        symIsNecessary(';');
        vmWriter.writePop(VMWriter.SEGMENT.TEMP, 0);
    }

    // Compiles a return statement
    private void compileReturn() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ';') {
            vmWriter.writePush(VMWriter.SEGMENT.CONST, 0);
        } else {
            tokenizer.pointerBack();
            compileExpression();
            symIsNecessary(';');
        }
        vmWriter.writeReturn();
    }

    // Compiles an expression
    private void compileExpression() { 
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.isOp()) {
                String opCommand = "";
                switch (tokenizer.symbol()){
                    case '+' : opCommand = "add"; break;
                    case '-' : opCommand = "sub"; break;
                    case '*' : opCommand = "call Math.multiply 2"; break;
                    case '/' : opCommand = "call Math.divide 2"; break;
                    case '<' : opCommand = "lt"; break;
                    case '>' : opCommand = "gt"; break;
                    case '=' : opCommand = "eq"; break;
                    case '&' : opCommand = "and"; break;
                    case '|' : opCommand = "or"; break;
                    default:error("An unknown operator");
                }
                compileTerm();
                vmWriter.writeCommand(opCommand, "", "");
            } else {
                tokenizer.pointerBack();
                break;
            }
        } while (true);
    }

    // Compiles a term
    private void compileTerm() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.IDENTIFIER) {
            String temp = tokenizer.identifier();
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '[') {
                vmWriter.writePush(getSeg(symbolTable.kindOf(temp)), symbolTable.indexOf(temp));
                compileExpression();
                symIsNecessary(']');
                vmWriter.writeArithmetic(VMWriter.COMMAND.ADD);
                vmWriter.writePop(VMWriter.SEGMENT.POINTER,1);
                vmWriter.writePush(VMWriter.SEGMENT.THAT,0);

            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')) {
                tokenizer.pointerBack();
                tokenizer.pointerBack();
                compileSubroutineCall();
            } else {
                tokenizer.pointerBack();
                vmWriter.writePush(getSeg(symbolTable.kindOf(temp)), symbolTable.indexOf(temp));
            }
        } else {
            if (tokenizer.tokenType() == JackTokenizer.TYPE.INT_CONST) {
                vmWriter.writePush(VMWriter.SEGMENT.CONST, tokenizer.intVal());
            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.STRING_CONST) {
                String str = tokenizer.stringVal();
                vmWriter.writePush(VMWriter.SEGMENT.CONST, str.length());
                vmWriter.writeCall("String.new", 1);
                for (int i = 0; i < str.length(); i++) {
                    vmWriter.writePush(VMWriter.SEGMENT.CONST, (int)str.charAt(i));
                    vmWriter.writeCall("String.appendChar", 2);
                }

            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.TRUE) {
                vmWriter.writePush(VMWriter.SEGMENT.CONST, 0); 
                vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && tokenizer.keyWord() == JackTokenizer.KEYWORD.THIS) {
                vmWriter.writePush(VMWriter.SEGMENT.POINTER,0);
            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.KEYWORD && (tokenizer.keyWord() == JackTokenizer.KEYWORD.FALSE || tokenizer.keyWord() == JackTokenizer.KEYWORD.NULL)) {
                vmWriter.writePush(VMWriter.SEGMENT.CONST, 0);
            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == '(') {
                compileExpression();
                symIsNecessary(')');
            } else if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                char c = tokenizer.symbol();
                compileTerm();
                if (c == '-') {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NEG);
                } else {
                    vmWriter.writeArithmetic(VMWriter.COMMAND.NOT);
                }
            } else {
                error("An unexpected constant/expression/term");
            }
        }
    }

    // Compiles an expression list, possibly empty, and returns the amount of its expressions
    private int compileExpressionList() {
        int nArgs = 0;
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.pointerBack();
        } else {
            nArgs = 1;
            tokenizer.pointerBack();
            compileExpression();
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.TYPE.SYMBOL && tokenizer.symbol() == ',') {
                    compileExpression();
                    nArgs++;
                } else {
                    tokenizer.pointerBack();
                    break;
                }
            } while (true);
        }

        return nArgs;
    }

    // A helper method - Returns a new label
    private String newLabel() { 
        return "LABEL_" + (labelIndex++);
    }

    // A helper method - Writes a function declaration
    private void writeFunctionDec(JackTokenizer.KEYWORD keyword) {
        vmWriter.writeFunction(currentFunction(), symbolTable.varCount(Symbol.KIND.VAR));
        if (keyword == JackTokenizer.KEYWORD.METHOD) {
            vmWriter.writePush(VMWriter.SEGMENT.ARG, 0);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER, 0);

        } else if (keyword == JackTokenizer.KEYWORD.CONSTRUCTOR) {
            vmWriter.writePush(VMWriter.SEGMENT.CONST, symbolTable.varCount(Symbol.KIND.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(VMWriter.SEGMENT.POINTER, 0); 
        }
    }

    // A helper method - Returns the corresponding segment of the given input kind
    private VMWriter.SEGMENT getSeg(Symbol.KIND kind) {
        switch (kind) {
            case FIELD : return VMWriter.SEGMENT.THIS;
            case STATIC : return VMWriter.SEGMENT.STATIC;
            case VAR : return VMWriter.SEGMENT.LOCAL;
            case ARG : return VMWriter.SEGMENT.ARG;
            default : return VMWriter.SEGMENT.NONE;
        }
    }

    // A helper method - Where a certain symbol is necessary, require its appearance
    private void symIsNecessary(char symbol) { 
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TYPE.SYMBOL || tokenizer.symbol() != symbol) {
            error("'" + symbol + "' required");
        }
    }

    // Handles errors by throwing exceptions
    private void error(String val) { 
        throw new IllegalStateException("Expected token missing: " + val + " Current token:" + tokenizer.getCurrentToken());
    }
}