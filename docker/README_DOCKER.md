# Docker และ Defects4J: คู่มือรัน benchmark

ใช้ Docker environment ใน docker/docker-compose.yml เพื่อให้ Defects4J, Java และ EvoSuite ใช้รุ่นเดียวกับการทดลอง. Container ชื่อ defects4j_sqa, service ชื่อ defects4j-env, และ mount repository ที่ /workspace.

## เครื่องมือใน container

- Defects4J commit 8c16da8230843cdc918eaf4ddb449637f02b83c6 (3.0.1-7-g8c16da82)
- OpenJDK 11 เป็นค่าเริ่มต้น; มี OpenJDK 8 สำหรับ MIO generation ที่ต้องใช้
- EvoSuite 1.0.6 ใน /opt/evosuite/
- PICT ติดตั้งใน environment เพื่อใช้เป็น reference tool; ผล PICT ไม่ใช่ Native IPO ใน benchmark หลัก
- Python สำหรับ benchmark runner; analytics และรายงาน workbook สร้างบน host ตาม requirements-analysis.txt

## เปิดและตรวจ environment

รันจาก root ของ repository:

~~~powershell
docker compose -f docker/docker-compose.yml up -d --build
docker ps --filter "name=defects4j_sqa"
docker exec defects4j_sqa defects4j pids
docker exec defects4j_sqa defects4j info -p Math -b 2
docker exec defects4j_sqa java -version
docker exec defects4j_sqa git -C /opt/defects4j rev-parse HEAD
~~~

เข้าสู่ shell ของ container:

~~~powershell
docker exec -it defects4j_sqa bash
~~~

## รัน benchmark

ทดสอบหนึ่งบั๊กและหนึ่งเทคนิค:

~~~powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --project Lang --bug 1 --techniques ipo
~~~

ประเมิน suite ที่มีอยู่ใน catalog ทั้งหมด และทำต่อจากผลที่บันทึกไว้:

~~~powershell
docker exec defects4j_sqa python3 /workspace/scripts/run_benchmark.py --all-bugs --resume
~~~

ตัวเลือก --sample-17 รัน 17 บั๊กตัวแทน (หนึ่งรายการต่อโปรเจกต์); ไม่ใช่ exhaustive benchmark. ก่อนรันชุดใหญ่ ตรวจ suite inventory และพื้นที่ดิสก์ให้พร้อม.

## ผลลัพธ์และสถานะ

Runner บันทึกผลดิบลง:

- results/benchmark_results.csv
- results/<Project>/<bug_id>/<technique>.json
- checkpoint `progress.json` สำหรับ resume ซึ่ง runner สร้างไว้ใน workspace และ Git ไม่ติดตาม

คอลัมน์ Run_Log ใน master dataset ชี้ไปยัง JSON run record ที่เก็บ suite hash, run ID, ผล buggy/fixed, coverage, เวลา และ error.

| สถานะ | ความหมาย |
|---|---|
| BUG_DETECTED | มี failure บน buggy และไม่มี failure บน fixed |
| NOT_DETECTED | ไม่พบ failure ทั้งสองเวอร์ชัน |
| FLAKY_OR_REGRESSION | มี failure บน fixed |
| COMPILE_ERROR | test suite compile ไม่ผ่าน |
| TIMEOUT | เกินเวลาในขั้นรัน |
| NO_SUITE | ไม่มี suite ที่ส่งเข้าประเมิน; ไม่ใช่ผล test |

Coverage ที่วัดไม่ได้ต้องเป็นค่าว่างและมีสถานะ/error ประกอบ ไม่ใช่ 0%. วิธีคำนวณและตัวหารดูใน results/DATA_DICTIONARY.md.

## ปิด container

หลังงานประเมินเสร็จ ปิด container ด้วย:

~~~powershell
docker compose -f docker/docker-compose.yml down
~~~

อย่าลบ volumes หรือผลลัพธ์ใน repository หากยังต้องใช้ run logs ต่อ.
