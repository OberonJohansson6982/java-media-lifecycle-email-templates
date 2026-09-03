#!/usr/bin/env sh
set -eu

mkdir -p target/classes
javac -d target/classes $(find src/main/java -name '*.java')
java -cp target/classes dev.lessonstream.MediaTemplateExample
