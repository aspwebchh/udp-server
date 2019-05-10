#!/bin/sh

#JDK path
JAVA_HOME=/data/app/java/jdk8

#MAIN PROGRAMM
APP_JAR=logViewServer

host_ip=192.168.11.237

#init psid var
psid=0
##################
#search programm pid
##################
checkpid() {
   j_ps=$($JAVA_HOME/bin/jps -l | grep $APP_JAR)
   #j_ps=$(ps aux | grep $APP_JAR)

   if [ -n "$j_ps" ]; then
      psid=$(echo $j_ps | awk '{print $1}')
   else
      psid=0
   fi
}
##################
#start programm
##################
start() {
   checkpid

   if [ $psid -ne 0 ]; then
      echo "================================"
      echo "warn: $APP_JAR already started! (pid=$psid)"
      echo "================================"
   else
      echo -n "Starting $APP_JAR ..."
           nohup $JAVA_HOME/bin/java -Dfile.encoding=utf-8  -jar logViewServer.jar -name $APP_JAR > server.log 2>&1 &
      checkpid
      if [ $psid -ne 0 ]; then
         echo "(pid=$psid) [OK]"
         mkdir ./running
      else
         echo "[Failed]"
      fi
   fi
}
##################
#stop programm
##################
stop() {
   checkpid

   if [ $psid -ne 0 ]; then
      echo -n "Stopping $APP_JAR ...(pid=$psid) "
      #su - $RUNNING_USER -c "kill -9 $psid"
          kill -9 $psid
      if [ $? -eq 0 ]; then
         echo "[OK]"
         rm -rf ./running
      else
         echo "[Failed]"
      fi

      checkpid
      if [ $psid -ne 0 ]; then
         stop
      fi
   else
      echo "================================"
      echo "warn: $APP_JAR is not running"
      echo "================================"
   fi
}
##################
#programm status
##################
status() {
   checkpid

   if [ $psid -ne 0 ];  then
      echo "$APP_JAR is running! (pid=$psid)"
   else
      echo "$APP_JAR is not running"
   fi
}
##################
#programm env info
##################
info() {
   echo "System Information:"
   echo "****************************"
   echo $(head -n 1 /etc/issue)
   echo $(uname -a)
   echo
   echo "JAVA_HOME=$JAVA_HOME"
   echo $($JAVA_HOME/bin/java -version)
   echo
   echo "APP_JAR=$APP_JAR"
   echo "****************************"
}

##################
#programm start
##################
case "$1" in
   'start')
      start
      ;;
   'stop')
     stop
     ;;
   'restart')
     stop
     start
     ;;
   'status')
     status
     ;;
   'info')
     info
     ;;
   'checkpid')
     checkpid
     echo $psid
     ;;
   *)
     echo "Usage: $0 {start|stop|restart|status|info}"
     exit 1
esac
exit 0

