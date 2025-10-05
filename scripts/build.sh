#!/usr/bin/env bash

kotlinc -cp deps/asm-9.8.jar:deps/asm-commons-9.8.jar:deps/asm-tree-9.8.jar -d out src
