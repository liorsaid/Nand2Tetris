(A) How to compile the code?

1. Open terminal in VSCode
2. Go to the project's folder
3. Run the following commands:

* javac JackTokenizer.java
* javac CompilationEngine.java
* javac JackAnalyzer.java

4. After the compilation, the files "JackTokenizer.class", "CompilationEngine.class" and "JackAnalyzer.class" will appear in the project's folder.

(B) How to run the code?

1. Run the command "java JackAnalyzer file" or "java JackAnalyzer directory"

Examples:

* Running the command "java JackAnalyzer ../Square/Main.jack" will create the files Main.xml and MainT.xml within the directory nand2tetris/projects/10/Square

* Running the command "java JackAnalyzer ../ArrayTest" will create the files Main.xml and MainT.xml within the directory nand2tetris/projects/10/Square - That is, because Main.jack is the only jack file within the given folder.

Any translation error that may occur during the run will result in throwing an Exception, detailing the cause of the error.