#!/usr/bin/env bash

mkdir -p deps

if [ ! -e deps/kotlin-stdlib-2.2.0.jar ]; then
  curl -o deps/kotlin-stdlib-2.2.0.jar https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/2.2.0/kotlin-stdlib-2.2.0.jar
fi

if [ ! -e deps/asm-9.8.jar ]; then
  curl -o deps/asm-9.8.jar https://repo1.maven.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.jar
fi

if [ ! -e deps/asm-commons-9.8.jar ]; then
  curl -o deps/asm-commons-9.8.jar https://repo1.maven.org/maven2/org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar
fi

if [ ! -e deps/asm-tree-9.8.jar ]; then
  curl -o deps/asm-tree-9.8.jar https://repo1.maven.org/maven2/org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar
fi
