package analyzer;

import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.lang.model.element.Modifier;
import javax.tools.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Deterministic Java AST Parser using the standard Java Compiler Tree API.
 * Extracts classes, nested types, enums, constructors, methods, parameters,
 * and source line spans without external dependencies.
 */
public class JavaAstParser {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java analyzer.JavaAstParser <file.java> | --batch <list.txt>");
            System.exit(1);
        }

        try {
            if ("--batch".equals(args[0])) {
                if (args.length < 2) {
                    System.err.println("Usage: java analyzer.JavaAstParser --batch <list.txt>");
                    System.exit(1);
                }
                List<String> files = Files.readAllLines(new File(args[1]).toPath(), StandardCharsets.UTF_8);
                for (String path : files) {
                    path = path.trim();
                    if (!path.isEmpty() && !path.startsWith("#")) {
                        parseAndPrint(new File(path));
                    }
                }
            } else {
                parseAndPrint(new File(args[0]));
            }
        } catch (Exception e) {
            System.err.println("Fatal error in JavaAstParser: " + e.getMessage());
            e.printStackTrace(System.err);
            System.exit(2);
        }
    }

    public static void parseAndPrint(File file) {
        try {
            String json = parseToJson(file);
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("{\"error\": " + quote(e.getMessage()) + ", \"file\": " + quote(file.getPath()) + "}");
        }
    }

    public static String parseToJson(File file) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("System Java Compiler not available. Running on a JRE instead of a JDK?");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
        Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjectsFromFiles(Collections.singletonList(file));

        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, diagnostics, Collections.singletonList("-proc:none"), null, compUnits);
        Iterable<? extends CompilationUnitTree> asts = task.parse();
        SourcePositions positions = Trees.instance(task).getSourcePositions();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"file\":").append(quote(file.getAbsolutePath())).append(",");

        for (CompilationUnitTree ast : asts) {
            LineMap lineMap = ast.getLineMap();
            String packageName = ast.getPackageName() != null ? ast.getPackageName().toString() : "";
            sb.append("\"package\":").append(quote(packageName)).append(",");

            // Imports
            sb.append("\"imports\":[");
            boolean firstImport = true;
            for (ImportTree imp : ast.getImports()) {
                if (!firstImport) sb.append(",");
                firstImport = false;
                sb.append(quote(imp.getQualifiedIdentifier().toString()));
            }
            sb.append("],");

            // Classes & Types
            sb.append("\"types\":[");
            boolean firstType = true;
            for (Tree decl : ast.getTypeDecls()) {
                if (decl instanceof ClassTree) {
                    if (!firstType) sb.append(",");
                    firstType = false;
                    appendClassJson(sb, (ClassTree) decl, ast, positions, lineMap, packageName, "");
                }
            }
            sb.append("]");
            break; // One compilation unit per file
        }

        sb.append("}");
        fileManager.close();
        return sb.toString();
    }

    private static void appendClassJson(StringBuilder sb, ClassTree cls, CompilationUnitTree ast,
                                        SourcePositions positions, LineMap lineMap,
                                        String packageName, String outerPrefix) {
        String simpleName = cls.getSimpleName().toString();
        String currentPrefix = outerPrefix.isEmpty() ? simpleName : outerPrefix + "$" + simpleName;
        String fqcn = packageName.isEmpty() ? currentPrefix : packageName + "." + currentPrefix;

        long startPos = positions.getStartPosition(ast, cls);
        long endPos = positions.getEndPosition(ast, cls);
        long startLine = lineMap != null && startPos >= 0 ? lineMap.getLineNumber(startPos) : -1;
        long endLine = lineMap != null && endPos >= 0 ? lineMap.getLineNumber(endPos) : -1;

        sb.append("{");
        sb.append("\"name\":").append(quote(simpleName)).append(",");
        sb.append("\"fqcn\":").append(quote(fqcn)).append(",");
        sb.append("\"kind\":").append(quote(cls.getKind().name().toLowerCase())).append(",");
        sb.append("\"start_line\":").append(startLine).append(",");
        sb.append("\"end_line\":").append(endLine).append(",");

        // Modifiers
        ModifiersTree mods = cls.getModifiers();
        appendModifiersJson(sb, mods);

        // Superclass & Interfaces
        String superclass = cls.getExtendsClause() != null ? cls.getExtendsClause().toString().trim() : "";
        sb.append("\"extends\":").append(quote(superclass)).append(",");
        sb.append("\"implements\":[");
        boolean firstIf = true;
        for (Tree iface : cls.getImplementsClause()) {
            if (!firstIf) sb.append(",");
            firstIf = false;
            sb.append(quote(iface.toString().trim()));
        }
        sb.append("],");

        // Enum constants
        sb.append("\"enum_constants\":[");
        boolean firstEnum = true;
        if (cls.getKind() == Tree.Kind.ENUM) {
            for (Tree member : cls.getMembers()) {
                if (member instanceof VariableTree) {
                    VariableTree var = (VariableTree) member;
                    // Enum constants in Tree are variables with kind ENUM_CONSTANT or matching type
                    if (var.getKind().name().contains("ENUM")) {
                        if (!firstEnum) sb.append(",");
                        firstEnum = false;
                        sb.append(quote(var.getName().toString()));
                    }
                }
            }
        }
        sb.append("],");

        // Members: constructors, methods, nested types
        List<MethodTree> constructors = new ArrayList<>();
        List<MethodTree> methods = new ArrayList<>();
        List<ClassTree> nestedTypes = new ArrayList<>();
        List<VariableTree> fields = new ArrayList<>();

        for (Tree member : cls.getMembers()) {
            if (member instanceof MethodTree) {
                MethodTree m = (MethodTree) member;
                if (m.getName().contentEquals("<init>")) {
                    constructors.add(m);
                } else if (!m.getName().contentEquals("<clinit>")) {
                    methods.add(m);
                }
            } else if (member instanceof ClassTree) {
                nestedTypes.add((ClassTree) member);
            } else if (member instanceof VariableTree) {
                fields.add((VariableTree) member);
            }
        }

        // Constructors
        sb.append("\"constructors\":[");
        boolean firstCtor = true;
        for (MethodTree ctor : constructors) {
            if (!firstCtor) sb.append(",");
            firstCtor = false;
            appendCallableJson(sb, ctor, simpleName, ast, positions, lineMap, true);
        }
        sb.append("],");

        // Methods
        sb.append("\"methods\":[");
        boolean firstMethod = true;
        for (MethodTree m : methods) {
            if (!firstMethod) sb.append(",");
            firstMethod = false;
            appendCallableJson(sb, m, m.getName().toString(), ast, positions, lineMap, false);
        }
        sb.append("],");

        // Fields
        sb.append("\"fields\":[");
        boolean firstField = true;
        for (VariableTree f : fields) {
            if (!firstField) sb.append(",");
            firstField = false;
            appendFieldJson(sb, f);
        }
        sb.append("],");

        // Nested types
        sb.append("\"nested_types\":[");
        boolean firstNested = true;
        for (ClassTree nested : nestedTypes) {
            if (!firstNested) sb.append(",");
            firstNested = false;
            appendClassJson(sb, nested, ast, positions, lineMap, packageName, currentPrefix);
        }
        sb.append("]");

        sb.append("}");
    }

    private static void appendCallableJson(StringBuilder sb, MethodTree m, String name,
                                           CompilationUnitTree ast, SourcePositions positions,
                                           LineMap lineMap, boolean isConstructor) {
        long startPos = positions.getStartPosition(ast, m);
        long endPos = positions.getEndPosition(ast, m);
        long startLine = lineMap != null && startPos >= 0 ? lineMap.getLineNumber(startPos) : -1;
        long endLine = lineMap != null && endPos >= 0 ? lineMap.getLineNumber(endPos) : -1;

        sb.append("{");
        sb.append("\"name\":").append(quote(name)).append(",");
        sb.append("\"kind\":").append(quote(isConstructor ? "constructor" : "method")).append(",");
        sb.append("\"start_line\":").append(startLine).append(",");
        sb.append("\"end_line\":").append(endLine).append(",");

        // Modifiers
        ModifiersTree mods = m.getModifiers();
        appendModifiersJson(sb, mods);

        // Return type
        String returnType = isConstructor ? null : (m.getReturnType() != null ? m.getReturnType().toString().trim() : "void");
        if (returnType == null) {
            sb.append("\"return_type\":null,");
        } else {
            sb.append("\"return_type\":").append(quote(returnType)).append(",");
        }

        // Parameters
        sb.append("\"parameters\":[");
        boolean firstParam = true;
        for (VariableTree param : m.getParameters()) {
            if (!firstParam) sb.append(",");
            firstParam = false;
            sb.append("{");
            sb.append("\"name\":").append(quote(param.getName().toString())).append(",");
            sb.append("\"type\":").append(quote(param.getType() != null ? param.getType().toString().trim() : "Object")).append(",");
            sb.append("\"annotations\":[");
            boolean firstAnn = true;
            if (param.getModifiers() != null) {
                for (AnnotationTree ann : param.getModifiers().getAnnotations()) {
                    if (!firstAnn) sb.append(",");
                    firstAnn = false;
                    sb.append(quote(ann.getAnnotationType().toString().trim()));
                }
            }
            sb.append("]}");
        }
        sb.append("],");

        // Thrown Exceptions
        sb.append("\"throws\":[");
        boolean firstThrow = true;
        for (ExpressionTree ex : m.getThrows()) {
            if (!firstThrow) sb.append(",");
            firstThrow = false;
            sb.append(quote(ex.toString().trim()));
        }
        sb.append("]");

        sb.append("}");
    }

    private static void appendFieldJson(StringBuilder sb, VariableTree f) {
        sb.append("{");
        sb.append("\"name\":").append(quote(f.getName().toString())).append(",");
        sb.append("\"type\":").append(quote(f.getType() != null ? f.getType().toString().trim() : "Object")).append(",");
        appendModifiersJson(sb, f.getModifiers());
        sb.deleteCharAt(sb.length() - 1); // remove trailing comma
        sb.append("}");
    }

    private static void appendModifiersJson(StringBuilder sb, ModifiersTree mods) {
        String visibility = "package";
        boolean isStatic = false;
        boolean isAbstract = false;
        boolean isFinal = false;
        List<String> annotations = new ArrayList<>();

        if (mods != null) {
            Set<Modifier> flags = mods.getFlags();
            if (flags.contains(Modifier.PUBLIC)) visibility = "public";
            else if (flags.contains(Modifier.PROTECTED)) visibility = "protected";
            else if (flags.contains(Modifier.PRIVATE)) visibility = "private";

            isStatic = flags.contains(Modifier.STATIC);
            isAbstract = flags.contains(Modifier.ABSTRACT);
            isFinal = flags.contains(Modifier.FINAL);

            for (AnnotationTree ann : mods.getAnnotations()) {
                annotations.add(ann.getAnnotationType().toString().trim());
            }
        }

        sb.append("\"visibility\":").append(quote(visibility)).append(",");
        sb.append("\"static\":").append(isStatic).append(",");
        sb.append("\"abstract\":").append(isAbstract).append(",");
        sb.append("\"final\":").append(isFinal).append(",");
        sb.append("\"annotations\":[");
        boolean firstAnn = true;
        for (String ann : annotations) {
            if (!firstAnn) sb.append(",");
            firstAnn = false;
            sb.append(quote(ann));
        }
        sb.append("],");
    }

    private static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
