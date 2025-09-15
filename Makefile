run-peer-0:
	@mvn exec:java -Dexec.args="1001"

run-peer-1:
	@mvn exec:java -Dexec.args="1002"	

compile:
	@mvn clean compile
