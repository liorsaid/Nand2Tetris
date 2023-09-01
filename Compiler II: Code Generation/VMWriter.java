import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;

public class VMWriter {

    public static enum SEGMENT {CONST, ARG, LOCAL, STATIC, THIS, THAT, POINTER, TEMP, NONE};
    public static enum COMMAND {ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT};

    private static HashMap<SEGMENT,String> segmentStrings = new HashMap<SEGMENT, String>();
    private static HashMap<COMMAND,String> commandStrings = new HashMap<COMMAND, String>();
    private PrintWriter printWriter;

     // Data structures to store all different segments and commands
    static {
        segmentStrings.put(SEGMENT.CONST, "constant");
        segmentStrings.put(SEGMENT.ARG, "argument");
        segmentStrings.put(SEGMENT.LOCAL, "local");
        segmentStrings.put(SEGMENT.STATIC, "static");
        segmentStrings.put(SEGMENT.THIS, "this");
        segmentStrings.put(SEGMENT.THAT, "that");
        segmentStrings.put(SEGMENT.POINTER, "pointer");
        segmentStrings.put(SEGMENT.TEMP, "temp");

        commandStrings.put(COMMAND.ADD, "add");
        commandStrings.put(COMMAND.SUB, "sub");
        commandStrings.put(COMMAND.NEG, "neg");
        commandStrings.put(COMMAND.EQ, "eq");
        commandStrings.put(COMMAND.GT, "gt");
        commandStrings.put(COMMAND.LT, "lt");
        commandStrings.put(COMMAND.AND, "and");
        commandStrings.put(COMMAND.OR, "or");
        commandStrings.put(COMMAND.NOT, "not");
    }

    // VMWriter Constructor
    public VMWriter(File fOut) {
        try {
            printWriter = new PrintWriter(fOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Writes a VM push command
    public void writePush(SEGMENT segment, int index) {
        writeCommand("push", segmentStrings.get(segment), String.valueOf(index));
    }

    // Writes a VM pop command
    public void writePop(SEGMENT segment, int index) {
        writeCommand("pop", segmentStrings.get(segment), String.valueOf(index));
    }

    // Writes a VM arithmetic command
    public void writeArithmetic(COMMAND command) {
        writeCommand(commandStrings.get(command), "", "");
    }

    // Writes a VM label command
    public void writeLabel(String label) {
        writeCommand("label", label, "");
    }

    // Writes a VM goto command
    public void writeGoto(String label) {
        writeCommand("goto", label, "");
    }
    
    // Writes a VM if-goto command
    public void writeIf(String label) {
        writeCommand("if-goto", label, "");
    }

    // Writes a VM call command
    public void writeCall(String name, int nArgs) {
        writeCommand("call", name, String.valueOf(nArgs));
    }

    // Writes a VM function command
    public void writeFunction(String name, int nLocals) {
        writeCommand("function", name, String.valueOf(nLocals));
    }

    // Writes a VM return command
    public void writeReturn() {
        writeCommand("return", "", "");
    }

    // A helper method - Writes a general VM command, used to write the command types more efficiently
    public void writeCommand(String cmd, String arg1, String arg2) {
        printWriter.print(cmd + " " + arg1 + " " + arg2 + "\n");
    }

    // Closes the output file
    public void close() {
        printWriter.close();
    }
}