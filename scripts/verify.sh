#!/usr/bin/env sh
set -eu

rm -rf target/classes target/test-classes
mkdir -p target/classes target/test-classes
javac -d target/classes $(find src/main/java -name '*.java')
javac -cp target/classes -d target/test-classes $(find src/test/java -name '*.java')
java -cp target/classes:target/test-classes dev.lessonstream.lifecycle.MediaLifecyclePolicyTest
