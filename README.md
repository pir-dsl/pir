Multimedia Information Retrieval (MIR) experiments on large data sets are resource intensive and time consuming. Recent development of distributed computing platforms such as Cloud services offers great opportunity to improve runtime performance. However, adapting existing MIR applications to such platforms is difficult even for embarrassingly parallel problems.

PIR helps deploy MIR applications onto distributed platforms directly without code change. The DSL programs are sequential and the DSL ``compiler" transforms each DSL program to a pipeline graph that can be executed in distributed platforms.

The expressiveness and effectiveness of the DSL have been evaluated by deploying MIR applications written in the DSL on Amazon EC2. A MapReduce framework -- Spark is utilized to distribute the parallel tasks such as data loading and feature extraction to Amazon EC2 worker nodes. The results show that the DSL programs can achieve good performance scalability on distributed platforms.

The step-by-step procedure to run PIR in Spark on Amazon EC2 on a Linux/unix system is as follows:

1. in your unix console, execute the below for later convenience (apparently you need to replace the placeholder ${...} with your actual values)

	```Shell
	export AWS_ACCESS_KEY_ID=${your_access_key_ID_VALUE}
	export AWS_SECRET_ACCESS_KEY=${your_secret_ACCESS_KEY_VALUE}
	```

2. Download Spark 1.0 binary to a folder (e.g. spark-1.0-incubating-bin-hadoop1);
3. Go to Amazon AWS to create a new keypair file (pir-keypair.pem) and copy the saved private key file into the Spark ec2 folder;
4. run ```chmod 600 pir-keypair.pem ```;
5. run ```./spark-ec2 -k pir-keypair -i pir-keypair.pem -s 4 launch pir ``` to launch the cluster on AWS (where 4 is the number of worker nodes we plan to spawn)
(you will see the dynamic ```host_name ``` once you finish step 5, please record it for usage in step 9);
//The below step 6 is optional and not needed if elastic IP is not used (please read Amazon EC2 documentation about elastic IP for details)
6. Associate a elastic IP with the master instance (do the same for the slave node) so we can use this ip as the hostname in the code;
7. run ```./spark-ec2 -k pir-keypair -i pir-keypair.pem login pir ``` to SSH onto the the machine;
8. run ```git clone https://github.com/pir-dsl/pir.git ``` and ```cd ``` to the pir folder;
9. edit the ```host_home ``` value in env.conf file to be the running instance host; 
10. run ```./sbt/sbt clean update assembly ``` to build the pir jar

To test and use PIR, we have several scripts ready out-of-the-box (Please execute them right in the EC2 master node after step 10):
```Shell
exec_ImageQuery.sh 
		: execute image index and query with Lire CEDD & Lire feature combined using Lucene index and the index/query steps are sequentially executed;
```
```Shell
exec_ImageQuerySift.sh 
		: execute image index and query with Lire SIFT feature;
```
```Shell
exec_NaiveIndexQuery.sh
		: execute image index and query with Lire CEDD & Lire feature combined with built-in index and query and hence the index/query steps are distributed;
```
```Shell
exec_NaiveIndexQuerySift.sh
		: execute image index and query with Lire SIFT feature combined with built-in index and query and hence the index/query steps are distributed;
```
```Shell
exec_Transmedia.sh
		: execute transmedia query with Sift image feature and LDA text feature combined;
```
```Shell
exec_SFA.sh
		: execute Series feature aggregation content-based image retrieval
```
Once you start one of the above scripts, you can open a browser and use http://{host_name}:8080 to view the running instances and runtime execution details (where host_name is the same as what you recored in step 5). 

Several caveats: 

1. If anything goes wrong, you can always exit from ssh remote (step 7) back to your local console and execute  ```./spark-ec2 stop pir ``` (```./spark-ec2 destroy ``` will terminate and kill everything for the instance) to stop the cluster and do thing again 
(Please do double-check your EC2 instances from the Amazon EC2 web console to make sure the instances have been deleted as otherwise you will pay for the hanging instance!).

2. After stopped the cluster, you can run ```./spark-ec2 -i pir-keypair.pem start pir ``` to restart the cluster

3. The ```spark_partition_size ``` (which is the # of slices of spark data) parameter in the env.conf file can be modified based on your needs 

4. The Amazon S3 bucket_name parameter in env.conf file can be modified; currently iaprtc12 contains data from ImageClef (around 20,000 images/annotations)  while PirData contains data from UCSD (around 2800 image/text) 
 
PIR is under pure GNU 2 license currently. 

We kindly ask you to refer the following paper in any publication/software mentioning PIR:

X. Huang, T. Zhao, and Y. Cao, "PIR: A Domain Specific Language for Multimedia Retrieval" in
IEEE International Symposium on Multimedia (ISM2013), Anaheim, California USA  December, 2013
Pease direct any questions in usage of PIR to xiaobing@uwm.edu.
