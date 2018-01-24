#! /bin/sh
# /etc/init.d/zookeeper: start the zookeeper daemon.
export HOSTNAME=$(hostname)

ZK_HOME=/opt/kafka
ZK_USER=root
ZK_SCRIPT=$ZK_HOME/bin/zookeeper-server-start.sh
ZK_CONFIG=$ZK_HOME/config/zookeeper.properties
ZK_CONSOLE_LOG=/var/log/zookeeper.log

PATH=/bin:/usr/bin:/sbin:/usr/sbin:/usr/local/bin

prog=zookeeper
DESC="zookeeper daemon"

RETVAL=0
STARTUP_WAIT=30
SHUTDOWN_WAIT=30

ZK_PIDFILE=/var/log/zookeeper.pid


# Source function library.
. /etc/init.d/functions

start() {
  echo -n $"Starting $prog: "

        # Create pid file
        if [ -f $ZK_PIDFILE ]; then
                read ppid < $ZK_PIDFILE
                if [ `ps --pid $ppid 2> /dev/null | grep -c $ppid 2> /dev/null` -eq '1' ]; then
                        echo -n "$prog is already running"
                        failure
                        echo
                        return 1
                else
                      rm -f $ZK_PIDFILE
                fi
        fi

	rm -f $ZK_CONSOLE_LOG
	mkdir -p $(dirname $ZK_PIDFILE)

	# Run daemon
	cd $ZK_HOME
	nohup sh $ZK_SCRIPT $ZK_CONFIG 2>&1 >> $ZK_CONSOLE_LOG 2>&1 &
	PID=$!
	echo $PID > $ZK_PIDFILE

	count=0
	launched=false

	until [ $count -gt $STARTUP_WAIT ]
	do
		grep 'binding to port' $ZK_CONSOLE_LOG > /dev/null
		if [ $? -eq 0 ] ; then
                	launched=true
                	break
		fi
		sleep 1
		let count=$count+1;
	done

	success
	echo
	return 0
}

stop() {
        echo -n $"Stopping $prog: "
        count=0;

        if [ -f $ZK_PIDFILE ]; then
                read kpid < $ZK_PIDFILE
                let kwait=$SHUTDOWN_WAIT

                # Try issuing SIGTERM
                kill -15 $kpid
                until [ `ps --pid $kpid 2> /dev/null | grep -c $kpid 2> /dev/null` -eq '0' ] || [ $count -gt $kwait ]
                        do
                        sleep 1
                        let count=$count+1;
                done

                if [ $count -gt $kwait ]; then
                        kill -9 $kpid
                fi
        fi

        rm -f $ZK_PIDFILE
        rm -f $ZK_CONSOLE_LOG
        success
        echo
}

reload() {
	stop
	start
}

restart() {
	stop
	start
}

status() {
	if [ -f $ZK_PIDFILE ]; then
		read ppid < $ZK_PIDFILE
		if [ `ps --pid $ppid 2> /dev/null | grep -c $ppid 2> /dev/null` -eq '1' ]; then
			echo "$prog is running (pid $ppid)"
			return 0
		else
			echo "$prog dead but pid file exists"
			return 1
		fi
	fi
	echo "$prog is not running"
	return 3
}

case "$1" in
start)
	start
	;;

stop)
	stop
	;;

reload)
	reload
	;;

restart)
	restart
	;;

status)
	status
	;;
*)

echo $"Usage: $0 {start|stop|reload|restart|status}"
exit 1
esac

exit $?