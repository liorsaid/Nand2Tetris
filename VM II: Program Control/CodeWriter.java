import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeWriter {

    private int arithmeticJump;
    private String fileInput = "";
    private PrintWriter printer;
    private static final Pattern labelReg = Pattern.compile("^[^0-9][0-9A-Za-z\\_\\:\\.\\$]+");
    private static int labelCount = 0;
    
    // A CodeWriter constructor
    public CodeWriter(File fileOut) {
        try {
            fileInput = fileOut.getName();
            printer = new PrintWriter(fileOut);
            arithmeticJump = 0;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setFileName(String fileName) {
        fileInput = fileName;
    }

    // For each given arithmetic command, translates it to assembly
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

    // For each given Push/Pop command, translates it to assembly
    public void writePushPop(int command, String segment, int index) {

        // Handles Push commands
        if (command == Parser.PUSH){
            if (segment.equals("constant")) {
                printer.print("@" + index + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");

            } else if (segment.equals("local")) {
                printer.print(pushTemplate("LCL",index,false));

            } else if (segment.equals("argument")) {
                printer.print(pushTemplate("ARG",index,false));

            } else if (segment.equals("this")) {
                printer.print(pushTemplate("THIS",index,false));

            } else if (segment.equals("that")) {
                printer.print(pushTemplate("THAT",index,false));

            } else if (segment.equals("temp")) {
                printer.print(pushTemplate("R5", index + 5,false));

            } else if (segment.equals("pointer") && index == 0) {
                printer.print(pushTemplate("THIS",index,true));

            } else if (segment.equals("pointer") && index == 1) {
                printer.print(pushTemplate("THAT",index,true));

            } else if (segment.equals("static")) {
                printer.print("@" + fileInput + index + "\n" + "D=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
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
                printer.print("@" + fileInput + index + "\nD=A\n@R13\nM=D\n@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");
            }
        } else {
            throw new IllegalArgumentException("Don't call writePushPop() for a non-pushpop command");
        }
    }

    // Writes assembly code that effects the label command
    public void writeLabel(String label) {
        Matcher m = labelReg.matcher(label);

        if (m.find()) {
            printer.print("(" + label +")\n");

        } else {
            throw new IllegalArgumentException("Wrong label format!");

        }
    }

    // Writes assembly code that effects the goto command
    public void writeGoto(String label) {
        Matcher m = labelReg.matcher(label);

        if (m.find()) {
            printer.print("@" + label +"\n0;JMP\n");

        } else {
            throw new IllegalArgumentException("Wrong label format!");

        }
    }

    // Writes assembly code that effects the if-goto command
    public void writeIf(String label) {
        Matcher m = labelReg.matcher(label);

        if (m.find()) {
            printer.print(arithmeticTemplate1() + "@" + label +"\nD;JNE\n");

        } else {
            throw new IllegalArgumentException("Wrong label format!");

        }
    }

    // Bootstrap Code - Writes assembly code that effects the VM initialization
    public void writeInit() {

    printer.print("@256\n" +
                "D=A\n" +
                "@SP\n" +
                "M=D\n");
     writeCall("Sys.init",0);
    
    }

    // Writes assembly code that effects the function command
    public void writeFunction(String functionName, int nVars) {
        printer.print("("+ functionName + ")\n");
        for (int i = 0; i < nVars; i++) {
            writePushPop(Parser.PUSH, "constant", 0);
        }
    }

    // Writes assembly code that effects the call command
    public void writeCall(String functionName, int nArgs) {
        String returnADDR = "RETURN_LABEL" + (labelCount++);
        printer.print("@" + returnADDR + "\n" + "D=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n"); // Push return address
        printer.print(pushTemplate("LCL",0, true)); // Push LCL
        printer.print(pushTemplate("ARG",0, true)); // Push ARG
        printer.print(pushTemplate("THIS",0, true)); // Push THIS
        printer.print(pushTemplate("THAT",0, true)); // Push THAT
        printer.print("@SP\n" + 
                        "D=M\n" + 
                        "@5\n" + 
                        "D=D-A\n" + 
                        "@" + nArgs + "\n" +
                        "D=D-A\n" + 
                        "@ARG\n" + 
                        "M=D\n" + // Repositions ARG
                        "@SP\n" + 
                        "D=M\n" + 
                        "@LCL\n" + 
                        "M=D\n" + // Repositions LCL 
                        "@" + functionName + "\n" + 
                        "0;JMP\n" + 
                        "(" + returnADDR + ")\n" // Declares a label for the return address 
        );                   
    }

    // Writes assembly code that effects the return command
    public void writeReturn() {
        printer.print("@LCL\n" +
                "D=M\n" +
                "@R11\n" +
                "M=D\n" +
                "@5\n" +
                "A=D-A\n" +
                "D=M\n" +
                "@R12\n" +
                "M=D\n" +
                popTemplate("ARG",0,false) +
                "@ARG\n" +
                "D=M\n" +
                "@SP\n" +
                "M=D+1\n" +
                positionTemplate("THAT") +
                positionTemplate("THIS") +
                positionTemplate("ARG") +
                positionTemplate("LCL") +
                "@R12\n" +
                "A=M\n" +
                "0;JMP\n"
        );
    }  

    // A helper method - Writes assembly code that effects the position of the given argType
    public String positionTemplate(String position){

        return "@R11\n" +
                "D=M-1\n" +
                "AM=D\n" +
                "D=M\n" +
                "@" + position + "\n" +
                "M=D\n";

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
        String noPointerCode = (isDirect)? "D=A\n" : "D=M\n@" + index + "\nD=D+A\n";
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