#!/bin/bash
#
# Copyright (c) Members of the EGEE Collaboration. 2004-2010.
# See http://www.eu-egee.org/partners for details on the copyright holders.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# show help and usage
progname=`basename $0`
showHelp()
{
cat << EndHelpHeader
Nagios probe for testing the status of Hydra service

Tests called:
    1. Register job
    2. Register to receive notifications 
    3. Log events
    4. Check job state
    5. Receive notifications

Return values:
    0: Passed
    1: Warning
    2: Critical
    3: Unknown

Console output:
    OK|<time the test job took to transit from Registered to Cleared>
    WARNING: <reason>
    DOWN: <reason>
    UNKNOWN: <reason>

EndHelpHeader

	echo "Usage: $progname [-h] [-v[v[v]]] {[-H server] [-p port] | [server[:port]]} [-t <timeout>] [-T <tmpdir>]"
	echo "Options:"
	echo "    -h | --help       Show this help message"
	echo "    -v[vv]            Verbosity level"
	echo "    -H <server>       server (Environmental variables are used if unspecified)"
	echo "    -p <server>       port (Environmental variables or defaults are used if unspecified)"
	echo "    -t <timeout>      Probe timeout in seconds"
	echo "    -T <tmpdir>       Temporary directory (default /var/lib/grid-monitoring/emi.lb)"
	echo ""
}

function vprintf()
{
#	echo $1 le $VERBLEVEL
	if [ $1 -le $VERBLEVEL ]; then
		printf "$2"
	fi
}

function check_exec()
{
        if [ -z $1 ]; then
                set_error "No binary to check"
                return 1
        fi
        # XXX: maybe use bash's command type?
        local ret=`which $1 2> /dev/null`
        if [ -n "$ret" -a -x "$ret" ]; then
                return 0
        else
                return 1
        fi
}

function check_binaries()
{
        local ret=0
        for file in $@
        do
                check_exec $file
                if [ $? -gt 0 ]; then
                        vprintf 2 "\nfile $file not found\n"
                        ret=1
                fi
        done
        return $ret
}

# showHelp;

# What do we want to test for the Hydra service?
# Test that a Hydra service is up and running.

# Assume tomcat5.pid exists

locate tomcat | grep pid > /dev/null 2>&1; result=$?
if [ $result -eq 0 ] # /var/run/tomcat5.pid was found.
then
    unset TOMCAT_PID_FILE
    TOMCAT_PID_FILE=`locate tomcat | grep pid`
    TOMCAT_PID=`cat $TOMCAT_PID_FILE`
    ps -ef | grep $TOMCAT_PID | grep -v grep > /dev/null 2>&1; result=$?
else # no /var/run/tomcat5.pid was found.
    ps -efH | grep tomcat; result=$?
    if [ $result -eq 0 ]
    then
    fi
fi

# If the result is "0" then we know that a tomcat process is running
# but this does not mean it's a Hydra key-store endpoint
# so must continue to test...



# Should make sure that the endpoints for the service are up.
# On SL we look at "/var/run/mysqld/mysqld.pid"


# Test if the mysql database is running also

