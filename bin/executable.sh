#!/bin/sh

COMMANDLINE_ARGS="$@"
EXEC_OPTION=""




# app installation dir
if [ -z "$APP_HOME" ] ; then
  APP_HOME="."
fi

# Configure a user with non root privileges, if no user is specified do not change user
if [ -z "$APP_USER" ] ; then
    APP_USER=""
fi

# app  base dir
if [ -z "$APP_BASE" ] ; then
  APP_BASE="$APP_HOME"
fi


if [ -z "$APP_DATA" ] ; then
    # For backwards compat with old variables we let ACTIVEMQ_DATA_DIR set ACTIVEMQ_DATA
    if [ -z "$APP_DATA_DIR" ] ; then
        APP_DATA="$APP_BASE/data"
    else
        APP_DATA="$APP_DATA_DIR"
    fi
fi

if [ -z "$APP_OUT" ]; then
  APP_OUT="/dev/null"
fi

# Location of the pidfile
if [ -z "$APP_PIDFILE" ]; then
  APP_PIDFILE="$APP_DATA/app.pid"
fi

# Detect the location of the java binary
if [ -z "$JAVACMD" ] || [ "$JAVACMD" = "auto" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  fi
fi

# Hm, we still do not know the location of the java binary
if [ -z "$JAVACMD" ] || [ "$JAVACMD" = "auto" ] || [ ! -x "$JAVACMD" ] ; then
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
fi
# Stop here if no java installation is defined/found
if [ ! -x "$JAVACMD" ] ; then
  echo "ERROR: Configuration variable JAVA_HOME or JAVACMD is not defined correctly."
  echo "       (JAVA_HOME='$JAVA_HOME', JAVACMD='$JAVACMD')"
  exit 1
fi


checkRunning(){
    local pidfile="${1}"

    if [ -f $pidfile ]; then
       if  [ -z "`cat "$pidfile"`" ];then
        echo "ERROR: Pidfile '$pidfile' exists but contains no pid"
        return 2
       fi
       local app_pid="`cat "$pidfile"`"
       local RET="`ps -eo "pid,args" | grep "^\s*$app_pid\s.*java"`"
       if [ -n "$RET" ];then
         return 0;
       else
         return 1;
       fi
    else
         return 1;
    fi
}



help(){
  echo "run to start"
  echo "stop to stop"
  echo "status to show the status"
  echo "log to show the log"
  echo ":)"
}

status(){
  if ( checkRunning "$APP_PIDFILE" );then
    PID="`cat $APP_PIDFILE`"
    echo "APP is running (pid '$PID')"
    exit 0
  fi
  echo "APP not running"
  exit 1
}

setCurrentUser(){
   CUSER="`whoami 2>/dev/null`"
   # Solaris hack
   if [ ! $? -eq 0 ]; then
      CUSER="`/usr/ucb/whoami 2>/dev/null`"
   fi
}

if [ ! -d "$APP_DATA" ]; then
   setCurrentUser
   if ( [ -z "$APP_USER" ] || [ "$APP_USER" = "$CUSER" ] );then
        mkdir "$APP_DATA"
   elif [ "`id -u`" = "0" ];then
      su -c "mkdir $APP_DATA" - "$APP_USER";
   fi
fi

if [ -z "$APP_OPTS" ] ; then
    APP_OPTS="$APP_OPTS_MEMORY "
fi

invokeJar(){
  PIDFILE="$1"

   if [ ! -f "${APP_HOME}/rallibau-ddd-backend.jar" ];then
      echo "ERROR: '${APP_HOME}/rallibau-ddd-backend.jar' does not exist, define APP_HOME in the config"
      exit 1
   fi

   setCurrentUser

    # shellcheck disable=SC2233
    # shellcheck disable=SC2031
    if ( [ -z "$APP_USER" ] || [ "$APP_USER" = "$CUSER" ] );then
         DOIT_PREFIX="sh -c "
         DOIT_POSTFIX=";"
      elif [ "`id -u`" = "0" ];then
         DOIT_PREFIX="su -s /bin/sh -c "
         DOIT_POSTFIX=" - $ACTIVEMQ_USER"
         echo "INFO: changing to user '$APP_USER' to invoke java"
      fi

    # Get Java version
    # Use in priority xpg4 awk or nawk on SunOS as standard awk is outdated
    AWK=awk
    if ${solaris}; then
      if [ -x /usr/xpg4/bin/awk ]; then
        AWK=/usr/xpg4/bin/awk
      elif [ -x /usr/bin/nawk ]; then
        AWK=/usr/bin/nawk
      fi
    fi
    VERSION=`"${JAVACMD}" -version 2>&1 | ${AWK} -F '"' '/version/ {print $2}' | sed -e 's/_.*//g; s/^1\.//g; s/\..*//g; s/-.*//g;'`


    $EXEC_OPTION $DOIT_PREFIX "\"$JAVACMD\" $APP_OPTS $APP_DEBUG_OPTS \
                    -jar \"${APP_HOME}/rallibau-ddd-backend.jar\" >> $APP_OUT --pid $SPID &
                    RET=\"\$?\"; APID=\"\$!\";
                    echo \$APID > "${PIDFILE}";
                    echo \"INFO: pidfile created : '${PIDFILE}' (pid '\$APID')\";exit \$RET" $DOIT_POSTFIX

    return "$?"
}

run(){
  if ( checkRunning "$APP_PIDFILE" );then
      PID="`cat $APP_PIDFILE`"
      echo "INFO: Process with pid '$PID' is already running"
      exit 0
    fi
  invokeJar "$APP_PIDFILE"
  return "$?"

}

stop(){
    if ( checkRunning "$APP_PIDFILE" );then
       ACTIVEMQ_OPTS="$ACTIVEMQ_OPTS $ACTIVEMQ_SSL_OPTS"
       COMMANDLINE_ARGS="$COMMANDLINE_ARGS $ACTIVEMQ_SUNJMX_CONTROL"

       APP_PIDFILE="`cat $APP_PIDFILE`"

       kill -9 "$APP_PIDFILE"
       rm -rf "$APP_DATA"
       echo "APP killed"
       exit 0
    fi
    echo "APP not running"
    exit 1
}

logAPP(){
  tail -f logs/rallibau-ddd-backend.log
}


if [ -z "$1" ];then
 help
fi

case "$1" in
  status)
    status
    ;;
  run)
    run
    ;;
  stop)
    stop
    ;;
  log)
      logAPP
      ;;
  *)
    echo "what??"
    exit $?
esac
