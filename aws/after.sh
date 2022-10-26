#!/bin/bash
sudo chown -R root:root /home/ec2-user/deploy
cd /home/ec2-user/deploy
unzip INTERNAL.zip
rm INTERNAL.zip