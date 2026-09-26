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

## Paired coverage comparisons

Wilcoxon signed-rank tests compare line coverage for the same project-bug keys with measured coverage in both techniques. Holm correction is applied across the six comparisons; the paired rank-biserial correlation reports direction and magnitude.
- Gemini 3.8 Flash vs DeepSeek V4 Flash: matched n=135, nonzero pairs=93, p=3.639e-12, Holm-adjusted p=7.278e-12, paired rank-biserial=0.83.
- MIO (EvoSuite SBST) vs Gemini 3.8 Flash: matched n=397, nonzero pairs=336, p=2.171e-33, Holm-adjusted p=1.085e-32, paired rank-biserial=-0.7579.
- MIO (EvoSuite SBST) vs DeepSeek V4 Flash: matched n=182, nonzero pairs=151, p=0.01538, Holm-adjusted p=0.01538, paired rank-biserial=-0.2273.
- MIO (EvoSuite SBST) vs IPO (Native IPO): matched n=245, nonzero pairs=232, p=2.73e-39, Holm-adjusted p=1.638e-38, paired rank-biserial=0.9931.
- Gemini 3.8 Flash vs IPO (Native IPO): matched n=147, nonzero pairs=142, p=4.744e-25, Holm-adjusted p=1.898e-24, paired rank-biserial=1.0.
- DeepSeek V4 Flash vs IPO (Native IPO): matched n=69, nonzero pairs=66, p=1.641e-12, Holm-adjusted p=4.923e-12, paired rank-biserial=1.0.

MIO generation budget paired tests compare the same project-bug-target class at two budgets; the output records matched sample size and Holm-adjusted p-values. These tests describe generation coverage and do not measure benchmark fault detection.
- MIO 30s_vs_60s: matched n=1006, p=1.693e-84, Holm-adjusted p=3.386e-84, paired rank-biserial=-0.8276.
- MIO 60s_vs_120s: matched n=981, p=1.541e-67, Holm-adjusted p=1.541e-67, paired rank-biserial=-0.7647.
