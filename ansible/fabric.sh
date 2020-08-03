#!/bin/sh

case "$1" in
start) cmd=started ;;
stop) cmd=stopped ;;
restart) cmd=restarted ;;
*) cmd=$1 ;;
esac

case "$cmd" in
start1)
  if [ $# -ne 2 ]; then
    echo "Usage: fabric.sh start1 requires IP address of node to (re-)start" >&2
  else
    ansible $2 -m service --become --args "name=fibre state=restarted"
  fi
  ;;
started | restarted)
  ansible infra -m service --become --args "name=fibre state=$cmd"
  echo waiting 10s to start simple fibres..
  sleep 10
  ansible simple -m service --become --args "name=fibre state=$cmd"
  ;;
stopped)
  ansible all -m service --become --args "name=fibre state=$cmd"
  ;;
erase)
  ansible all --become --args "rm -rf /opt/fabric/var/1/fabric-state"
  ;;
status)
  ansible all --args 'sh -c "hostname;  ps -ef | grep [j]ava; true"'
  ;;
bootlog)
  ansible all --args 'sh -c "hostname;  cat /opt/fabric/var/fibre.out"'
  ;;
cmd)
  shift
  ansible all --args "sh -c \"hostname; $*\""
  ;;
reinstall)
  ansible-playbook fabric.yml --extra-vars update_fabric=true
  ;;
*)
  echo "Usage: $0: start | stop | erase | status | bootlog | start1 | reinstall" >&2
  ;;
esac

#
