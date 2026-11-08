package com.yisy.modules.doraai.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CodeIndexTest {

    private CodeIndex index;

    @BeforeEach
    void setUp() {
        index = new CodeIndex();
    }

    @Test
    void newIndex_isEmpty() {
        assertEquals(0, index.getFileCount());
        assertEquals(0, index.getSymbolCount());
        assertTrue(index.getFilePaths().isEmpty());
        assertTrue(index.getAllSymbols().isEmpty());
    }

    @Test
    void addFile_increasesCounts() {
        CodeFile file = new CodeFile("src/Main.java", "public class Main {}");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        assertEquals(1, index.getFileCount());
        assertEquals(1, index.getSymbolCount());
    }

    @Test
    void addFile_storesFileContent() {
        String content = "public class Main { public static void main(String[] args) {} }";
        CodeFile file = new CodeFile("src/Main.java", content);

        index.addFile(file, List.of());

        assertEquals(content, index.getFileContent("src/Main.java"));
    }

    @Test
    void addFile_returnsFileByPath() {
        CodeFile file = new CodeFile("src/Main.java", "content");

        index.addFile(file, List.of());

        assertNotNull(index.getFile("src/Main.java"));
        assertEquals("src/Main.java", index.getFile("src/Main.java").getPath());
    }

    @Test
    void addFile_returnsNullForMissingFile() {
        assertNull(index.getFile("nonexistent.java"));
        assertNull(index.getFileContent("nonexistent.java"));
    }

    @Test
    void addMultipleFiles_tracksAllPaths() {
        CodeFile file1 = new CodeFile("src/Main.java", "content1");
        CodeFile file2 = new CodeFile("src/Util.java", "content2");

        index.addFile(file1, List.of());
        index.addFile(file2, List.of());

        assertEquals(2, index.getFileCount());
        assertTrue(index.getFilePaths().contains("src/Main.java"));
        assertTrue(index.getFilePaths().contains("src/Util.java"));
    }

    @Test
    void searchSymbols_findsByName() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        List<CodeSymbol> results = index.searchSymbols("MyClass");
        assertEquals(1, results.size());
        assertEquals("MyClass", results.get(0).getName());
    }

    @Test
    void searchSymbols_caseInsensitive() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        List<CodeSymbol> results = index.searchSymbols("myclass");
        assertEquals(1, results.size());
    }

    @Test
    void searchSymbols_partialMatch() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        List<CodeSymbol> results = index.searchSymbols("My");
        assertEquals(1, results.size());
    }

    @Test
    void searchSymbols_bySignature() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.METHOD, "processData", "public void processData(",
                "src/Main.java", 5, 5, "public", "Main"
        );

        index.addFile(file, List.of(symbol));

        List<CodeSymbol> results = index.searchSymbols("process");
        assertEquals(1, results.size());
        assertEquals("processData", results.get(0).getName());
    }

    @Test
    void searchSymbols_emptyQuery() {
        assertTrue(index.searchSymbols(null).isEmpty());
        assertTrue(index.searchSymbols("").isEmpty());
        assertTrue(index.searchSymbols("   ").isEmpty());
    }

    @Test
    void searchSymbols_noMatch() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        assertTrue(index.searchSymbols("NonExistent").isEmpty());
    }

    @Test
    void getSymbolsByType_returnsMatchingSymbols() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol cls = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );
        CodeSymbol method = new CodeSymbol(
                CodeSymbol.SymbolType.METHOD, "run", "public void run(",
                "src/Main.java", 3, 3, "public", "Main"
        );

        index.addFile(file, List.of(cls, method));

        assertEquals(1, index.getSymbolsByType(CodeSymbol.SymbolType.CLASS).size());
        assertEquals(1, index.getSymbolsByType(CodeSymbol.SymbolType.METHOD).size());
        assertTrue(index.getSymbolsByType(CodeSymbol.SymbolType.INTERFACE).isEmpty());
    }

    @Test
    void removeFile_removesFileAndSymbols() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));
        index.removeFile("src/Main.java");

        assertEquals(0, index.getFileCount());
        assertEquals(0, index.getSymbolCount());
        assertNull(index.getFile("src/Main.java"));
    }

    @Test
    void removeFile_nonExistentDoesNothing() {
        index.removeFile("nonexistent.java");
        assertEquals(0, index.getFileCount());
    }

    @Test
    void removeFile_keepsOtherFiles() {
        CodeFile file1 = new CodeFile("src/Main.java", "content1");
        CodeFile file2 = new CodeFile("src/Util.java", "content2");
        CodeSymbol s1 = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );
        CodeSymbol s2 = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Util", "public class Util",
                "src/Util.java", 1, 1, "public", null
        );

        index.addFile(file1, List.of(s1));
        index.addFile(file2, List.of(s2));
        index.removeFile("src/Main.java");

        assertEquals(1, index.getFileCount());
        assertEquals(1, index.getSymbolCount());
        assertNotNull(index.getFile("src/Util.java"));
        assertNull(index.getFile("src/Main.java"));
    }

    @Test
    void getProjectOverview_containsCounts() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        String overview = index.getProjectOverview();
        assertTrue(overview.contains("Total files: 1"));
        assertTrue(overview.contains("Total symbols: 1"));
        assertTrue(overview.contains("CLASS: 1"));
    }

    @Test
    void getProjectOverview_listsClasses() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        String overview = index.getProjectOverview();
        assertTrue(overview.contains("Classes:"));
        assertTrue(overview.contains("MyClass"));
    }

    @Test
    void getRelevantContext_returnsMatchingFiles() {
        CodeFile file = new CodeFile("src/Main.java", "public class Main {}");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        String context = index.getRelevantContext("Main", 5);
        assertTrue(context.contains("src/Main.java"));
        assertTrue(context.contains("public class Main {}"));
    }

    @Test
    void getRelevantContext_limitsFiles() {
        for (int i = 0; i < 5; i++) {
            CodeFile file = new CodeFile("src/File" + i + ".java", "class File" + i + " {}");
            CodeSymbol symbol = new CodeSymbol(
                    CodeSymbol.SymbolType.CLASS, "File" + i, "class File" + i,
                    "src/File" + i + ".java", 1, 1, null, null
            );
            index.addFile(file, List.of(symbol));
        }

        String context = index.getRelevantContext("File", 2);
        // Should contain at most 2 file sections
        int fileCount = 0;
        int idx = 0;
        while ((idx = context.indexOf("File: ", idx)) != -1) {
            fileCount++;
            idx += 6;
        }
        assertTrue(fileCount <= 2, "Should limit to 2 files, found: " + fileCount);
    }

    @Test
    void getRelevantContext_emptyWhenNoMatch() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        String context = index.getRelevantContext("NonExistent", 5);
        assertTrue(context.isBlank());
    }

    @Test
    void clear_removesAllData() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));
        index.clear();

        assertEquals(0, index.getFileCount());
        assertEquals(0, index.getSymbolCount());
        assertTrue(index.getFilePaths().isEmpty());
    }

    @Test
    void addFile_withMultipleSymbols() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol cls = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );
        CodeSymbol method = new CodeSymbol(
                CodeSymbol.SymbolType.METHOD, "run", "public void run(",
                "src/Main.java", 3, 3, "public", "Main"
        );
        CodeSymbol field = new CodeSymbol(
                CodeSymbol.SymbolType.FIELD, "name", "private String name",
                "src/Main.java", 2, 2, "private", "Main"
        );

        index.addFile(file, List.of(cls, method, field));

        assertEquals(1, index.getFileCount());
        assertEquals(3, index.getSymbolCount());
    }

    @Test
    void getAllSymbols_returnsCopy() {
        CodeFile file = new CodeFile("src/Main.java", "content");
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Main", "public class Main",
                "src/Main.java", 1, 1, "public", null
        );

        index.addFile(file, List.of(symbol));

        List<CodeSymbol> symbols = index.getAllSymbols();
        assertEquals(1, symbols.size());
        // Modify returned list should not affect index
        symbols.clear();
        assertEquals(1, index.getSymbolCount());
    }

    @Test
    void getFilePaths_returnsCopy() {
        CodeFile file = new CodeFile("src/Main.java", "content");

        index.addFile(file, List.of());

        var paths = index.getFilePaths();
        assertEquals(1, paths.size());
        // Should be immutable copy
        assertThrows(UnsupportedOperationException.class, () -> paths.add("test"));
    }
}
