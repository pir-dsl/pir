Multimedia Information Retrieval (MIR) experiments on large data sets are resource intensive and time consuming. Recent development of distributed computing platforms such as Cloud services offers great opportunity to improve runtime performance. However, adapting existing MIR applications to such platforms is difficult even for embarrassingly parallel problems.

PIR helps deploy MIR applications onto distributed platforms directly without code change. The DSL programs are sequential and the DSL ``compiler" transforms each DSL program to a pipeline graph that can be executed in distributed platforms.

The expressiveness and effectiveness of the DSL have been evaluated by deploying MIR applications written in the DSL on Amazon EC2. A MapReduce framework -- Spark is utilized to distribute the parallel tasks such as data loading and feature extraction to Amazon EC2 worker nodes. The results show that the DSL programs can achieve good performance scalability on distributed platforms.

PIR is under pure GNU 2 license currently. 

We kindly ask you to refer the following paper in any publication/software mentioning PIR:

X. Huang, T. Zhao, and Y. Cao, "PIR: A Domain Specific Language for Multimedia Retrieval" in
IEEE International Symposium on Multimedia (ISM2013), Anaheim, California USA  December, 2013

Pease direct any questions in usage of PIR to xiaobing@uwm.edu.
