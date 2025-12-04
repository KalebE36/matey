# Alex Desktop
run-peer-0:
	@mvn exec:java -Dexec.args="1001"

# Alex Desktop
run-peer-1:
	@mvn exec:java -Dexec.args="1002"

# Alex Desktop
run-peer-2:
	@mvn exec:java -Dexec.args="1003"

# Alex thinkpad
run-peer-3:
	@mvn exec:java -Dexec.args="1004"

# Kaleb
run-peer-4:
	@mvn exec:java -Dexec.args="1005"

# Edward
run-peer-5:
	@mvn exec:java -Dexec.args="1006"

compile:
	@mvn clean compile

format:
	@mvn spotless:apply

check-format:
	@mvn spotless:check
