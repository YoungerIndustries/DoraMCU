package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.model.CodeFile;
import com.yisy.modules.doraai.model.CodeIndex;
import com.yisy.modules.doraai.model.CodeSymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scans project directories for source files and extracts code symbols
 * (classes, methods, interfaces, enums) for the CodeIndex.
 * <p>
 * Uses regex-based parsing to extract symbols without a full AST parser.
 * Supports Java, Kotlin, Python, and other common languages.
 * </p>
 */
@Service
public class ProjectScanner {

    private static final Logger log = LoggerFactory.getLogger(ProjectScanner.class);

    /** Supported source file extensions. */
    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".kt", ".py", ".js", ".ts", ".go", ".rs",
            ".cpp", ".c", ".h", ".rb", ".cs", ".php"
    );

    /** Patterns to skip (build dirs, hidden dirs, etc.). */
    private static final Set<String> SKIP_DIRS = Set.of(
            "node_modules", ".git", "target", "build", "dist",
            ".gradle", ".idea", ".vscode", ".junie", "venv",
            "__pycache__", ".m2", ".cache"
    );

    // Java patterns
    private static final Pattern JAVA_PACKAGE = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern JAVA_IMPORT = Pattern.compile("^\\s*import\\s+([\\w.]+)\\s*;");
    private static final Pattern JAVA_CLASS = Pattern.compile(
            "^\\s*(?:(public|private|protected)\\s+)?(?:(static\\s+)?(?:abstract\\s+|final\\s+)*)?(class|interface|enum)\\s+(\\w+)",
            Pattern.MULTILINE
    );
    private static final Pattern JAVA_METHOD = Pattern.compile(
            "^\\s*(?:(public|private|protected)\\s+)?(?:static\\s+)?(?:final\\s+)?(?:synchronized\\s+)?(?:abstract\\s+)?([\\w<>\\[\\],\\s]+?)\\s+(\\w+)\\s*\\([^)]*\\)\\s*(?:throws\\s+[\\w,\\s]+)?\\s*\\{?",
            Pattern.MULTILINE
    );
    private static final Pattern JAVA_FIELD = Pattern.compile(
            "^\\s*(?:(public|private|protected)\\s+)?(?:static\\s+)?(?:final\\s+)?(?:volatile\\s+)?([\\w<>\\[\\],\\s]+?)\\s+(\\w+)\\s*=",
            Pattern.MULTILINE
    );

    /**
     * Scan a directory and index all source files into the provided CodeIndex.
     *
     * @param directory the root directory to scan
     * @param index the code index to populate
     * @return the number of files scanned
     */
    public int scanDirectory(String directory, CodeIndex index) {
        Path root = Paths.get(directory).toAbsolutePath().normalize();
        if (!Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Directory does not exist: " + directory);
        }

        log.info("Scanning directory: {}", root);
        int fileCount = 0;

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> sourceFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> !SKIP_DIRS.contains(p.getParent().getFileName().toString()))
                    .filter(p -> SOURCE_EXTENSIONS.contains(getFileExtension(p.toString())))
                    .toList();

            log.info("Found {} source files to scan", sourceFiles.size());

            for (Path file : sourceFiles) {
                try {
                    String content = Files.readString(file);
                    String relativePath = root.relativize(file).toString();
                    CodeFile codeFile = new CodeFile(relativePath, content);
                    List<CodeSymbol> symbols = extractSymbols(file.toString(), content);
                    index.addFile(codeFile, symbols);
                    fileCount++;
                } catch (IOException e) {
                    log.warn("Failed to read file: {}", file, e);
                }
            }
        } catch (IOException e) {
            log.error("Failed to walk directory: {}", root, e);
            throw new RuntimeException("Failed to scan directory: " + directory, e);
        }

        log.info("Scanned {} files, indexed {} symbols", fileCount, index.getSymbolCount());
        return fileCount;
    }

    /**
     * Extract code symbols from a source file.
     */
    public List<CodeSymbol> extractSymbols(String filePath, String content) {
        List<CodeSymbol> symbols = new ArrayList<>();
        String[] lines = content.split("\n");

        String language = getLanguage(filePath);
        switch (language) {
            case "java" -> extractJavaSymbols(lines, filePath, symbols);
            case "python" -> extractPythonSymbols(lines, filePath, symbols);
            case "kotlin" -> extractKotlinSymbols(lines, filePath, symbols);
            default -> {
                // Generic: extract function/method definitions
                extractGenericSymbols(lines, filePath, symbols);
            }
        }

        return symbols;
    }

    private void extractJavaSymbols(String[] lines, String filePath, List<CodeSymbol> symbols) {
        String currentPackage = null;
        String currentClass = null;
        int braceDepth = 0;
        int classStartLine = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Package declaration
            Matcher pkgMatcher = JAVA_PACKAGE.matcher(line);
            if (pkgMatcher.find()) {
                currentPackage = pkgMatcher.group(1);
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.PACKAGE, currentPackage, currentPackage,
                        filePath, i + 1, i + 1, null, null
                ));
            }

            // Class/interface/enum
            Matcher classMatcher = JAVA_CLASS.matcher(line);
            if (classMatcher.find()) {
                String access = classMatcher.group(1);
                String type = classMatcher.group(3);
                String name = classMatcher.group(4);
                CodeSymbol.SymbolType symbolType = switch (type.toLowerCase()) {
                    case "interface" -> CodeSymbol.SymbolType.INTERFACE;
                    case "enum" -> CodeSymbol.SymbolType.ENUM;
                    default -> CodeSymbol.SymbolType.CLASS;
                };
                currentClass = name;
                classStartLine = i + 1;
                String signature = (access != null ? access + " " : "") + type + " " + name;
                symbols.add(new CodeSymbol(
                        symbolType, name, signature, filePath,
                        i + 1, i + 1, access, null
                ));
            }

            // Method
            Matcher methodMatcher = JAVA_METHOD.matcher(line);
            if (methodMatcher.find() && !line.startsWith("//") && !line.startsWith("*")) {
                String access = methodMatcher.group(1);
                String returnType = methodMatcher.group(2).trim();
                String name = methodMatcher.group(3);
                // Skip if it looks like a class declaration
                if (!name.equals("class") && !name.equals("interface") && !name.equals("enum")
                        && !line.contains(" new ") && !line.contains("import ")) {
                    String signature = (access != null ? access + " " : "") +
                            returnType + " " + name + "(";
                    symbols.add(new CodeSymbol(
                            CodeSymbol.SymbolType.METHOD, name, signature, filePath,
                            i + 1, i + 1, access, currentClass
                    ));
                }
            }

            // Field
            Matcher fieldMatcher = JAVA_FIELD.matcher(line);
            if (fieldMatcher.find() && currentClass != null) {
                String access = fieldMatcher.group(1);
                String type = fieldMatcher.group(2).trim();
                String name = fieldMatcher.group(3);
                String signature = (access != null ? access + " " : "") + type + " " + name;
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.FIELD, name, signature, filePath,
                        i + 1, i + 1, access, currentClass
                ));
            }

            // Track brace depth for class end detection
            for (char c : line.toCharArray()) {
                if (c == '{') braceDepth++;
                if (c == '}') {
                    if (braceDepth == 0 && currentClass != null && classStartLine > 0) {
                        // Update class end line
                        for (int j = symbols.size() - 1; j >= 0; j--) {
                            CodeSymbol s = symbols.get(j);
                            if (s.getName().equals(currentClass) &&
                                    (s.getType() == CodeSymbol.SymbolType.CLASS ||
                                     s.getType() == CodeSymbol.SymbolType.INTERFACE ||
                                     s.getType() == CodeSymbol.SymbolType.ENUM)) {
                                // Update end line by creating a new symbol with updated end line
                                symbols.set(j, new CodeSymbol(
                                        s.getType(), s.getName(), s.getSignature(), s.getFilePath(),
                                        s.getStartLine(), i + 1, s.getAccessModifier(), s.getParentClass()
                                ));
                                break;
                            }
                        }
                        currentClass = null;
                    }
                    braceDepth--;
                    if (braceDepth < 0) braceDepth = 0;
                }
            }
        }
    }

    private void extractPythonSymbols(String[] lines, String filePath, List<CodeSymbol> symbols) {
        String currentClass = null;
        Pattern classPattern = Pattern.compile("^\\s*class\\s+(\\w+)");
        Pattern methodPattern = Pattern.compile("^\\s*def\\s+(\\w+)\\s*\\(");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            Matcher classMatcher = classPattern.matcher(line);
            if (classMatcher.find()) {
                currentClass = classMatcher.group(1);
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.CLASS, currentClass, "class " + currentClass,
                        filePath, i + 1, i + 1, null, null
                ));
            }

            Matcher methodMatcher = methodPattern.matcher(line);
            if (methodMatcher.find()) {
                String name = methodMatcher.group(1);
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.METHOD, name, "def " + name + "(",
                        filePath, i + 1, i + 1, null, currentClass
                ));
            }
        }
    }

    private void extractKotlinSymbols(String[] lines, String filePath, List<CodeSymbol> symbols) {
        String currentPackage = null;
        String currentClass = null;
        Pattern pkgPattern = Pattern.compile("^\\s*package\\s+([\\w.]+)");
        Pattern classPattern = Pattern.compile("^\\s*(?:(public|private|protected)\\s+)?(?:abstract\\s+|open\\s+|sealed\\s+|data\\s+|enum\\s+|value\\s+)*(class|interface|enum)\\s+(\\w+)");
        Pattern methodPattern = Pattern.compile("^\\s*(?:(public|private|protected)\\s+)?(?:fun|val|var)\\s+(?:<[^>]*>\\s+)?(\\w+)\\s*[<(]");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            Matcher pkgMatcher = pkgPattern.matcher(line);
            if (pkgMatcher.find()) {
                currentPackage = pkgMatcher.group(1);
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.PACKAGE, currentPackage, currentPackage,
                        filePath, i + 1, i + 1, null, null
                ));
            }

            Matcher classMatcher = classPattern.matcher(line);
            if (classMatcher.find()) {
                currentClass = classMatcher.group(3);
                String access = classMatcher.group(1);
                String type = classMatcher.group(2);
                CodeSymbol.SymbolType symbolType = switch (type.toLowerCase()) {
                    case "interface" -> CodeSymbol.SymbolType.INTERFACE;
                    case "enum" -> CodeSymbol.SymbolType.ENUM;
                    default -> CodeSymbol.SymbolType.CLASS;
                };
                symbols.add(new CodeSymbol(
                        symbolType, currentClass, type + " " + currentClass,
                        filePath, i + 1, i + 1, access, null
                ));
            }

            Matcher methodMatcher = methodPattern.matcher(line);
            if (methodMatcher.find()) {
                String name = methodMatcher.group(1);
                if (!name.equals("class") && !name.equals("interface") && !name.equals("enum")) {
                    symbols.add(new CodeSymbol(
                            CodeSymbol.SymbolType.METHOD, name, "fun " + name + "(",
                            filePath, i + 1, i + 1, null, currentClass
                    ));
                }
            }
        }
    }

    private void extractGenericSymbols(String[] lines, String filePath, List<CodeSymbol> symbols) {
        // Generic function/method detection
        Pattern funcPattern = Pattern.compile("^\\s*(?:func|function|fn|def)\\s+(\\w+)");
        for (int i = 0; i < lines.length; i++) {
            Matcher matcher = funcPattern.matcher(lines[i]);
            if (matcher.find()) {
                symbols.add(new CodeSymbol(
                        CodeSymbol.SymbolType.METHOD, matcher.group(1),
                        "func " + matcher.group(1) + "(",
                        filePath, i + 1, i + 1, null, null
                ));
            }
        }
    }

    private String getFileExtension(String path) {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex >= 0) {
            return path.substring(dotIndex).toLowerCase();
        }
        return "";
    }

    private String getLanguage(String path) {
        String ext = getFileExtension(path);
        return switch (ext) {
            case ".java" -> "java";
            case ".kt" -> "kotlin";
            case ".py" -> "python";
            case ".js", ".ts" -> "javascript";
            case ".go" -> "go";
            case ".rs" -> "rust";
            case ".cpp", ".c", ".h" -> "cpp";
            case ".rb" -> "ruby";
            case ".cs" -> "csharp";
            case ".php" -> "php";
            default -> "unknown";
        };
    }
}
