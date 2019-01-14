#!/bin/sh


export MAVEN_OPTS="-Xmx16G"
#-Dlog4j.configuration=file:data/fox/log4j.properties"

#ARGS="-c training -i /home/rspeck/Data/data/input"
#ARGS="-c analysis -i /home/rspeck/Data/data/input"
ARGS="-c execution -i /home/rspeck/Data/data/input"

nohup mvn exec:java -Dexec.mainClass="org.aksw.simba.rcc.Rcc" -Dexec.args="$ARGS"> run.log & 
