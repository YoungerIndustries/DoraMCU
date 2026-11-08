package com.yisy.modules.doraai.model;

/**
 * Represents a code symbol (class, method, interface, enum, field) extracted from source files.
 * <p>
 * Used by the CodeIndex to provide AI context about project structure,
 * enabling the AI to answer questions about classes, methods, and their relationships.
 * </p>
 */
public class CodeSymbol {

    /** Types of code symbols that can be extracted. */
    public enum SymbolType {
        CLASS,
        INTERFACE,
        ENUM,
        METHOD,
        FIELD,
        CONSTRUCTOR,
        ANNOTATION,
        PACKAGE
    }

    private final SymbolType type;
    private final String name;
    private final String signature;
    private final String filePath;
    private final int startLine;
    private final int endLine;
    private final String accessModifier;
    private final String parentClass;

    public CodeSymbol(SymbolType type, String name, String signature, String filePath,
                       int startLine, int endLine, String accessModifier, String parentClass) {
        this.type = type;
        this.name = name;
        this.signature = signature;
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.accessModifier = accessModifier;
        this.parentClass = parentClass;
    }

    public SymbolType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getAccessModifier() {
        return accessModifier;
    }

    public String getParentClass() {
        return parentClass;
    }

    @Override
    public String toString() {
        return "CodeSymbol{type=" + type + ", name='" + name + "', file='" + filePath +
                "', lines=" + startLine + "-" + endLine + "}";
    }
}
