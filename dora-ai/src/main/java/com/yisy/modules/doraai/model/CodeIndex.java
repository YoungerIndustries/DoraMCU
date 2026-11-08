package com.yisy.modules.doraai.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory index of project code symbols for AI-assisted code understanding.
 * <p>
 * Stores indexed files and their extracted symbols (classes, methods, interfaces, etc.)
 * and provides search capabilities to find relevant code context for AI queries.
 * Thread-safe with ConcurrentHashMap.
 * </p>
 */
public class CodeIndex {

    private final Map<String, CodeFile> files = new ConcurrentHashMap<>();
    private final Map<String, List<CodeSymbol>> symbolsByName = new ConcurrentHashMap<>();
    private final Map<String, List<CodeSymbol>> symbolsByType = new ConcurrentHashMap<>();
    private final List<CodeSymbol> allSymbols = Collections.synchronizedList(new ArrayList<>());

    /**
     * Add a file and its extracted symbols to the index.
     */
    public void addFile(CodeFile file, List<CodeSymbol> symbols) {
        files.put(file.getPath(), file);
        synchronized (allSymbols) {
            allSymbols.addAll(symbols);
        }
        for (CodeSymbol symbol : symbols) {
            symbolsByName.computeIfAbsent(symbol.getName().toLowerCase(), k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(symbol);
            symbolsByType.computeIfAbsent(symbol.getType().name(), k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(symbol);
        }
    }

    /**
     * Remove a file and its symbols from the index.
     */
    public void removeFile(String path) {
        CodeFile removed = files.remove(path);
        if (removed != null) {
            synchronized (allSymbols) {
                allSymbols.removeIf(s -> s.getFilePath().equals(path));
            }
            symbolsByName.values().forEach(list -> list.removeIf(s -> s.getFilePath().equals(path)));
            symbolsByType.values().forEach(list -> list.removeIf(s -> s.getFilePath().equals(path)));
        }
    }

    /**
     * Search symbols by name (case-insensitive, partial match).
     */
    public List<CodeSymbol> searchSymbols(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String lowerQuery = query.toLowerCase();
        List<CodeSymbol> results = new ArrayList<>();
        symbolsByName.forEach((name, symbols) -> {
            if (name.contains(lowerQuery)) {
                results.addAll(symbols);
            }
        });
        // Also search by signature for partial matches
        synchronized (allSymbols) {
            for (CodeSymbol symbol : allSymbols) {
                if (symbol.getSignature() != null &&
                    symbol.getSignature().toLowerCase().contains(lowerQuery) &&
                    !results.contains(symbol)) {
                    results.add(symbol);
                }
            }
        }
        return results;
    }

    /**
     * Get symbols by type.
     */
    public List<CodeSymbol> getSymbolsByType(CodeSymbol.SymbolType type) {
        return symbolsByType.getOrDefault(type.name(), List.of());
    }

    /**
     * Get file content by path.
     */
    public String getFileContent(String path) {
        CodeFile file = files.get(path);
        return file != null ? file.getContent() : null;
    }

    /**
     * Get a registered file by path.
     */
    public CodeFile getFile(String path) {
        return files.get(path);
    }

    /**
     * Get all registered file paths.
     */
    public Set<String> getFilePaths() {
        return Set.copyOf(files.keySet());
    }

    /**
     * Get all symbols.
     */
    public List<CodeSymbol> getAllSymbols() {
        synchronized (allSymbols) {
            return new ArrayList<>(allSymbols);
        }
    }

    /**
     * Get a high-level project overview for AI context.
     * Returns a summary of the project structure including package hierarchy,
     * class counts, and key symbols.
     */
    public String getProjectOverview() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project Structure Overview:\n");
        sb.append("Total files: ").append(files.size()).append("\n");
        sb.append("Total symbols: ").append(allSymbols.size()).append("\n\n");

        // Group by package
        Map<String, List<CodeSymbol>> byPackage = new HashMap<>();
        synchronized (allSymbols) {
            for (CodeSymbol symbol : allSymbols) {
                if (symbol.getType() == CodeSymbol.SymbolType.PACKAGE) {
                    String pkg = symbol.getName();
                    byPackage.computeIfAbsent(pkg, k -> new ArrayList<>()).add(symbol);
                }
            }
        }

        // Count by type
        Map<String, Long> typeCounts = new HashMap<>();
        synchronized (allSymbols) {
            typeCounts = allSymbols.stream()
                    .collect(Collectors.groupingBy(s -> s.getType().name(), Collectors.counting()));
        }

        sb.append("Symbol counts by type:\n");
        typeCounts.forEach((type, count) -> sb.append("  ").append(type).append(": ").append(count).append("\n"));
        sb.append("\n");

        // List classes and their methods
        List<CodeSymbol> classes = getSymbolsByType(CodeSymbol.SymbolType.CLASS);
        if (!classes.isEmpty()) {
            sb.append("Classes:\n");
            for (CodeSymbol cls : classes.stream().limit(50).toList()) {
                sb.append("  - ").append(cls.getName()).append(" (").append(cls.getFilePath()).append(")\n");
                List<CodeSymbol> methods = symbolsByName.values().stream()
                        .flatMap(List::stream)
                        .filter(s -> s.getType() == CodeSymbol.SymbolType.METHOD &&
                                     s.getParentClass() != null &&
                                     s.getParentClass().equals(cls.getName()))
                        .toList();
                for (CodeSymbol method : methods) {
                    sb.append("    · ").append(method.getSignature()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    /**
     * Find the most relevant files for a given query.
     * Returns file contents for up to the top N matching files.
     */
    public String getRelevantContext(String query, int maxFiles) {
        List<CodeSymbol> matches = searchSymbols(query);
        Set<String> relevantFiles = new LinkedHashSet<>();
        for (CodeSymbol symbol : matches) {
            relevantFiles.add(symbol.getFilePath());
            if (relevantFiles.size() >= maxFiles) {
                break;
            }
        }

        StringBuilder context = new StringBuilder();
        int count = 0;
        for (String path : relevantFiles) {
            CodeFile file = files.get(path);
            if (file != null && count < maxFiles) {
                context.append("File: ").append(file.getPath()).append("\n");
                context.append("Language: ").append(file.getLanguage()).append("\n");
                context.append("```\n").append(file.getContent()).append("\n```\n\n");
                count++;
            }
        }
        return context.toString();
    }

    /**
     * Clear all indexed data.
     */
    public void clear() {
        files.clear();
        symbolsByName.clear();
        symbolsByType.clear();
        synchronized (allSymbols) {
            allSymbols.clear();
        }
    }

    /**
     * Get the number of indexed files.
     */
    public int getFileCount() {
        return files.size();
    }

    /**
     * Get the number of indexed symbols.
     */
    public int getSymbolCount() {
        synchronized (allSymbols) {
            return allSymbols.size();
        }
    }
}
