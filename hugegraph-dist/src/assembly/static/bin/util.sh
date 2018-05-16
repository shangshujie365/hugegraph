#!/bin/bash

#read a property from a .properties file
function read_property(){
    # replace "." to "\."
    property_name=`echo $2 | sed 's/\./\\\./g'`
    # file path
    file_name=$1;
    cat $file_name | sed -n -e "s/^[ ]*//g;/^#/d;s/^$property_name=//p" | tail -1
}

function process_num() {
    num=`ps -ef | grep $1 | grep -v grep | wc -l`
    return $num
}

function process_id() {
    pid=`ps -ef | grep $1 | grep -v grep | awk '{print $2}'`
    return $pid
}

# check the port of rest server is occupied
function check_port() {
    local port=`echo $1 | awk -F':' '{print $3}'`
    lsof -i :$port >/dev/null

    if [ $? -eq 0 ]; then
        echo "The port "$port" has already used"
        exit 1
    fi
}

function crontab_append() {
    local job="$1"
    (crontab -l ; echo "$job") | crontab -
}

function crontab_remove() {
    local job="$1"
    crontab -l | grep -Fv "$job"  | crontab -
}

# wait_for_startup friendly_name host port timeout_s
function wait_for_startup() {
    local server_name="$1"
    local server_url="$2"
    local timeout_s="$3"

    local now_s=`date '+%s'`
    local stop_s=$(( $now_s + $timeout_s ))

    local status

    echo -n "Connecting to $server_name ($server_url)"
    while [ $now_s -le $stop_s ]; do
        echo -n .
        status=`curl -o /dev/null -s -w %{http_code} $server_url`
        if [ $status -eq 200 ]; then
            echo "OK"
            return 0
        fi
        sleep 2
        now_s=`date '+%s'`
    done

    echo "The operation timed out when attempting to connect to $server_url" >&2
    return 1
}

wait_for_shutdown() {
    local process_name="$1"
    local class_name="$2"
    local timeout_s="$3"

    local now_s=`date '+%s'`
    local stop_s=$(( $now_s + $timeout_s ))

    while [ $now_s -le $stop_s ]; do
        process_status "$process_name" $class_name >/dev/null
        if [ $? -eq 1 ]; then
            # Class not found in the jps output. Assume that it stopped.
            return 0
        fi
        sleep 2
        now_s=`date '+%s'`
    done

    echo "$process_name shutdown timeout(exceeded $timeout_s seconds)" >&2
    return 1
}

process_status() {
    local process_name="$1"
    local class_name="$2"

    local p=`ps -ef | grep "$class_name" | grep -v grep | awk '{print $2}'`
    if [ -n "$p" ]; then
        echo "$process_name is running with pid $p"
        return 0
    else
        echo "The process $process_name does not exist"
        return 1
    fi
}

kill_process() {
    local process_name="$1"
    local class_name="$2"

    local pids=`ps -ef | grep "$class_name" | grep -v grep | awk '{print $2}' | xargs`

    if [ "$pids" = "" ]; then
        echo "There is no $1 process"
    fi

    for pid in ${pids[@]}
    do
        if [ -z "$pid" ]; then
            echo "The process $process_name does not exist"
            return
        fi
        echo "Killing $process_name (pid $pid)..." >&2
        case "`uname`" in
            CYGWIN*) taskkill /F /PID "$pid" ;;
            *)       kill "$pid" ;;
        esac
    done
}

function kill_process_and_wait() {
    local process_name="$1"
    local class_name="$2"
    local timeout_s="$3"

    kill_process $process_name $class_name
    wait_for_shutdown $process_name $class_name $timeout_s
}

function free_memory() {
    local free=
    local os=`uname`
    if [ "$os" == "Linux" ]; then
        local distributor=`lsb_release -a | grep 'Distributor ID' | awk -F':' '{print $2}' | tr -d "\t"`
        if [ "$distributor" == "CentOS" ]; then
            free=`free -m | grep '\-\/\+' | awk '{print $4}'`
        elif [ "$distributor" == "Ubuntu" ]; then
            free=`free -m | grep 'Mem' | awk '{print $7}'`
        else
            echo "Unsupported Linux Distributor " $distributor
        fi
    elif [ "$os" == "Darwin" ]; then
        free=`top -l 1 | head -n 10 | grep PhysMem | awk -F',' '{print $2}' \
             | awk -F'M' '{print $1}' | tr -d " "`
    else
        echo "Unsupported operating system " $os
        exit 1
    fi
    echo $free
}

function calc_xmx() {
    local min_mem=$1
    local max_mem=$2
    # Get machine available memory
    local free=`free_memory`
    local half_free=$[free/2]

    local xmx=$min_mem
    if [[ "$free" -lt "$min_mem" ]]; then
        exit 1
    elif [[ "$half_free" -ge "$max_mem" ]]; then
        xmx=$max_mem
    elif [[ "$half_free" -lt "$min_mem" ]]; then
        xmx=$min_mem
    else
        xmx=$half_free
    fi
    echo $xmx
}
