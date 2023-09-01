import java.io.File;
import java.util.ArrayList;

public class JackCompiler {
    public static ArrayList<File> getJackFiles(File dir) { 

        // Creates a new array of all the given jack files
        File[] files = dir.listFiles();
        ArrayList<File> result = new ArrayList<File>();
        if (files == null) return result;
        for (File f:files){
            if (f.getName().endsWith(".jack")){
                result.add(f);
            }
        }
        return result;
    }

    public static void main(String[] args) { 

        // Executed only if given a single file/directory name
        if (args.length != 1){
            System.out.println("Usage:java JackCompiler [filename|directory]");
        } else {
            String fileInName = args[0];
            File fileIn = new File(fileInName);
            String fileOutPath = "";
            File fileOut;
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
            // Sets fileOutPath to be the name of the given file - But with a .vm extention
            for (File f: jackFiles) {
                fileOutPath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(".")) + ".vm";
                fileOut = new File(fileOutPath);
                CompilationEngine compilationEngine = new CompilationEngine(f, fileOut);
                compilationEngine.compileClass();
                System.out.println("File created: " + fileOutPath);
            }
        }
    }
}