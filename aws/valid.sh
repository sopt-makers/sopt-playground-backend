#!/bin/bash
sleep 10
root=`sudo lsof -i -P -n | grep LISTEN | grep 8080 2>/dev/null | head -n 1 | awk -F" " '{print$3}'`
if [[ "$root" = "root" ]]; then
  echo "port OK_1"
else
  echo "port Fail_1"
  sleep 20

  root_nxt=`sudo lsof -i -P -n | grep LISTEN | grep 8080 2>/dev/null | head -n 1 | awk -F" " '{print$3}'`
  if [[ "$root_nxt" = "root" ]]; then
    echo "port OK_2"
  else
    echo "port Fail_2"
    exit 1
  fi
fi