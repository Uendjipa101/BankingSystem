#!/bin/bash
rm -rf out
mkdir out
javac -cp "lib/*" -d out $(find src -name "*.java" -not -path "src/view/*")
java -cp "out:lib/*" ConsoleApp