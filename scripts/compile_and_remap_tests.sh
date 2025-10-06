#!/usr/bin/env bash

for dir in ./test/*; do
  mkdir -p ${dir%/}/out
  rm -rf ${dir%/}/out/*
  javac -d ${dir%/}/out ${dir%/}/src/*.java
  java -cp deps/*:out com.thebluetropics.crabremapper.CrabRemapper -m ${dir%/}/mappings.baked remap ${dir%/}/out
done
