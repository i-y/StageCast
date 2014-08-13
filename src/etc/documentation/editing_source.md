Guide to Editing Program Source Code {#editing_source}
====================================

The source code defines the program. There is a lot of it and even educated readers will probably need to dedicate some time to learning it. The purpose of this set of documentation is to make this task easier. One area of critical importance in this is the readability of the source code, that is, how easy is it for a first-time reader to follow and understand a certain piece of code. As part of readability, a standard  coding style is usually desirable. No style is inherently superior to any others, but maintaining consistency is better than abruptly going back and forth between styles or failing to have any sort of consistent style at all.

The original development of this program attempted to follow the style outlined below. Of course, individual developers are under no obligation to follow these guidelines if they do not wish to; however, it is encouraged for code submitted back to the project for the reasons outlined above.

**Style Guide**
 - Opening brackets are on the same line as the opening statement and closing brackets are on their own line.

        int foo() {
            x = 0;
            for(int i = 0; i < 10; i++) {
                x++;
            }
            return(x);
        }
 - There should be no empty line between the documentation for a method and the start of that method.
 - There should be no empty lines within methods.
 - There should be one empty line following the closing brace of a method.
 - Indents are one tab per indent level.
 - Methods should be declared in alphabetic order.
 - Variable declarations should be grouped into private and public sets.
  - Within these sets, the variables should be declared in alphabetic order.
  - There should be no empty lines within these sets but there should be an empty line between them.
 - Variable declaration blocks are set off from the start of the method documentation by a single empty line.
 - Import statements should be grouped based on whether it is importing from the Java library or from a local package.
  - Like variables, these import blocks are separated by an empty line and set off from all other elements by an empty line.
 - Compiler or IDE warnings should be dealt with if at all feasible.