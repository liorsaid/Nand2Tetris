// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed. 

(ALLPIXELS)
  // Sending the pixels to be brightened if @KBD=0
  // Sending the pixels to be blackened otherwise
  @KBD
  D=M
  @WHITE
  D;JEQ
  @BLACK
  D;JGT

(WHITE)
  // Brightening the pixels
  @8191
  D=A
  @SCREEN
  A=A+D
  D=A
  (BRIGHTENING)
    // Iterating on all the pixels, Brightening them pixel-by-pixel
    A=D
    M=0
    D=D-1
    // While D>=0, the loop goes on
    @BRIGHTENING
    D;JGE
    // When d<0, the loops stops... to be initiated again
    @ALLPIXELS
    D;JLT
  
(BLACK)
  // Blackening the pixels
  @8191
  D=A
  @SCREEN
  A=A+D
  D=A
  (BLACKENING)
    // Iterating on all the pixels, Blackening them pixel-by-pixel
    A=D
    M=-1
    D=D-1
    // While D>=0, the loop goes on
    @BLACKENING
    D;JGE
    // When d<0, the loops stops... to be initiated again
    @ALLPIXELS
    D;JLT






