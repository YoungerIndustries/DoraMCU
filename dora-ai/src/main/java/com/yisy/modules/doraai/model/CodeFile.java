package com.yisy.modules.doraai.model;

/**
 * Represents a source code file in the project index.
 * <p>
 * Holds the file path, content, and a list of extracted symbols
 * (classes, methods, interfaces, enums) for AI-assisted code understanding.
 * </p>
 */
public class CodeFile {

    private final String path;
    private final String content;
    private final String language;

    public CodeFile(String path, String content) {
        this.path = path;
        this.content = content;
        this.language = detectLanguage(path);
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public String getLanguage() {
        return language;
    }

    private String detectLanguage(String path) {
        if (path == null) {
            return "unknown";
        }
        String lower = path.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".kt")) return "kotlin";
        if (lower.endsWith(".py")) return "python";
        if (lower.endsWith(".js") || lower.endsWith(".ts")) return "javascript";
        if (lower.endsWith(".go")) return "go";
        if (lower.endsWith(".rs")) return "rust";
        if (lower.endsWith(".cpp") || lower.endsWith(".c") || lower.endsWith(".h")) return "cpp";
        if (lower.endsWith(".rb")) return "ruby";
        if (lower.endsWith(".cs")) return "csharp";
        if (lower.endsWith(".php")) return "php";
        if (lower.endsWith(".sql")) return "sql";
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "html";
        if (lower.endsWith(".css")) return "css";
        if (lower.endsWith(".xml")) return "xml";
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) return "yaml";
        if (lower.endsWith(".md")) return "markdown";
        return "unknown";
    }

    @Override
    public String toString() {
        return "CodeFile{path='" + path + "', language='" + language + "'}";
    }
}
