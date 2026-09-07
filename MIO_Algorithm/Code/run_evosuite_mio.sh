#!/bin/bash
# สั่งรัน EvoSuite ด้วย MIO Algorithm

# สร้างโฟลเดอร์สำหรับเก็บผลลัพธ์รอบที่ 1 หากยังไม่มี
mkdir -p /workspace/MIO_Algorithm/Result_Round1

# สั่งรัน EvoSuite
java -jar /workspace/MIO_Algorithm/Code/evosuite-1.0.6.jar \
  -class org.apache.commons.lang3.math.NumberUtils \
  -projectCP /tmp/Lang_1_buggy/target/classes \
  -Dalgorithm=MIO \
  -Dsearch_budget=60 \
  -Dcriterion=LINE:BRANCH:EXCEPTION:MUTATION \
  -Dreport_dir=/workspace/MIO_Algorithm/Result_Round1
