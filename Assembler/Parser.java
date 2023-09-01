import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParsePosition;

public class Parser {

    private  String strFile;  // Reading the file, line by line
	public  String strFileArr[];  // Setting string array
	public int lineCount; // Line counter
	public BufferedReader br;
	public static commandType comType;  // A,C,L Commands
	public static int symbValue  = 16; // Symbol value

    public Parser(String File) {
        lineCount = 0;
		FileInputStream fstream = null; 
		try {
			fstream = new FileInputStream(File);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		// Converts int content to string
		int content;
        try {
			while ((content = fstream.read()) != -1) {
			    strFile += (char) content; 	// Converts to char
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
         
		    // Gets the object of DataInputStream
		    DataInputStream in = new DataInputStream(fstream);
		    br = new BufferedReader(new InputStreamReader(in));
		    
		    
		   strFile =  removeComments(strFile);
		   // Copies string to string array
		   strFileArr = strFile.split("\n");
		   for(int i=0; i < strFileArr.length; i++){
			   strFileArr[i] =  strFileArr[i].trim();
       }
    }

    // Checks if the given file has more lines
    public boolean hasMoreLines() {
        if (lineCount != strFileArr.length) 
          return true;
		return false;
    }

    // Advances the file to the next line (if exists)
    public void advance() {
        lineCount++;
    }

    // Checks for the command type of the current line
    public commandType comType() {
        if(strFileArr[lineCount].startsWith("(")) {
			return comType = commandType.L_COMMAND;
		}
		else if(strFileArr[lineCount].startsWith("@")) {
			return comType = commandType.A_COMMAND;
		}
		return commandType.C_COMMAND;
    }

    // Translates the symbols of the given program
    public String symbol() {
        String retLabel = "";
		if(strFileArr[lineCount].startsWith("@")) {
			retLabel = strFileArr[lineCount];
			retLabel = retLabel.replaceAll("@", "");
		}
		else 
			if(strFileArr[lineCount].startsWith("(")) {
				retLabel = strFileArr[lineCount];
				retLabel = retLabel.replaceAll("\\((.*?)\\)", "$1");
			}
		return retLabel;
    }

    // Handling dest strings 
    public String dest() {
        if(strFileArr[lineCount].contains("=")) {
		  String retDest = strFileArr[lineCount];
		  int endIndex = retDest.lastIndexOf("=");
		  retDest =  retDest.substring(0,endIndex);
		  return retDest;
		}
		return null;
    }

    // Handling comp strings
    public String comp() {
        String retComp = strFileArr[lineCount]; 
		if (strFileArr[lineCount].contains("=")) {
		  int endIndex = retComp.lastIndexOf("=");
		  retComp =  retComp.substring(endIndex+1,retComp.length());
		}
		else if (strFileArr[lineCount].contains(";")) {
			int endIndex = retComp.lastIndexOf(";");
			retComp =  retComp.substring(0,endIndex);
		}
		return retComp;
    }

    // Handling jump strings
    public String jump() {
        if (strFileArr[lineCount].contains(";")) {
			 String retJump = strFileArr[lineCount]; 
			 int endIndex = retJump.lastIndexOf(";");
			 return retJump.substring(endIndex+1,retJump.length());
		}
		return null;
    }

    // Removing comments from the given file
    public String removeComments(String file) {
		String tmpFile =  file.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/|(?m)^[ \t]*\r?\n|null|\t", "" );
		tmpFile = tmpFile.replaceAll("(?m)^[ \t]*\r?\n", "");
        return tmpFile;
	}

	// Decimal to binary converter
	public static String dexToBin(int value) {
		String binVal = Integer.toBinaryString(value);
			return binVal;
			
		}
		// Check's if number
		public boolean isNum(String num)
		{
			NumberFormat formatter = NumberFormat.getInstance();
			  ParsePosition pos = new ParsePosition(0);
			  formatter.parse(num, pos);
			  return  num.length() == pos.getIndex();
			
		}
		
		// Adds zeroes
		public String addZero(String num)
		{
			StringBuilder sb = new StringBuilder();
	
			for (int toPrepend=16-num.length(); toPrepend>0; toPrepend--) {
				sb.append('0');
			}
	
			sb.append(num);
			String result = sb.toString();
			return result;
		}
	
	// Command type, enums
	public enum commandType {
		A_COMMAND,L_COMMAND,C_COMMAND
	}
}