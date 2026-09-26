# Dataset specification

Catalog: 854 bugs across 17 projects.
Master CSV rows: 3416; expected matrix rows: 3416.
Available suite rows: 2797; attempted rows: 2797; NO_SUITE rows: 619; unresolved rows: 0.

| Field | Meaning |
|---|---|
| Project, Bug_ID | Defects4J bug key |
| Technique | IPO native, MIO, DeepSeek, or Gemini |
| Target_Classes | Modified classes in scope |
| Suite_Available, Test_Files | Current suite inventory |
| Line_Coverage_%, Branch_Coverage_% | Measured target class coverage; blank unless measured |
| Fault_Detection_Status | BUG_DETECTED only when buggy fails and fixed passes; otherwise measured classification or NOT_EVALUATED |
| Execution_Status | DONE, COMPILE_ERROR, TIMEOUT, NO_SUITE, NOT_RUN, INVALID_SUITE, or STALE_RESULT |
| Suite_SHA256, Run_ID, Timestamp | Suite provenance and run identity |
| Run_Log | Structured JSON run record with provenance, status, timing, and errors or failing-test details |
| Evaluation_Duration_Sec | Wall time of the benchmark evaluation |

FDR is reported over attempted bug-technique evaluations (DONE, COMPILE_ERROR, TIMEOUT) and separately over the full catalog. A bug is detected only when at least one target test fails on buggy and passes on fixed. Coverage means use DONE rows with measured numeric coverage only.

MIO budget figures describe suite-generation experiments from the MIO budget summary and are separate from this benchmark evaluation dataset.

Current execution status by technique:

| Technique | Execution status | Rows |
|---|---|---:|
| IPO (Native IPO) | COMPILE_ERROR | 5 |
| IPO (Native IPO) | DONE | 252 |
| IPO (Native IPO) | NO_SUITE | 597 |
| MIO (EvoSuite SBST) | COMPILE_ERROR | 37 |
| MIO (EvoSuite SBST) | DONE | 797 |
| MIO (EvoSuite SBST) | NO_SUITE | 20 |
| DeepSeek V4 Flash | COMPILE_ERROR | 661 |
| DeepSeek V4 Flash | DONE | 192 |
| DeepSeek V4 Flash | NO_SUITE | 1 |
| Gemini 3.8 Flash | COMPILE_ERROR | 429 |
| Gemini 3.8 Flash | DONE | 424 |
| Gemini 3.8 Flash | NO_SUITE | 1 |
