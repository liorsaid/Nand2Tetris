import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Parser {
    public String strFileArr[];
    private Scanner commands;
    private String currentCommand;
    public static final int ARITHMETIC = 0;
    public static final int PUSH = 1;
    public static final int POP = 2;
    public static final int LABEL = 3;
    public static final int GOTO = 4;
    public static final int IF = 5;
    public static final int FUNCTION = 6;
    public static final int RETURN = 7;
    public static final int CALL = 8;
    public static final ArrayList<String> arithmetic = new ArrayList<String>();
    private int argType;
    private String arg1;
    private int arg2;

    // Creates a list of arithmetic commands
    static {
        arithmetic.add("add");
        arithmetic.add("sub");
        arithmetic.add("neg");
        arithmetic.add("eq");
        arithmetic.add("gt");
        arithmetic.add("lt");
        arithmetic.add("and");
        arithmetic.add("or");
        arithmetic.add("not");
    }

    // A Parser constructor
    public Parser (File fileIn) {

        // Initializing argType and the arguments arg1, arg2
        argType = -1;
        arg1 = "";
        arg2 = -1;

        // Receives a given file and returns its content as a string of commands,
        // for the Parser to work with efficiently 
        try {
            commands = new Scanner(fileIn);
            String fileString = "";
            String line = "";
            while (commands.hasNext()) {
                line = noComments(commands.nextLine()).trim();
                if (line.length() > 0) {
                    fileString += line + "\n";
                }
            }
            commands = new Scanner(fileString.trim());
        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
        }
    }

    // Checks whether the commands string has more commands in it
    public boolean hasMoreLines() { 
      return commands.hasNextLine();
    }

    // Advances to the next command, receives argType and
    // determines the values of arg1, arg2 accordingly
    public void advance(){
        currentCommand = commands.nextLine();
        arg1 = "";
        arg2 = -1;
        String[] segments = currentCommand.split(" ");

        // Must receive no more than 3 arguments 
        if (segments.length > 3) {
            throw new IllegalArgumentException("Too many arguments!");
        }

        // If argType is ARITHMETIC/RETURN, includes only arg1. Otherwise includes arg1, arg2 
        if (arithmetic.contains(segments[0])) {
            argType = ARITHMETIC;
            arg1 = segments[0];

        } else if (segments[0].equals("return")) {
            argType = RETURN;
            arg1 = segments[0];

        } else {
            arg1 = segments[1];
            if (segments[0].equals("push")) {
                argType = PUSH;

            } else if (segments[0].equals("pop")){
                argType = POP;

            } else if (segments[0].equals("label")){
                argType = LABEL;

            } else if (segments[0].equals("if")){
                argType = IF;

            } else if (segments[0].equals("goto")){
                argType = GOTO;

            } else if (segments[0].equals("function")){
                argType = FUNCTION;

            } else if (segments[0].equals("call")){
                argType = CALL;

            } else {
                throw new IllegalArgumentException("Unknown Command Type!");
            }

            // For several argTypes, checks whether arg2 is an integer
            if (argType == PUSH || argType == POP || argType == FUNCTION || argType == CALL) {
                try {
                    arg2 = Integer.parseInt(segments[2]);
                } catch (Exception e){
                    throw new IllegalArgumentException("Arg2 is not an integer!");
                }
            }
        }
    }

    // Returns the numeric value of an argType
    public int commandType() {
        if (argType != -1) {
            return argType;
        } else {
            throw new IllegalStateException("No command!");
        }
    }

    // Returns arg1 of a command, unless argType is RETURN
    public String arg1() {
        if (commandType() != RETURN) {
            return arg1;
        } else {
            throw new IllegalStateException("An illegal command");
        }
    }

    // Returns arg2 of a command, but only if argType is PUSH/POP/FUNCTION/CALL
    public int arg2() {
        if (commandType() == PUSH || commandType() == POP || commandType() == FUNCTION || commandType() == CALL) {
            return arg2;
        } else {
            throw new IllegalStateException("An illegal argument");
        }
    }

    // A helper method - Receives a line and removes its comments, if exist
    public static String noComments(String strLine){
        int position = strLine.indexOf("//");
        if (position != -1) {
            strLine = strLine.substring(0, position);
        }
        return strLine;
    }

    // A helper method - Receives a line and removes its spaces, if exist
    public static String noSpaces(String strIn){
        String strLine = "";
        if (strIn.length() != 0){
            String[] segments = strLine.split(" ");
            for (String s: segments){
                strLine += s;
            }
        }
        return strLine;
    }

    // A helper method - Receives a file name and checks its extension
    public static String Extension(String fileName){
        int index = fileName.lastIndexOf('.');
        if (index != -1){
            return fileName.substring(index);
        } else {
            return "";
        }
    }
}