Guide to Editing Program Documentation {#editing_doc}
======================================

This program's documentation is based around a program called doxygen. What this allows us to do is to write the documentation inside the source code itself, to be processed externally by doxygen to turn in to the html pages you are currently reading. Besides producing attractive documentation, the primary advantage of doxygen's choice to place documentation and code together is that it increases aid of maintenance. Since the documentation is right next to the code it describes, both can be changed together, rather than forcing the programmer to go leafing through an external document to find the line that must be updated. A secondary advantage of this is the regular advantage to leaving good comments in the source code. Since doxygen documentation is designed to be read in their plain format as regular comments, they are as instructive to people reading the code as they are to people reading the standalone html documentation.

Part of having maintainable documentation is trying to have a unified standard for how that documentation should be written. Outlined below is the style guide followed in the creation of the original program. Of course, individual developers are under no obligation to follow these guidelines if they do not wish to; however, it is encouraged for code submitted back to the project since it maintains the readability of the code.



**Guidelines**
 - All block comments will follow the javadoc style. 

        /**
         * comment
         */

 - All doxygen commands will be prefaced with an `@` such as in the command `@``brief`.
 - The value of `@``date` will be the date the member was first documented.
 - The comment blocks do not contain any blank lines except for the lines containing the opening or closing marks.
 - Variables should be commented in an inline manner using `/**< */` to the right of the variable declaration unless a multi-line comment block is necessary.
 - All parameters should be documented with the `@``param` command.
  - However, the return statement does not need to be documented unless it is doing something very out of the ordinary.
 - If there is little to be said about a method or class, make a `@``brief` entry but not a `@``details` entry.
 - The documentation must be written in a manner accessible to people with no formal training in computer science but who are otherwise educated and skilled (i.e. the "educated layperson").
 - The code should be self-documenting (i.e. the name tells the function) but the actual comments should err on the side of speaking too much as opposed to speaking to little.
 - The `@``brief` command should come first followed by the `@``details` command if needed. Everything else follows from there.
 - Each class documentation block should include the author, the date it was documented, and the license it is being released under.
  - The commands for this are `@``author`, `@``date`, `@``copyright`.
 - When in doubt about a certain bit of functionality in the code, flag it with either `@``note`,`@``warning`, or `@``bug`, depending on which seems most appropriate.
 - Pages containing markdown script but no code documentation, such as this one, should be put into their own files with the `.mk` file extention and put into the /src/documentation folder.
 - When a change has been made to either the code documentation or a markdown page, the coder who changed it should run doxygen to ensure that the new edits are displayed correctly.
 - Packages should only be documented in one place, preferably the largest or most important class in that package.
  - For example, the @ref main main package is documented in the @ref Main.java class.