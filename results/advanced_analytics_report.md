# Member 4 analytics

Master dataset currently has 3416 rows for 854 catalog bugs across 17 projects.
All available suites have a current benchmark outcome and the full matrix has explicit final statuses.

## Bug-level results

| Technique | Attempted | Completed | Line mean (N) | Branch mean (N) | Detected | FDR of attempts | FDR of full catalog |
|---|---:|---:|---:|---:|---:|---:|---:|
| IPO (Native IPO) | 257 | 252 | 26.76% (n=252) | 18.67% (n=252) | 37 | 14.40% | 4.33% |
| MIO (EvoSuite SBST) | 834 | 797 | 63.85% (n=797) | 56.51% (n=797) | 5 | 0.60% | 0.59% |
| DeepSeek V4 Flash | 836 | 191 | 77.99% (n=191) | 70.25% (n=191) | 11 | 1.32% | 1.29% |
| Gemini 3.8 Flash | 841 | 422 | 86.24% (n=422) | 79.45% (n=422) | 107 | 12.72% | 12.53% |

## Coverage

Coverage averages include completed evaluations with numeric coverage only; compile failures and missing values are excluded.

## Suite generation metrics

MIO budget and AI generation records are summarized from their own source files. They are not joined to measured fault detections unless a benchmark run identity supports the join.
- Gemini 3.8 Flash: 1068 generation log records; mean tokens 20699.64; mean generation time 88.66 seconds; measured benchmark detections 107.
- DeepSeek V4 Flash: 1069 generation log records; mean tokens 20900.15; mean generation time 295.11 seconds; measured benchmark detections 11.

## Hypothesis tests

Mann–Whitney U tests compare measured line coverage. Small samples are marked insufficient.
- Gemini 3.8 Flash vs DeepSeek V4 Flash: n=422/191, p=1.613e-05, A12=0.6078.
- MIO (EvoSuite SBST) vs Gemini 3.8 Flash: n=797/422, p=4.233e-40, A12=0.2699.
- MIO (EvoSuite SBST) vs DeepSeek V4 Flash: n=797/191, p=1.578e-08, A12=0.3685.
- MIO (EvoSuite SBST) vs IPO (Native IPO): n=797/252, p=4.116e-44, A12=0.7907.
- Gemini 3.8 Flash vs IPO (Native IPO): n=422/252, p=9.938e-78, A12=0.9282.
- DeepSeek V4 Flash vs IPO (Native IPO): n=191/252, p=2.312e-41, A12=0.8733.
