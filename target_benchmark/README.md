# Defects4J 17-Project Representative Benchmark

โฟลเดอร์นี้เก็บ 1 Bug ID ตัวแทนต่อ Defects4J project รวม 17 bug targets ข้อมูล ground truth ของแต่ละรายการอยู่ใน `<Project>_<BugID>b/defects4j_info.txt`

## Source of truth

`defects4j_info.txt` เป็นแหล่งข้อมูลหลักสำหรับ:

- Project และ Bug ID
- modified source classes
- triggering tests
- bug report ID และ URL

ไฟล์ [`catalog_17_projects.json`](catalog_17_projects.json) สร้างจาก metadata เหล่านี้ด้วยโค้ด ไม่แก้ class หรือ triggering test ด้วยมือ:

```bash
python Combinatorial_IPO/Code/runner/generate_catalog.py \
  --target-root target_benchmark \
  --output target_benchmark/catalog_17_projects.json \
  --preserve-domains-from target_benchmark/catalog_17_projects.json
```

ตัว generator ตรวจว่า directory ตรงกับ `<Project>_<BugID>b` และ Java file ของทุก modified source มีอยู่จริง หากไม่ตรงจะหยุดพร้อม error แทนการเดาไฟล์ทดแทน

## Current scope

| Project | Bug ID | Modified sources | Triggering tests |
| :--- | ---: | ---: | ---: |
| Chart | 1 | 1 | 1 |
| Cli | 1 | 1 | 1 |
| Closure | 1 | 1 | 8 |
| Codec | 1 | 3 | 5 |
| Collections | 25 | 1 | 1 |
| Compress | 1 | 1 | 1 |
| Csv | 1 | 1 | 1 |
| Gson | 1 | 1 | 1 |
| JacksonCore | 1 | 2 | 1 |
| JacksonDatabind | 1 | 1 | 1 |
| JacksonXml | 1 | 1 | 3 |
| Jsoup | 1 | 1 | 1 |
| JxPath | 1 | 2 | 2 |
| Lang | 1 | 1 | 1 |
| Math | 2 | 1 | 1 |
| Mockito | 1 | 1 | 26 |
| Time | 1 | 2 | 1 |
| **รวม** | **17 bug targets** | **22** | **56** |

หนึ่งบั๊กอาจแก้หลายคลาสหรือมี triggering tests หลายรายการ จึงต้องอ่าน `modified_sources` และ `trigger_tests` เมื่อต้องการ ground truth ครบถ้วน

## Catalog compatibility

ฟิลด์ต่อไปนี้เก็บข้อมูลครบจาก Defects4J:

- `modified_sources`: modified classes ทุกคลาส
- `trigger_tests`: triggering tests ทุก test

เพื่อไม่ทำให้เครื่องมือเดิมของสมาชิกคนอื่นเสีย catalog ยังคงมี:

- `target_class` และ `simple_name`: modified source ตัวแรก
- `trigger_test`: triggering test ตัวแรก

ฟิลด์แบบเอกพจน์เป็น compatibility view เท่านั้น ไม่ใช่หลักฐานว่าบั๊กมีเพียงหนึ่ง class หรือหนึ่ง triggering test

## Member workflows

- Member 1 IPO: วิเคราะห์ทุก class ใน `modified_sources`; ผลตัวทดลองอยู่ `Combinatorial_IPO/Result_Round1/` และผล catalog-loop จริงอยู่ `Combinatorial_IPO/Result_Round2/`
- Member 2/3: เครื่องมือเดิมยังอ่าน `target_class`, `simple_name` และ `trigger_test` ได้ แต่ควรขยายไปใช้รายการพหูพจน์เมื่อต้องการ coverage/FDR ครบทุก modified source
- Member 4: ใช้ Project, Bug ID, modified sources และ triggering tests จาก catalog เดียวกันในการประเมิน benchmark
