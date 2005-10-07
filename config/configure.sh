#!/bin/bash
#
# Copyright (c) Members of the EGEE Collaboration. 2004.
# See http://public.eu-egee.org/partners/ for details on 
# the copyright holders.
# For license conditions see the license file or
# http://eu-egee.org/license.html
#
# Authors: Akos Frohner <Akos.Frohner@cern.ch>

PACKAGENAME=glite-data-hydra-service

source ${GLITE_LOCATION:-/opt/glite}/sbin/glite-data-common-service-configure.sh

_mysql_create_user
_mysql_load "schema/mysql/mysql-schema.sql"

_print_cleanup

