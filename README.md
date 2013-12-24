PIR (Pipeline Information Retrieval) is a software framework that aims at providing a Scala-based high-level programming
language for Multimedia Information Retrieval (MIR) domain. Specifically, PIR is hosted in Scala and allows the user to perform
complex MIR tasks by writing simple and straightforward script-like statements. Out of the box, PIR supports a few MIR libraries (e.g. Lire and Mallet)
Users are also able to plug in their own favorite software packages into the system with simple customizing. Probably the biggest 
advantage of PIR is that its two stage execution strategy. PIR first "compiles" user's code into a execution graph which can then be 
optimized (e.g. parallellization). By only focusing on high-level constructs of the program, users can focus on the domain problems while
the implementation details can be analyzed and optimized separately in compile time and hence the execution performance can be hugely
increased.

PIR is under pure GNU 2 license currently. 

We kindly ask you to refer the following paper in any publication/software mentioning PIR:

X. Huang, T. Zhao, and Y. Cao, "PIR: A Domain Specific Language for Multimedia Retrieval" in
IEEE International Symposium on Multimedia (ISM2013), Anaheim, California USA  December, 2013

Pease direct any questions in usage of PIR to xiaobing@uwm.edu.