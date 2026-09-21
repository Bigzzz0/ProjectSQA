import os
import glob
import json

d4j_bugs = {
    'Chart': 26, 'Cli': 39, 'Closure': 174, 'Codec': 18, 'Collections': 28,
    'Compress': 47, 'Csv': 16, 'Gson': 18, 'JacksonCore': 26, 'JacksonDatabind': 110,
    'JacksonXml': 6, 'Jsoup': 93, 'JxPath': 22, 'Lang': 61, 'Math': 106,
    'Mockito': 38, 'Time': 26
}

test_code_dir = 'MIO_Algorithm/TestCode'
progress_file = 'MIO_Algorithm/progress_mio.json'

with open(progress_file, 'r', encoding='utf-8') as f:
    progress_data = json.load(f)

results = {}
for project, total in sorted(d4j_bugs.items()):
    valid_bugs = set()
    for b in range(1, total + 1):
        key = f"{project}_{b}b"
        # Check progress_data
        if key in progress_data and progress_data[key].get("completed", False):
            valid_bugs.add(b)
            continue
        # Also check disk directly
        pattern = os.path.join(test_code_dir, key, "**", "*_ESTest.java")
        files = glob.glob(pattern, recursive=True)
        if files:
            valid_bugs.add(b)
    
    missing = [b for b in range(1, total + 1) if b not in valid_bugs]
    results[project] = {
        'total': total,
        'completed': len(valid_bugs),
        'missing_count': len(missing),
        'missing_bugs': missing
    }

print("=========================================================================================")
print(f"{'PROJECT':16} | {'COMPLETED':10} | {'TOTAL':6} | {'PERCENT':8} | {'MISSING BUGS'}")
print("=========================================================================================")
for p, data in results.items():
    pct = (data['completed'] / data['total']) * 100
    missing_str = str(data['missing_bugs'][:15]) + ("..." if len(data['missing_bugs']) > 15 else "")
    print(f"{p:16} | {data['completed']:6}     | {data['total']:5}  | {pct:6.1f}%  | {missing_str}")
print("=========================================================================================")
total_bugs = sum(d['total'] for d in results.values())
total_done = sum(d['completed'] for d in results.values())
print(f"TOTAL: {total_done}/{total_bugs} ({(total_done/total_bugs)*100:.1f}%)")
