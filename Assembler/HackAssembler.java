import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HackAssembler {
	public static int counter = 0;
	public static int nextRam = 16;
	public static String compT, destT, jumpT;
	public static void main(String[] args) {
	
		String name = args[0].substring(0, args[0].indexOf('.'));	// Copies name of given file
		String outFileName = name + ".hack";  // Output - file name
		SymbolTable st = new SymbolTable(); // Init's symbol table
		Code codeTable = new Code();  // Init's code tables
		Parser Parse = new Parser(args[0]);  // New parser 
		File out = new File(outFileName);  // 0utput - hack file	
		FileWriter Fwriter = null;
		try {
            if (out.exists()) {
                out.delete();
            }
            out.createNewFile();
			Fwriter = new FileWriter(out.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter Bwriter = new BufferedWriter(Fwriter);
	
		// First pass on the file 
		while (Parse.hasMoreLines()) {  
		  if (Parse.comType() == Parser.commandType.L_COMMAND) { 
			st.addEntry(Parse.symbol(), counter) ; // Adds a new symbol to the symbol table
		  }
		  else counter++; // Advancing to the next line
		  Parse.advance();  // Advancing to the next command
		}
		Parse.lineCount = 0;   // Resets the counter and starts from first line

		// Second pass on the file
		while (Parse.hasMoreLines())	{
			if (Parse.comType() == Parser.commandType.A_COMMAND) { 
				{
					if (Parse.strFileArr[Parse.lineCount].startsWith("@"))
					{
					String tmp  = Parse.symbol(); 
						if (Parse.isNum(tmp)) {
							int xxx = Integer.parseInt(tmp);
							tmp = Parser.dexToBin(xxx);	
							tmp = Parse.addZero(tmp);
							try {
								Bwriter.write(tmp + '\n'); // write to hack
							} catch (IOException e) {
								e.printStackTrace();
							}	
						}
						else {
							if (!st.contains(tmp)) { 
								st.addEntry(tmp, nextRam);  
								nextRam++;
							}
							 if (st.contains(tmp)) { 
								int xxx = st.getAddress(tmp);
								String tmp2 = Parser.dexToBin(xxx);
								tmp2 = Parse.addZero(tmp2);
								try {
									Bwriter.write(tmp2+'\n'); // write to hack
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}	
			if (Parse.comType() == Parser.commandType.C_COMMAND) {
				if (Parse.strFileArr[Parse.lineCount].contains("=")) { // dest=comp	
					destT = codeTable.dest(Parse.dest());
					compT = codeTable.comp(Parse.comp());
					jumpT = codeTable.jump("NULL");  // No need for jump
					try {
						Bwriter.write("111" + compT + destT + jumpT +'\n');
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else if (Parse.strFileArr[Parse.lineCount].contains(";")) { // jump
					destT = codeTable.dest("NULL"); // No need for dest
					compT = codeTable.comp(Parse.comp());
					jumpT = codeTable.jump(Parse.jump());
					try {
						Bwriter.write("111" + compT + destT + jumpT +'\n');
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			Parse.advance(); // Advancing to the next line of the parser	
		}
		
        // Closing the file
        try {
	      Bwriter.close();
        } catch (IOException e) {
	      e.printStackTrace();
        }
	}
}