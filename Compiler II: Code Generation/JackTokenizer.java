import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {

    public static enum TYPE {KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST, NONE};
    public static enum KEYWORD {CLASS, METHOD, FUNCTION, CONSTRUCTOR, INT, BOOLEAN, CHAR, VOID, VAR, 
                                STATIC, FIELD, LET, DO, IF, ELSE, WHILE,RETURN,TRUE, FALSE, NULL, THIS};

    private String currentToken;
    private TYPE currentTokenType;
    private int pointer;
    private ArrayList<String> tokens;
    private static Pattern tokenPatterns;
    private static String keyWordReg;
    private static String symbolReg;
    private static String intReg;
    private static String strReg;
    private static String idReg;
    private static HashMap<String,KEYWORD> keyWordMap = new HashMap<String, KEYWORD>();
    private static HashSet<Character> opSet = new HashSet<Character>();

     // Data structures to store all different keywords and operators
    static {

        keyWordMap.put("class",KEYWORD.CLASS);keyWordMap.put("constructor",KEYWORD.CONSTRUCTOR);keyWordMap.put("function",KEYWORD.FUNCTION);
        keyWordMap.put("method",KEYWORD.METHOD);keyWordMap.put("field",KEYWORD.FIELD);keyWordMap.put("static",KEYWORD.STATIC);
        keyWordMap.put("var",KEYWORD.VAR);keyWordMap.put("int",KEYWORD.INT);keyWordMap.put("char",KEYWORD.CHAR);
        keyWordMap.put("boolean",KEYWORD.BOOLEAN);keyWordMap.put("void",KEYWORD.VOID);keyWordMap.put("true",KEYWORD.TRUE);
        keyWordMap.put("false",KEYWORD.FALSE);keyWordMap.put("null",KEYWORD.NULL);keyWordMap.put("this",KEYWORD.THIS);
        keyWordMap.put("let",KEYWORD.LET);keyWordMap.put("do",KEYWORD.DO);keyWordMap.put("if",KEYWORD.IF);
        keyWordMap.put("else",KEYWORD.ELSE);keyWordMap.put("while",KEYWORD.WHILE);keyWordMap.put("return",KEYWORD.RETURN);

        opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
        opSet.add('<');opSet.add('>');opSet.add('=');
    }


    // JackTokenizer Cosntructor
    public JackTokenizer(File inFile) {
        try {
            Scanner scanner = new Scanner(inFile);
            String preprocessed = "";
            String line = "";

            while (scanner.hasNext()){
                line = noComments(scanner.nextLine()).trim();
                if (line.length() > 0) {
                    preprocessed += line + "\n";
                }
            }

            preprocessed = noBlockComments(preprocessed).trim();

            // Initializes all regex
            initRegs();

            Matcher m = tokenPatterns.matcher(preprocessed);
            tokens = new ArrayList<String>();
            pointer = 0;

            while (m.find()){
                tokens.add(m.group());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // A helper method - Initializes all necessary regex for the tokenizer
    private void initRegs() {
        keyWordReg = "";
        for (String seg: keyWordMap.keySet()) {
            keyWordReg += seg + "|";
        }
        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
        intReg = "[0-9]+";
        strReg = "\"[^\"\n]*\"";
        idReg = "[a-zA-Z_]\\w*";
        tokenPatterns = Pattern.compile(idReg + "|" + keyWordReg + symbolReg + "|" + intReg + "|" + strReg);
    }

    // Checks whether the input has more tokens
    public boolean hasMoreTokens() {
        return pointer < tokens.size();
    }

    // If the input has more tokens, advances to the next one
    public void advance() {
        if (hasMoreTokens()) {
            currentToken = tokens.get(pointer);
            pointer++;
        }else {
            throw new IllegalStateException("No more tokens");
        }

        // For each token, sets its type
        if (currentToken.matches(keyWordReg)) {
            currentTokenType = TYPE.KEYWORD;
        } else if (currentToken.matches(symbolReg)) {
            currentTokenType = TYPE.SYMBOL;
        } else if (currentToken.matches(intReg)) {
            currentTokenType = TYPE.INT_CONST;
        } else if (currentToken.matches(strReg)) {
            currentTokenType = TYPE.STRING_CONST;
        } else if (currentToken.matches(idReg)) {
            currentTokenType = TYPE.IDENTIFIER;
        } else {
            throw new IllegalArgumentException("Unknown token:" + currentToken);
        }
    }

    // Gets the current token
    public String getCurrentToken() {
        return currentToken;
    }

    // Gets the current token type
    public TYPE tokenType() {
        return currentTokenType;
    }

    // If the token type is keyword, returns the current token
    public KEYWORD keyWord() {
        if (currentTokenType == TYPE.KEYWORD){
            return keyWordMap.get(currentToken);
        } else {
            throw new IllegalStateException("Current token is not a keyword!");
        }
    }

    // If the token type is symbol, returns the current token
    public char symbol() {
        if (currentTokenType == TYPE.SYMBOL) {
            return currentToken.charAt(0);
        } else {
            throw new IllegalStateException("Current token is not a symbol!");
        }
    }

    // If the token type is identifier, returns the current token
    public String identifier() {
        if (currentTokenType == TYPE.IDENTIFIER) {
            return currentToken;
        } else {
            throw new IllegalStateException("Current token is not an identifier! current type:" + currentTokenType);
        }
    }

    // If the token type is integer, returns the current token
    public int intVal() {
        if (currentTokenType == TYPE.INT_CONST) {
            return Integer.parseInt(currentToken);
        } else {
            throw new IllegalStateException("Current token is not an integer constant!");
        }
    }

    // If the token type is string, returns the current token (The string, but without the double quotes)
    public String stringVal() {
        if (currentTokenType == TYPE.STRING_CONST) {
            return currentToken.substring(1, currentToken.length() - 1);
        } else {
            throw new IllegalStateException("Current token is not a string constant!");
        }
    }

    // A helper method - Moves the pointer back
    public void pointerBack() {
        if (pointer > 0) {
            pointer--;
            currentToken = tokens.get(pointer);
        }
    }

    // A helper method - Checks whether the current symbol is an operator
     public boolean isOp() {
        return opSet.contains(symbol());
     }

    // A helper method - Handles all comments in the given file
    public static String noComments(String strIn) {

        int position = strIn.indexOf("//");
        if (position != -1){
             strIn = strIn.substring(0, position);
        }
    
        return strIn;
    }

    // A helper method - Handles all spaces in the given file
    public static String noSpaces(String strIn) {
        String result = "";

        if (strIn.length() != 0){
            String[] segs = strIn.split(" ");
            for (String s: segs){
                result += s;
            }
        }
        return result;
    }

    // A helper method - Handles all block comments in the given file
    public static String noBlockComments(String strIn){

        int startIndex = strIn.indexOf("/*");
        if (startIndex == -1) return strIn;
        String result = strIn;
        int endIndex = strIn.indexOf("*/");

        while (startIndex != -1) {
            if (endIndex == -1) {
                return strIn.substring(0,startIndex - 1);
            }
            result = result.substring(0,startIndex) + result.substring(endIndex + 2);
            startIndex = result.indexOf("/*");
            endIndex = result.indexOf("*/");
        }
        return result;
    } 
}