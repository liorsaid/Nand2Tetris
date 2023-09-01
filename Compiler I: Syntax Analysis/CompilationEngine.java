import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CompilationEngine {

    private PrintWriter printWriter;
    private PrintWriter tokenPrintWriter;
    private JackTokenizer tokenizer;

    // CompiltaionEngine Constructor - Creates a tokenized file and a parsed file
    public CompilationEngine(File fileIn, File fileOut, File tokenFileOut) { 
        try {
            tokenizer = new JackTokenizer(fileIn);
            printWriter = new PrintWriter(fileOut);
            tokenPrintWriter = new PrintWriter(tokenFileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // A helper method - Compiles a type
    private void compileType() {
        tokenizer.advance();
        boolean isType = false;

        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && (tokenizer.keyWord() == JackTokenizer.INT || tokenizer.keyWord() == JackTokenizer.CHAR || tokenizer.keyWord() == JackTokenizer.BOOLEAN)){
            printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            isType = true;
        }
        if (tokenizer.tokenType() == JackTokenizer.IDENTIFIER){
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            isType = true;
        }
        if (!isType) {
            error("Required input of type int/char/boolean/className");
        }
    }

    // Compiles a complete class
    public void compileClass() {
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || tokenizer.keyWord() != JackTokenizer.CLASS){
            error("Class required");
        }

        printWriter.print("<class>\n");
        tokenPrintWriter.print("<tokens>\n");
        printWriter.print("<keyword>class</keyword>\n");
        tokenPrintWriter.print("<keyword>class</keyword>\n");

        // Handles the class Name
        tokenizer.advance();

        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("ClassName required");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        symIsNecessary('{');

        // Handles class variable declaration and subroutine declaration
        compileClassVarDec();
        compileSubroutine();
        symIsNecessary('}');

        if (tokenizer.hasMoreTokens()){
            throw new IllegalStateException("Unexpected tokens");
        }

        tokenPrintWriter.print("</tokens>\n");
        printWriter.print("</class>\n");

        // Saves file
        printWriter.close();
        tokenPrintWriter.close();
    }

    // Compiles a static variable declaration or a field declaration
    private void compileClassVarDec() {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        // Subroutine declaration or class variable declaration, both start with a keyword
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD){
            error("Keywords required");
        }

        // Subroutine declaration
        if (tokenizer.keyWord() == JackTokenizer.CONSTRUCTOR || tokenizer.keyWord() == JackTokenizer.FUNCTION || tokenizer.keyWord() == JackTokenizer.METHOD){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<classVarDec>\n");

        // Checks if a class variable declaration exists
        if (tokenizer.keyWord() != JackTokenizer.STATIC && tokenizer.keyWord() != JackTokenizer.FIELD){
            error("static or field required");
        }

        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        compileType();

        do {
            // Handles the Variable Name
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier required");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';' required");
            }

            if (tokenizer.symbol() == ',') {
                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");
            } else {
                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }
        } while (true); 
        printWriter.print("</classVarDec>\n");
        compileClassVarDec();
    }

    // Compiles a complete subroutine - Method, Function or Constructor
    private void compileSubroutine() {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }

        // A subroutine's start
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || (tokenizer.keyWord() != JackTokenizer.CONSTRUCTOR && tokenizer.keyWord() != JackTokenizer.FUNCTION && tokenizer.keyWord() != JackTokenizer.METHOD)){
            error("Required constructor/function/method");
        }

        printWriter.print("<subroutineDec>\n");
        printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && tokenizer.keyWord() == JackTokenizer.VOID){
            printWriter.print("<keyword>void</keyword>\n");
            tokenPrintWriter.print("<keyword>void</keyword>\n");
        } else {
            tokenizer.pointerBack();
            compileType();
        }

        // A subroutine name which is an identifier
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("subroutineName required");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        symIsNecessary('(');

        // Handles a parameter list
        printWriter.print("<parameterList>\n");
        compileParameterList();
        printWriter.print("</parameterList>\n");
        symIsNecessary(')');

        // Handles a subroutine's body
        compileSubroutineBody();
        printWriter.print("</subroutineDec>\n");
        compileSubroutine();
    }

    // Compiles a parameter list, possibly empty - Without the enclosing () tokens
    private void compileParameterList() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.pointerBack();
            return;
        }

        tokenizer.pointerBack();
        do {
            compileType();

            // Handles a variable name
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier required");
            }
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ')')){
                error("',' or ')'");
            }
            if (tokenizer.symbol() == ','){
                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");
            } else {
                tokenizer.pointerBack();
                break;
            }
        } while (true);
    }

    // Compiles a subroutine's body 
    private void compileSubroutineBody() { 
        printWriter.print("<subroutineBody>\n");
        symIsNecessary('{');
        compileVarDec();
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        symIsNecessary('}');
        printWriter.print("</subroutineBody>\n");
    }

    // A helper method - Compiles a subroutine call
    private void compileSubroutineCall() {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
            error("identifier required");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '(') {
            printWriter.print("<symbol>(</symbol>\n");
            tokenPrintWriter.print("<symbol>(</symbol>\n");

            // Handles an expression List
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            symIsNecessary(')');
        } else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '.') {
            printWriter.print("<symbol>.</symbol>\n");
            tokenPrintWriter.print("<symbol>.</symbol>\n");

            // Handles a subroutine's name
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier required");
            }
            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            symIsNecessary('(');
            printWriter.print("<expressionList>\n");
            compileExpressionList();
            printWriter.print("</expressionList>\n");
            symIsNecessary(')');
        } else {
            error("'(' or '.' required");
        }
    }

    // Compiles a variable declaration
    private void compileVarDec() {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD || tokenizer.keyWord() != JackTokenizer.VAR){
            tokenizer.pointerBack();
            return;
        }

        printWriter.print("<varDec>\n");
        printWriter.print("<keyword>var</keyword>\n");
        tokenPrintWriter.print("<keyword>var</keyword>\n");
        compileType();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER){
                error("identifier required");
            }

            printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
            tokenizer.advance();

            if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != ',' && tokenizer.symbol() != ';')){
                error("',' or ';'");
            }

            if (tokenizer.symbol() == ',') {
                printWriter.print("<symbol>,</symbol>\n");
                tokenPrintWriter.print("<symbol>,</symbol>\n");

            } else {
                printWriter.print("<symbol>;</symbol>\n");
                tokenPrintWriter.print("<symbol>;</symbol>\n");
                break;
            }
        } while (true);
        printWriter.print("</varDec>\n");
        compileVarDec();
    }

    // Compiles a sequence of statements
    private void compileStatements() { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '}'){
            tokenizer.pointerBack();
            return;
        }
        if (tokenizer.tokenType() != JackTokenizer.KEYWORD){
            error("keyword required");
        } else {
            switch (tokenizer.keyWord()) {
                case JackTokenizer.LET : compileLet(); break;
                case JackTokenizer.IF : compileIf(); break;
                case JackTokenizer.WHILE : compileWhile(); break;
                case JackTokenizer.DO : compileDo(); break;
                case JackTokenizer.RETURN : compileReturn(); break;
                default:error("Required 'let'/'if'/'while'/'do'/'return'");
            }
        }
        compileStatements();
    }

    // Compiles a let statement
    private void compileLet() { 
        printWriter.print("<letStatement>\n");
        printWriter.print("<keyword>let</keyword>\n");
        tokenPrintWriter.print("<keyword>let</keyword>\n");
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.IDENTIFIER) {
            error("variable name required");
        }

        printWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");
        tokenPrintWriter.print("<identifier>" + tokenizer.identifier() + "</identifier>\n");

        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.SYMBOL || (tokenizer.symbol() != '[' && tokenizer.symbol() != '=')){
            error("'[' or '=' required");
        }

        boolean isExpression = false;
        if (tokenizer.symbol() == '[') {

            isExpression = true;
            printWriter.print("<symbol>[</symbol>\n");
            tokenPrintWriter.print("<symbol>[</symbol>\n");
            compileExpression();
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ']'){
                printWriter.print("<symbol>]</symbol>\n");
                tokenPrintWriter.print("<symbol>]</symbol>\n");
            } else {
                error("']' required");
            }
        }

        if (isExpression) {
            tokenizer.advance();
        } 
        printWriter.print("<symbol>=</symbol>\n");
        tokenPrintWriter.print("<symbol>=</symbol>\n");
        compileExpression();
        symIsNecessary(';');
        printWriter.print("</letStatement>\n");
    }

    // Compiles an if statement
    private void compileIf() { 
        printWriter.print("<ifStatement>\n");
        printWriter.print("<keyword>if</keyword>\n");
        tokenPrintWriter.print("<keyword>if</keyword>\n");
        symIsNecessary('(');
        compileExpression();
        symIsNecessary(')');
        symIsNecessary('{');
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        symIsNecessary('}');

        // If there is an else statement
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.KEYWORD && tokenizer.keyWord() == JackTokenizer.ELSE){
            printWriter.print("<keyword>else</keyword>\n");
            tokenPrintWriter.print("<keyword>else</keyword>\n");
            symIsNecessary('{');
            printWriter.print("<statements>\n");
            compileStatements();
            printWriter.print("</statements>\n");
            symIsNecessary('}');
        } else {
            tokenizer.pointerBack();
        }

        printWriter.print("</ifStatement>\n");
    }

    // Compiles a while statement
    private void compileWhile() { 
        printWriter.print("<whileStatement>\n");
        printWriter.print("<keyword>while</keyword>\n");
        tokenPrintWriter.print("<keyword>while</keyword>\n");
        symIsNecessary('(');
        compileExpression();
        symIsNecessary(')');
        symIsNecessary('{');
        printWriter.print("<statements>\n");
        compileStatements();
        printWriter.print("</statements>\n");
        symIsNecessary('}');
        printWriter.print("</whileStatement>\n");
    }

    // Compiles a do statement
    private void compileDo() { 
        printWriter.print("<doStatement>\n");
        printWriter.print("<keyword>do</keyword>\n");
        tokenPrintWriter.print("<keyword>do</keyword>\n");
        compileSubroutineCall();
        symIsNecessary(';');
        printWriter.print("</doStatement>\n");
    }

    // Compiles a return statement
    private void compileReturn() { 
        printWriter.print("<returnStatement>\n");
        printWriter.print("<keyword>return</keyword>\n");
        tokenPrintWriter.print("<keyword>return</keyword>\n");
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ';'){
            printWriter.print("<symbol>;</symbol>\n");
            tokenPrintWriter.print("<symbol>;</symbol>\n");
            printWriter.print("</returnStatement>\n");
            return;
        }
        tokenizer.pointerBack();
        compileExpression();
        symIsNecessary(';');
        printWriter.print("</returnStatement>\n");
    }

    // Compiles an expression
    private void compileExpression() { 
        printWriter.print("<expression>\n");
        compileTerm();
        do {
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.isOp()){
                if (tokenizer.symbol() == '>'){
                    printWriter.print("<symbol>&gt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&gt;</symbol>\n");
                } else if (tokenizer.symbol() == '<'){
                    printWriter.print("<symbol>&lt;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&lt;</symbol>\n");
                } else if (tokenizer.symbol() == '&') {
                    printWriter.print("<symbol>&amp;</symbol>\n");
                    tokenPrintWriter.print("<symbol>&amp;</symbol>\n");
                } else {
                    printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                    tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                }
                compileTerm();
            } else {
                tokenizer.pointerBack();
                break;
            }

        } while (true);
        printWriter.print("</expression>\n");
    }

    // Compiles a term
    private void compileTerm() { 
        printWriter.print("<term>\n");
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.IDENTIFIER) {
            String temp = tokenizer.identifier();
            tokenizer.advance();
            if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '['){
                printWriter.print("<identifier>" + temp + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + temp + "</identifier>\n");
                printWriter.print("<symbol>[</symbol>\n");
                tokenPrintWriter.print("<symbol>[</symbol>\n");
                compileExpression();
                symIsNecessary(']');
            } else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && (tokenizer.symbol() == '(' || tokenizer.symbol() == '.')){
                tokenizer.pointerBack();
                tokenizer.pointerBack();
                compileSubroutineCall();
            } else {
                printWriter.print("<identifier>" + temp + "</identifier>\n");
                tokenPrintWriter.print("<identifier>" + temp + "</identifier>\n");
                tokenizer.pointerBack();
            }

        } else {
            if (tokenizer.tokenType() == JackTokenizer.INT_CONST) {
                printWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
                tokenPrintWriter.print("<integerConstant>" + tokenizer.intVal() + "</integerConstant>\n");
            } else if (tokenizer.tokenType() == JackTokenizer.STRING_CONST) {
                printWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
                tokenPrintWriter.print("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>\n");
            } else if (tokenizer.tokenType() == JackTokenizer.KEYWORD &&
                            (tokenizer.keyWord() == JackTokenizer.TRUE ||
                            tokenizer.keyWord() == JackTokenizer.FALSE ||
                            tokenizer.keyWord() == JackTokenizer.NULL ||
                            tokenizer.keyWord() == JackTokenizer.THIS)) {
                    printWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
                    tokenPrintWriter.print("<keyword>" + tokenizer.getCurrentToken() + "</keyword>\n");
            } else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == '(') {
                printWriter.print("<symbol>(</symbol>\n");
                tokenPrintWriter.print("<symbol>(</symbol>\n");
                compileExpression();
                symIsNecessary(')');
            } else if (tokenizer.tokenType() == JackTokenizer.SYMBOL && (tokenizer.symbol() == '-' || tokenizer.symbol() == '~')) {
                printWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                tokenPrintWriter.print("<symbol>" + tokenizer.symbol() + "</symbol>\n");
                compileTerm();
            } else {
                error("Not a valid term");
            }
        }

        printWriter.print("</term>\n");
    }

    // Compiles an expression list, possibly empty
    private void compileExpressionList() {
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ')') {
            tokenizer.pointerBack();
        } else {
            tokenizer.pointerBack();
            compileExpression();
            do {
                tokenizer.advance();
                if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == ',') {
                    printWriter.print("<symbol>,</symbol>\n");
                    tokenPrintWriter.print("<symbol>,</symbol>\n");
                    compileExpression();
                } else {
                    tokenizer.pointerBack();
                    break;
                }
            } while (true);
        }
    }

    // A helper method - Where a certain symbol is necessary, require its appearance
    private void symIsNecessary(char symbol) { 
        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.SYMBOL && tokenizer.symbol() == symbol){
            printWriter.print("<symbol>" + symbol + "</symbol>\n");
            tokenPrintWriter.print("<symbol>" + symbol + "</symbol>\n");
        } else {
            error("'" + symbol + "' required");
        }
    }

    // Handles errors by throwing exceptions
    private void error(String val) { 
        throw new IllegalStateException("Expected token missing: " + val + " Current token:" + tokenizer.getCurrentToken());
    }
}