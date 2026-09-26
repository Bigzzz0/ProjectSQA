#!/usr/bin/env python3
"""
Generate detailed delivery report for Member 4 with all SHA-256 hashes,
target classes, test packages, and file paths.
"""

import os
import hashlib
import json

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

def sha256(path):
    h = hashlib.sha256()
    with open(path, 'rb') as f:
        for chunk in iter(lambda: f.read(1024*1024), b''):
            h.update(chunk)
    return h.hexdigest()

slots = [
    # Task 1
    ('Closure', 18, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CompilerDeepseekTest.java', 'com.google.javascript.jscomp.Compiler'),
    ('Closure', 22, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CheckSideEffectsDeepseekTest.java', 'com.google.javascript.jscomp.CheckSideEffects'),
    ('Closure', 31, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CompilerDeepseekTest.java', 'com.google.javascript.jscomp.Compiler'),
    ('Collections', 8, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'UnboundedFifoBufferDeepseekTest.java', 'org.apache.commons.collections.buffer.UnboundedFifoBuffer'),
    ('Collections', 8, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'UnboundedFifoBufferGeminiTest.java', 'org.apache.commons.collections.buffer.UnboundedFifoBuffer'),
    ('Lang', 58, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'NumberUtilsDeepseekTest.java', 'org.apache.commons.lang.math.NumberUtils'),
    ('Lang', 58, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'NumberUtilsGeminiTest.java', 'org.apache.commons.lang.math.NumberUtils'),
    # Task 3
    ('Closure', 41, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'FunctionTypeBuilderDeepseekTest.java', 'com.google.javascript.jscomp.FunctionTypeBuilder'),
    ('JacksonCore', 25, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'ReaderBasedJsonParserDeepseekTest.java', 'com.fasterxml.jackson.core.json.ReaderBasedJsonParser'),
    # Task 2
    ('Math', 9, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'LineDeepseekTest.java', 'org.apache.commons.math3.geometry.euclidean.threed.Line'),
    ('Math', 9, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'LineGeminiTest.java', 'org.apache.commons.math3.geometry.euclidean.threed.Line'),
    ('Closure', 34, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CodeGeneratorDeepseekTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 34, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CodePrinterDeepseekTest.java', 'com.google.javascript.jscomp.CodePrinter'),
    ('Closure', 34, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'CodeGeneratorGeminiTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 34, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'CodePrinterGeminiTest.java', 'com.google.javascript.jscomp.CodePrinter'),
    ('Closure', 42, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'IRFactoryDeepseekTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 42, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'IRFactoryGeminiTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 52, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CodeGeneratorDeepseekTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 52, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'CodeGeneratorGeminiTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 81, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'IRFactoryDeepseekTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 81, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'IRFactoryGeminiTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 84, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'IRFactoryDeepseekTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 84, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'IRFactoryGeminiTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 122, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'IRFactoryDeepseekTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 122, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'IRFactoryGeminiTest.java', 'com.google.javascript.jscomp.parsing.IRFactory'),
    ('Closure', 123, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CodeGeneratorDeepseekTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 123, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'CodeGeneratorGeminiTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 128, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'CodeGeneratorDeepseekTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 128, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'CodeGeneratorGeminiTest.java', 'com.google.javascript.jscomp.CodeGenerator'),
    ('Closure', 131, 'DeepSeek V4 Flash', 'Deepseek-v4_flash', 'TokenStreamDeepseekTest.java', 'com.google.javascript.rhino.TokenStream'),
    ('Closure', 131, 'Gemini 3.8 Flash', 'Gemini-3_8_flash', 'TokenStreamGeminiTest.java', 'com.google.javascript.rhino.TokenStream'),
]

report_rows = []
for proj, bid, tech, tdir, fname, tcls in slots:
    p = os.path.join(PROJECT_ROOT, tdir, 'TestCode', f'{proj}_{bid}b', fname)
    if not os.path.exists(p):
        p = os.path.join(PROJECT_ROOT, tdir, 'TestCode', f'{proj}-{bid}', fname)
    hash_val = sha256(p)
    pkg = tcls.rsplit('.', 1)[0]
    cls_name = fname.replace('.java', '')
    simple_tgt = tcls.rsplit('.', 1)[-1]
    prompt_p = os.path.join(tdir, 'Prompt', f'{proj}_{bid}b', f'actual_prompt_{simple_tgt}.md')
    if not os.path.exists(os.path.join(PROJECT_ROOT, prompt_p)):
        prompt_p = os.path.join(tdir, 'Prompt', f'{proj}-{bid}', f'actual_prompt_{simple_tgt}.md')
    
    report_rows.append({
        'bug': f'{proj}-{bid}',
        'tech': tech,
        'target_cls': tcls,
        'package_cls': f'{pkg}.{cls_name}',
        'file_rel': os.path.relpath(p, PROJECT_ROOT).replace('\\', '/'),
        'sha256': hash_val,
        'prompt_rel': prompt_p.replace('\\', '/')
    })

print(json.dumps(report_rows, indent=2))
