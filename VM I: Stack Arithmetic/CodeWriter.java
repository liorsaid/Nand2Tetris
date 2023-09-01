import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class CodeWriter {

    private int arithmeticJump;
    private PrintWriter printer;

    // A CodeWriter constructor
    public CodeWriter(File fileOut) {
        try {
            printer = new PrintWriter(fileOut);
            arithmeticJump = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // For each given arithmetic command, translate it to assembly
    public void writeArithmetic(String command){

        if (command.equals("add")) {
            printer.print(arithmeticTemplate1() + "M=M+D\n");

        } else if (command.equals("sub")) {
            printer.print(arithmeticTemplate1() + "M=M-D\n");

        } else if (command.equals("and")) {
            printer.print(arithmeticTemplate1() + "M=M&D\n");

        } else if (command.equals("or")) {
            printer.print(arithmeticTemplate1() + "M=M|D\n");

        } else if (command.equals("gt")) {
            printer.print(arithmeticTemplate2("JLE")); // Negate
            arithmeticJump++;

        } else if (command.equals("lt")) {
            printer.print(arithmeticTemplate2("JGE")); // Negate
            arithmeticJump++;

        } else if (command.equals("eq")) {
            printer.print(arithmeticTemplate2("JNE")); // Negate
            arithmeticJump++;

        } else if (command.equals("not")) {
            printer.print("@SP\nA=M-1\nM=!M\n");

        } else if (command.equals("neg")) {
            printer.print("D=0\n@SP\nA=M-1\nM=D-M\n");

        } else {
            throw new IllegalArgumentException("Don't call writeArithmetic() for a non-arithmetic command");
        }
    }

    // For each given Push/Pop command, translate it to assembly
    public void writePushPop(int command, String segment, int index){

        // Handles Push commands
        if (command == Parser.PUSH){
            if (segment.equals("constant")){
                printer.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            } else if (segment.equals("local")){
                printer.print(pushTemplate("LCL",index,false));

            } else if (segment.equals("argument")){
                printer.print(pushTemplate("ARG",index,false));

            } else if (segment.equals("this")){
                printer.print(pushTemplate("THIS",index,false));

            } else if (segment.equals("that")){
                printer.print(pushTemplate("THAT",index,false));

            } else if (segment.equals("temp")){
                printer.print(pushTemplate("R5", index + 5,false));

            } else if (segment.equals("pointer") && index == 0){
                printer.print(pushTemplate("THIS",index,true));

            } else if (segment.equals("pointer") && index == 1){
                printer.print(pushTemplate("THAT",index,true));

            } else if (segment.equals("static")) {
                printer.print(pushTemplate(String.valueOf(16 + index),index,true));
            }

        // Handles Pop commands
        } else if (command == Parser.POP) {
            if (segment.equals("local")) {
                printer.print(popTemplate("LCL",index,false));

            } else if (segment.equals("argument")) {
                printer.print(popTemplate("ARG",index,false));

            } else if (segment.equals("this")) {
                printer.print(popTemplate("THIS",index,false));

            } else if (segment.equals("that")) {
                printer.print(popTemplate("THAT",index,false));

            } else if (segment.equals("temp")) {
                printer.print(popTemplate("R5", index + 5,false));

            } else if (segment.equals("pointer") && index == 0) {
                printer.print(popTemplate("THIS",index,true));

            } else if (segment.equals("pointer") && index == 1) {
                printer.print(popTemplate("THAT",index,true));

            } else if (segment.equals("static")) {
                printer.print(popTemplate(String.valueOf(16 + index),index,true));
            }
        } else {
            throw new IllegalArgumentException("Don't call writePushPop() for a non-pushpop command");
        }
    }

    // Closes the given file
    public void close() {
        printer.close();
    }

    // A unique template for add/sub/and/or arithmetic commands
    private String arithmeticTemplate1() {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n";
    }

    // A unique template for gt/lt/eq arithmetic commands
    private String arithmeticTemplate2(String type) {
        return "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "A=A-1\n" +
                "D=M-D\n" +
                "@FALSE" + arithmeticJump + "\n" +
                "D;" + type + "\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=-1\n" +
                "@CONTINUE" + arithmeticJump + "\n" +
                "0;JMP\n" +
                "(FALSE" + arithmeticJump + ")\n" +
                "@SP\n" +
                "A=M-1\n" +
                "M=0\n" +
                "(CONTINUE" + arithmeticJump + ")\n";
    }

    // A unique template for Push commands
    private String pushTemplate(String segment, int index, boolean isDirect){
        String noPointerCode = (isDirect)? "" : "@" + index + "\n" + "A=D+A\nD=M\n";
        return "@" + segment + "\n" +
                "D=M\n"+
                noPointerCode +
                "@SP\n" +
                "A=M\n" +
                "M=D\n" +
                "@SP\n" +
                "M=M+1\n";
    }

    // A unique template for Pop commands
    private String popTemplate(String segment, int index, boolean isDirect){
        String noPointerCode;
        if (isDirect) {
            noPointerCode = "D=A\n";
        } else {
            noPointerCode = "D=M\n@" + index + "\nD=D+A\n";
        }
        return "@" + segment + "\n" +
                noPointerCode +
                "@R13\n" +
                "M=D\n" +
                "@SP\n" +
                "AM=M-1\n" +
                "D=M\n" +
                "@R13\n" +
                "A=M\n" +
                "M=D\n";
    }
}