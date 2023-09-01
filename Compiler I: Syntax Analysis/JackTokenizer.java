import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {
    
        // A list of onstants for token types
        public final static int KEYWORD = 1;
        public final static int SYMBOL = 2;
        public final static int IDENTIFIER = 3;
        public final static int INT_CONST = 4;
        public final static int STRING_CONST = 5;
    
        // A list of onstants for keywords
        public final static int CLASS = 10;
        public final static int METHOD = 11;
        public final static int FUNCTION = 12;
        public final static int CONSTRUCTOR = 13;
        public final static int INT = 14;
        public final static int BOOLEAN = 15;
        public final static int CHAR = 16;
        public final static int VOID = 17;
        public final static int VAR = 18;
        public final static int STATIC = 19;
        public final static int FIELD = 20;
        public final static int LET = 21;
        public final static int DO = 22;
        public final static int IF = 23;
        public final static int ELSE = 24;
        public final static int WHILE = 25;
        public final static int RETURN = 26;
        public final static int TRUE = 27;
        public final static int FALSE = 28;
        public final static int NULL = 29;
        public final static int THIS = 30;
    
        private Scanner scanner;
        private String currentToken;
        private int currentTokenType;
        private int pointer;
        private ArrayList<String> tokens;
    
        private static Pattern tokenPatterns;
        private static String keyWordReg;
        private static String symbolReg;
        private static String intReg;
        private static String strReg;
        private static String idReg;
    
        private static HashMap<String,Integer> keyWordMap = new HashMap<String, Integer>();
        private static HashSet<Character> opSet = new HashSet<Character>();
    
        // Data structures to store all different keywords and operators
        static {
    
            keyWordMap.put("class",CLASS);keyWordMap.put("constructor",CONSTRUCTOR);keyWordMap.put("function",FUNCTION);
            keyWordMap.put("method",METHOD);keyWordMap.put("field",FIELD);keyWordMap.put("static",STATIC);
            keyWordMap.put("var",VAR);keyWordMap.put("int",INT);keyWordMap.put("char",CHAR);
            keyWordMap.put("boolean",BOOLEAN);keyWordMap.put("void",VOID);keyWordMap.put("true",TRUE);
            keyWordMap.put("false",FALSE);keyWordMap.put("null",NULL);keyWordMap.put("this",THIS);
            keyWordMap.put("let",LET);keyWordMap.put("do",DO);keyWordMap.put("if",IF);
            keyWordMap.put("else",ELSE);keyWordMap.put("while",WHILE);keyWordMap.put("return",RETURN);
    
            opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
            opSet.add('<');opSet.add('>');opSet.add('=');
        }

        // JackTokenizer Cosntructor
        public JackTokenizer(File inFile) {

            try {
                scanner = new Scanner(inFile);
                String preprocessed = "";
                String line = "";
    
                while (scanner.hasNext()) {
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
    
                while (m.find()) {
                    tokens.add(m.group());
                }
    
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Initializes currentToken, currentTokenType
            currentToken = "";
            currentTokenType = -1;
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
            idReg = "[\\w_]+";
    
            tokenPatterns = Pattern.compile(keyWordReg + symbolReg + "|" + intReg + "|" + strReg + "|" + idReg);
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
            } else {
                throw new IllegalStateException("No more tokens");
            }
    
            // For each token, sets its type
            if (currentToken.matches(keyWordReg)) {
                currentTokenType = KEYWORD;
            } else if (currentToken.matches(symbolReg)) {
                currentTokenType = SYMBOL;
            } else if (currentToken.matches(intReg)) {
                currentTokenType = INT_CONST;
            } else if (currentToken.matches(strReg)) {
                currentTokenType = STRING_CONST;
            } else if (currentToken.matches(idReg)) {
                currentTokenType = IDENTIFIER;
            } else {
                throw new IllegalArgumentException("Unknown token:" + currentToken);
            }
        }

        // Gets the current token
        public String getCurrentToken() {
            return currentToken;
        }

        // Gets the current token type
        public int tokenType(){
            return currentTokenType;
        }

        // If the token type is keyword, returns the current token
        public int keyWord() {
            if (currentTokenType == KEYWORD){
                return keyWordMap.get(currentToken);
    
            } else {
                throw new IllegalStateException("Current token is not a keyword!");
            }
        }

        // If the token type is symbol, returns the current token
        public char symbol() {
            if (currentTokenType == SYMBOL){
                return currentToken.charAt(0);
    
            } else {
                throw new IllegalStateException("Current token is not a symbol!");
            }
        }

        // If the token type is identifier, returns the current token
        public String identifier() {
            if (currentTokenType == IDENTIFIER){
                return currentToken;
    
            } else {
                throw new IllegalStateException("Current token is not an identifier!");
            }
        }

        // If the token type is integer, returns the current token
        public int intVal() {
            if (currentTokenType == INT_CONST){
                return Integer.parseInt(currentToken);

            } else {
                throw new IllegalStateException("Current token is not an integer constant!");
            }
        }

        // If the token type is string, returns the current token (The string, but without the double quotes)
        public String stringVal() {
            if (currentTokenType == STRING_CONST){
                return currentToken.substring(1, currentToken.length() - 1);
    
            } else {
                throw new IllegalStateException("Current token is not a string constant!");
            }
        }

        // A helper method - Moves the pointer back
        public void pointerBack() {
            if (pointer > 0) {
                pointer--;
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