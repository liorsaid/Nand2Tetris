(A) How to compile the code?

1. Open terminal in VSCode
2. Go to the project's folder
3. Run the following commands:

* javac Parser.java
* javac CodeWriter.java
* javac VMTranslator.java

4. After the compilation, the files "Parser.class", "CodeWriter.class" and "VMTranslator.class" will appear in the project's folder.

(B) How to run the code?

1. Run the command "java VMTranslator file" or "java VMTranslator directory"

Examples:

* Running the command "java VMTranslator FunctionCalls/FibonacciElement/Sys.vm" will create the file Sys.asm within the directory nand2tetris/projects/08/FunctionCalls/FibonacciElement

* Running the command "java VMTranslator ProgramFlow/BasicLoop" will create only the file BasicTest.asm within the directory nand2tetris/projects/08/ProgramFlow/BasicLoop - That is, because BasicLoop.vm is the only vm file within the given folder.

Any translation error that may occur during the run will result in throwing an Exception, detailing the cause of the error.