#!/bin/sh
java -Dfile.encoding=UTF-8 \
     -XX:MaxGCPauseMillis=100 \
	 -Xmx800m -Xms800m -Xmn700m \
     -XX:+UseConcMarkSweepGC -XX:-UseParNewGC -XX:+ExplicitGCInvokesConcurrent \
	 -XX:+PrintGCDateStamps -verbose:gc -XX:+PrintGCDetails -Xloggc:"gc.log" \
     -jar SoftwareChallenge.jar $@
cat "gc.log"