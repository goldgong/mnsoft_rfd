#!/bin/bash

SPARK_HOME=/home/v2x/apps/spark
V2X_HOME=/home/v2x/v2x/analysis

printUsage()
{
        echo "Usage : $0 [start|stop]"
        exit 0
}

if [ $# -lt 1 ]
then
        printUsage $*
fi

export WEBUI_PORT=4040
export APP_NAME=v2x-analysis
export JAR_NAME=v2x-analysis.jar
export MAIN_CLASS=com.skt.v2x.analysis.SparkStreamingMain
export LOG4J_CONFIGURATION=log4j.xml

export CLASSPATH=.:$V2X_HOME/conf:$V2X_HOME/lib/server/$JAR_NAME:$V2X_HOME/lib/jars/*

echo $CLASSPATH

case "$1" in
        start)

nohup $SPARK_HOME/bin/spark-submit \
 --properties-file $V2X_HOME/conf/spark-defaults.conf \
 --driver-class-path $CLASSPATH \
 --driver-java-options "-Dspark.executor.extraClassPath=$CLASSPATH -Dlog4j.configuration=$LOG4J_CONFIGURATION" \
 --class $MAIN_CLASS \
 $V2X_HOME/lib/server/$JAR_NAME > /dev/null 2>&1 &
echo -n $! > $V2X_HOME/conf/v2x-analysis.pid 
echo [`cat $V2X_HOME/conf/v2x-analysis.pid`] start
                ;;
        local)

$SPARK_HOME/bin/spark-submit \
 --properties-file $V2X_HOME/conf/spark-defaults.conf \
 --driver-class-path $CLASSPATH \
 --driver-java-options "-Dspark.executor.extraClassPath=$CLASSPATH -Dlog4j.configuration=$LOG4J_CONFIGURATION" \
 --class $MAIN_CLASS \
 $V2X_HOME/lib/server/$JAR_NAME -name $APP_NAME 
                ;;

        stop)

if [ -f "$V2X_HOME/conf/v2x-analysis.pid" ]
then
        echo [`cat $V2X_HOME/conf/v2x-analysis.pid`] stop
        kill -9 `cat $V2X_HOME/conf/v2x-analysis.pid`
fi

                ;;
        *)
                printUsage $*
                ;;
esac
