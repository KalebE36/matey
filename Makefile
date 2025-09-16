run-peer-0:
	@mvn exec:java -Dexec.args="1001"

run-peer-1:
	@mvn exec:java -Dexec.args="1002"

run-peer-2:
	@mvn exec:java -Dexec.args="1003"

compile:
	@mvn clean compile
