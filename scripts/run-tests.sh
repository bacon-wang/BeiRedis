#!/bin/bash
# 编译 src + test，用 JUnit Launcher API 运行全部测试
# 依赖 ~/.m2 里 IDEA 下载的 JUnit 5.14（见 12-BeiRedis.iml），不需要 Maven、不联网
set -e
cd "$(dirname "$0")/.."
M=~/.m2/repository
CP=$(find $M/org/junit $M/org/opentest4j $M/org/apiguardian -name "*.jar" \
       \( -path "*/5.14.0/*" -o -path "*/1.14.0/*" -o -path "*opentest4j/1.3.0/*" -o -path "*apiguardian-api/1.1.2/*" \) \
       ! -name "*sources*" ! -name "*javadoc*" ! -name "*vintage*" | tr '\n' ':')
OUT=out/cli
rm -rf "$OUT" && mkdir -p "$OUT"
javac -encoding UTF-8 -d "$OUT" -cp "$CP" $(find src test -name "*.java") scripts/RunTests.java
java -cp "$OUT:$CP" RunTests
