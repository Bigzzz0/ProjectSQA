# Lang-1 PICT pilot baseline

โฟลเดอร์นี้เก็บผลการทดลองนำร่องเดิมของ `NumberUtils.createNumber(String)` ซึ่งสร้าง abstract pairwise combinations ด้วย Microsoft PICT ก่อนที่ทีมจะมี native IPO implementation

ไฟล์เหล่านี้เป็น **PICT reference baseline** ไม่ใช่ผลจาก IPO/IPOG ของทีม และไม่ใช่ชุดทดสอบรอบสุดท้ายสำหรับส่งให้ Member 4

## Provenance

- Archived from repository commit: `2607cf0`
- Project and bug: `Lang-1`
- Fixed-version oracle: `Lang-1f`
- Target class: `org.apache.commons.lang3.math.NumberUtils`
- Target method: `createNumber(String)`
- Strength: pairwise (`t = 2`)
- Generator: Microsoft PICT

## Pilot result

- Semantic factors: 4
- Cartesian combinations: 960
- PICT combinations: 48
- Covered value pairs: 194/194
- Unique concrete inputs: 39
- Reduction from Cartesian space: 95%
- Mandatory seed included: `"0x80000000"`
- Oracle outcomes: 48
- Generated JUnit 4 tests: 48, each with `@Test(timeout = 4000)`
- Fixed-version verification: `OK (48 tests)`

## Archived artifacts

- `Models/NumberUtils/`: PICT model and semantic value mapping
- `Result_Round1/NumberUtils/`: abstract combinations, concrete inputs and fixed-version oracle
- `Result_Round1/batch_manifest.json`: original pilot manifest
- `TestCode/NumberUtils_IPOTest.java`: original generated suite; the filename is historical and does not make this a native IPO result

Native IPO results must be generated independently into the active `Models/`, `Result_Round1/` and `TestCode/` paths, with generator provenance recorded as `native-ipo`.
