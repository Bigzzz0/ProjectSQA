import subprocess, os

bugs_to_fix = [
    ('Closure', 34, [('com.google.javascript.jscomp.CodeGenerator', 'CodeGenerator.java'), ('com.google.javascript.jscomp.CodePrinter', 'CodePrinter.java')]),
    ('Closure', 42, [('com.google.javascript.jscomp.parsing.IRFactory', 'IRFactory.java')]),
    ('Closure', 52, [('com.google.javascript.jscomp.CodeGenerator', 'CodeGenerator.java')]),
    ('Closure', 81, [('com.google.javascript.jscomp.parsing.IRFactory', 'IRFactory.java')]),
    ('Closure', 84, [('com.google.javascript.jscomp.parsing.IRFactory', 'IRFactory.java')]),
    ('Closure', 122, [('com.google.javascript.jscomp.parsing.IRFactory', 'IRFactory.java')]),
    ('Closure', 123, [('com.google.javascript.jscomp.CodeGenerator', 'CodeGenerator.java')]),
    ('Closure', 128, [('com.google.javascript.jscomp.CodeGenerator', 'CodeGenerator.java')]),
    ('Closure', 131, [('com.google.javascript.rhino.TokenStream', 'TokenStream.java')]),
]

for proj, bid, targets in bugs_to_fix:
    print(f"Checking {proj}-{bid}...")
    work_dir = f"/tmp/check_{proj}_{bid}"
    cmd = f"rm -rf {work_dir} && defects4j checkout -p {proj} -v {bid}b -w {work_dir}"
    subprocess.run(['docker', 'exec', 'defects4j_sqa', 'bash', '-c', cmd], capture_output=True)
    
    for fqcn, fname in targets:
        rel_path = fqcn.replace('.', '/') + '.java'
        find_cmd = f'find {work_dir} -path "*/{rel_path}"'
        res = subprocess.run(['docker', 'exec', 'defects4j_sqa', 'bash', '-c', find_cmd], capture_output=True, text=True)
        found_path = res.stdout.strip().splitlines()
        print(f"  Target {fqcn} -> {found_path}")
        if found_path:
            dest = f"target_benchmark/{proj}_{bid}b/{fname}"
            os.makedirs(os.path.dirname(dest), exist_ok=True)
            cp_cmd = ['docker', 'cp', f"defects4j_sqa:{found_path[0]}", dest]
            subprocess.run(cp_cmd)
            print(f"    Copied to {dest}")
        else:
            print(f"    WARNING: NOT FOUND for {fqcn} in {work_dir}")

print("Done extracting correct target sources!")
