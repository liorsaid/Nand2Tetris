import java.io.File;
import java.util.ArrayList;

public class VMTranslator {

    // Returns a list of all .vm files in a given directory
    public static ArrayList<File> AllFiles(File dir){
        File[] files = dir.listFiles();
        ArrayList<File> vmfiles = new ArrayList<File>();
        for (File f:files){
            if (f.getName().endsWith(".vm")) {
                vmfiles.add(f);
            }
        }
        return vmfiles;
    }

    public static void main(String[] args) {

        // Executed only if given a single file/directory name
        if (args.length != 1) {
            System.out.println("Usage:java VMtranslator [filename|directory]");
        } else {
            String fileInName = args[0];
            File fileIn = new File(fileInName);
            String fileOutPath = "";
            File fileOut;
            CodeWriter writer;
            ArrayList<File> VMFiles = new ArrayList<File>();

            // Checks whether the args[0] is a valid vm file name, and if so adds it to VMFiles
            // and sets fileOutPath to be the name of the given file - But with an .asm extention
            if (fileIn.isFile()) {
                String path = fileIn.getAbsolutePath();
                if (!Parser.Extension(path).equals(".vm")) {
                    throw new IllegalArgumentException(".vm file is required!");
                }
                VMFiles.add(fileIn);
                fileOutPath = fileIn.getAbsolutePath().substring(0, fileIn.getAbsolutePath().lastIndexOf(".")) + ".asm";
            
            // Checks whether args[0] is a valid directory name, checks whether it contains any vm files,
            // and if so sets fileOutPath to be the name of the vm file in the given folder -
            // But with an .asm extention
            } else if (fileIn.isDirectory()) {
                VMFiles = AllFiles(fileIn);
                if (VMFiles.size() == 0) {
                    throw new IllegalArgumentException("No .vm file in this directory");
                }
                fileOutPath = fileIn.getAbsolutePath() + "/" +  fileIn.getName() + ".asm";
            }
            fileOut = new File(fileOutPath);
            writer = new CodeWriter(fileOut);

            // VM initialization
            writer.writeInit();

            // For all vm files - Parses each line of the file and writes the corresponding 
            // assembly commands, according to the commandType given in each line 
            for (File f : VMFiles) {
                Parser parser = new Parser(f);
                writer.setFileName(f.getName());
                int type = -1;
                while (parser.hasMoreLines()) {
                    parser.advance();
                    type = parser.commandType();
                    if (type == Parser.ARITHMETIC) {
                        writer.writeArithmetic(parser.arg1());

                    } else if (type == Parser.POP || type == Parser.PUSH) {
                        writer.writePushPop(type, parser.arg1(), parser.arg2());

                    } else if (type == Parser.LABEL) {
                        writer.writeLabel(parser.arg1());
                        
                    } else if (type == Parser.GOTO) {
                        writer.writeGoto(parser.arg1());

                    } else if (type == Parser.IF) {
                        writer.writeIf(parser.arg1());

                    } else if (type == Parser.RETURN) {
                        writer.writeReturn();

                    } else if (type == Parser.FUNCTION) {
                        writer.writeFunction(parser.arg1(),parser.arg2());

                    } else if (type == Parser.CALL) {
                        writer.writeCall(parser.arg1(),parser.arg2());
                    }
                }
            }

            // Closes the created file - A CodeWriter method
            writer.close();
        }
    }
}