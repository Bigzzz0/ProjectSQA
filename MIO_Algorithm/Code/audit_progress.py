import os
import glob
import json

cat_file = 'target_benchmark/all_bugs_catalog.json'
test_code_dir = 'MIO_Algorithm/TestCode'
progress_file = 'MIO_Algorithm/progress_mio.json'

with open(cat_file, 'r', encoding='utf-8') as f:
    catalog = json.load(f)

with open(progress_file, 'r', encoding='utf-8') as f:
    progress_data = json.load(f)

projects_map = {}
for item in catalog:
    p = item['project']
    bid = item['bug_id']
    if p not in projects_map:
        projects_map[p] = []
    projects_map[p].append(bid)

results = {}
for p in sorted(projects_map.keys()):
    all_bids = sorted(projects_map[p])
    valid_bugs = set()
    for b in all_bids:
        key = f"{p}_{b}b"
        if key in progress_data and progress_data[key].get("completed", False):
            valid_bugs.add(b)
            continue
        pattern = os.path.join(test_code_dir, key, "**", "*_ESTest.java")
        files = glob.glob(pattern, recursive=True)
        if files:
            valid_bugs.add(b)
            continue
        direct = os.path.join(test_code_dir, key, "*_ESTest.java")
        if glob.glob(direct):
            valid_bugs.add(b)

    missing = [b for b in all_bids if b not in valid_bugs]
    results[p] = {
        'total': len(all_bids),
        'completed': len(valid_bugs),
        'missing_count': len(missing),
        'missing_bugs': missing
    }

print("=========================================================================================")
print(f"{'PROJECT':16} | {'COMPLETED':10} | {'TOTAL':6} | {'PERCENT':8} | {'MISSING BUGS'}")
print("=========================================================================================")
for p, data in results.items():
    pct = (data['completed'] / data['total']) * 100
    missing_str = str(data['missing_bugs']) if data['missing_bugs'] else "None (100%)"
    print(f"{p:16} | {data['completed']:6}     | {data['total']:5}  | {pct:6.1f}%  | {missing_str}")
print("=========================================================================================")
total_bugs = sum(d['total'] for d in results.values())
total_done = sum(d['completed'] for d in results.values())
print(f"TOTAL COMPLETED : {total_done}/{total_bugs} ({(total_done/total_bugs)*100:.1f}%)")
print(f"TOTAL REMAINING : {total_bugs - total_done}/{total_bugs} ({((total_bugs - total_done)/total_bugs)*100:.1f}%)")
