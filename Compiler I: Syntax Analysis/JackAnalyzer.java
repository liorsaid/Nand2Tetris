import java.io.File;
import java.util.ArrayList;

public class JackAnalyzer {
    public static ArrayList<File> getJackFiles(File dir){

        // Creates a new array of all the given jack files
        File[] files = dir.listFiles();
        ArrayList<File> result = new ArrayList<File>();
        if (files == null) return result;
        for (File f:files) {
            if (f.getName().endsWith(".jack")){
                result.add(f);
            }
        }
        return result;
    }

    public static void main(String[] args) {

        // Executed only if given a single file/directory name
        if (args.length != 1) {
            System.out.println("Usage:java JackAnalyzer [filename|directory]");
        } else {
            String fileInName = args[0];
            File fileIn = new File(fileInName);
            String fileOutPath = "", tokenFileOutPath = "";
            File fileOut,tokenFileOut;
            ArrayList<File> jackFiles = new ArrayList<File>();

            // Checks whether the args[0] is a valid jack file name, and if so adds it to jackFiles
            if (fileIn.isFile()) {

                String path = fileIn.getAbsolutePath();
                if (!path.endsWith(".jack")) {
                    throw new IllegalArgumentException(".jack file is required!");
                }
                jackFiles.add(fileIn);

            // Checks whether args[0] is a valid directory name, and checks whether it contains any jack files
            } else if (fileIn.isDirectory()) {
                jackFiles = getJackFiles(fileIn);
                if (jackFiles.size() == 0) {
                    throw new IllegalArgumentException("No jack file in this directory");
                }
            }

            // For all jack files, if exist...
            // (1) Sets tokenFileOutPath to be the name of the given file, tokenized - But with a T.xml extention
            // (2) Sets fileOutPath to be the name of the given file, tokenized and parsed - But with an .xml extention
            for (File f: jackFiles) {

                tokenFileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + "T.xml";
                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".xml";
                tokenFileOut = new File(tokenFileOutPath);
                fileOut = new File(fileOutPath);

                CompilationEngine compilationEngine = new CompilationEngine(f,fileOut,tokenFileOut);
                compilationEngine.compileClass();

                System.out.println("File created: " + fileOutPath);
                System.out.println("File created: " + tokenFileOutPath);
            }
        }
    }
}