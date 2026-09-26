# Member 4 analytics

Master dataset currently has 3416 rows for 854 catalog bugs across 17 projects.
All available suites have a current benchmark outcome and the full matrix has explicit final statuses.

## Bug-level results

| Technique | Attempted | Completed | Line mean (N) | Branch mean (N) | Detected | FDR of attempts | FDR of full catalog |
|---|---:|---:|---:|---:|---:|---:|---:|
| IPO (Native IPO) | 257 | 252 | 26.76% (n=252) | 18.67% (n=252) | 37 | 14.40% | 4.33% |
| MIO (EvoSuite SBST) | 834 | 797 | 63.85% (n=797) | 56.51% (n=797) | 5 | 0.60% | 0.59% |
| DeepSeek V4 Flash | 853 | 192 | 78.02% (n=192) | 70.22% (n=192) | 11 | 1.29% | 1.29% |
| Gemini 3.8 Flash | 853 | 424 | 86.29% (n=424) | 79.54% (n=424) | 107 | 12.54% | 12.53% |

## Coverage

Coverage averages include completed evaluations with numeric coverage only; compile failures and missing values are excluded.

## Suite generation metrics

MIO budget and AI generation records are summarized from their own source files. They are not joined to measured fault detections unless a benchmark run identity supports the join.
- Gemini 3.8 Flash: 1079 generation log records; mean tokens 20855.06; mean generation time 89.78 seconds; measured benchmark detections 107.
- DeepSeek V4 Flash: 1082 generation log records; mean tokens 21045.26; mean generation time 295.54 seconds; measured benchmark detections 11.

## Paired coverage comparisons

Wilcoxon signed-rank tests compare line coverage for the same project-bug keys with measured coverage in both techniques. Holm correction is applied across the six comparisons; the paired rank-biserial correlation reports direction and magnitude.
- Gemini 3.8 Flash vs DeepSeek V4 Flash: matched n=136, nonzero pairs=94, p=2.255e-12, Holm-adjusted p=4.923e-12, paired rank-biserial=0.8336.
- MIO (EvoSuite SBST) vs Gemini 3.8 Flash: matched n=399, nonzero pairs=337, p=1.457e-33, Holm-adjusted p=7.285e-33, paired rank-biserial=-0.7589.
- MIO (EvoSuite SBST) vs DeepSeek V4 Flash: matched n=183, nonzero pairs=152, p=0.01752, Holm-adjusted p=0.01752, paired rank-biserial=-0.2221.
- MIO (EvoSuite SBST) vs IPO (Native IPO): matched n=245, nonzero pairs=232, p=2.73e-39, Holm-adjusted p=1.638e-38, paired rank-biserial=0.9931.
- Gemini 3.8 Flash vs IPO (Native IPO): matched n=147, nonzero pairs=142, p=4.744e-25, Holm-adjusted p=1.898e-24, paired rank-biserial=1.0.
- DeepSeek V4 Flash vs IPO (Native IPO): matched n=69, nonzero pairs=66, p=1.641e-12, Holm-adjusted p=4.923e-12, paired rank-biserial=1.0.

MIO generation budget paired tests compare the same project-bug-target class at two budgets; the output records matched sample size and Holm-adjusted p-values. These tests describe generation coverage and do not measure benchmark fault detection.
- MIO 30s_vs_60s: matched n=1006, p=1.693e-84, Holm-adjusted p=3.386e-84, paired rank-biserial=-0.8276.
- MIO 60s_vs_120s: matched n=981, p=1.541e-67, Holm-adjusted p=1.541e-67, paired rank-biserial=-0.7647.
