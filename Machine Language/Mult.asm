// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

// Reset value of R2 to be 0
@R2
M=0

// Find value of R0
@R0
D=M

// Add value of R1 to R2 - R0 times
// To conclude, the value of R2 will be: R1 * R0, as required
(LOOP)
    @END
    D;JEQ
    @R1
    D=M
    @R2
    M=M+D
    @R0
    M=M-1
    D=M
    @LOOP
    D;JGT
(END)





