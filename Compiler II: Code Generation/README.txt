(A) How to compile the code?

1. Open terminal in VSCode
2. Go to the project's folder
3. Run the following commands:

* javac JackTokenizer.java
* javac CompilationEngine.java
* javac JackCompiler.java
* javac VMWriter.java
* javac SymbolTable.java
* javac Symbol.java

4. After the compilation, the files "JackTokenizer.class", "CompilationEngine.class", "JackCompiler.class", "VMWriter.class", "SymbolTable.class" and "Symbol.class" will appear in the project's folder.

(B) How to run the code?

1. Run the command "java JackCompiler file" or "java JackAnalyzer directory"

Examples:

* Running the command "java JackAnalyzer ../Square/Main.jack" will create the file Main.vm within the directory nand2tetris/projects/11/Square

* Running the command "java JackAnalyzer ../ComplexArrays" will create the file Main.vm within the directory nand2tetris/projects/11/ComplexArrays - That is, because Main.jack is the only jack file within the given folder.

Any translation error that may occur during the run will result in throwing an Exception, detailing the cause of the error.