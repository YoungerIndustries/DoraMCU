package com.yisy.modules.doraai.service;

import com.yisy.modules.doraai.model.CodeIndex;
import com.yisy.modules.doraai.model.CodeSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectScannerTest {

    private ProjectScanner scanner;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        scanner = new ProjectScanner();
    }

    // ——— scanDirectory ———

    @Test
    void scanDirectory_throwsForNonExistentDirectory() {
        assertThrows(IllegalArgumentException.class, () ->
                scanner.scanDirectory("/nonexistent/path", new CodeIndex())
        );
    }

    @Test
    void scanDirectory_emptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);
        CodeIndex index = new CodeIndex();

        int count = scanner.scanDirectory(emptyDir.toString(), index);

        assertEquals(0, count);
        assertEquals(0, index.getFileCount());
    }

    @Test
    void scanDirectory_scansJavaFiles() throws IOException {
        Path javaFile = tempDir.resolve("Main.java");
        Files.writeString(javaFile, """
                package com.example;
                
                public class Main {
                    private String name;
                    
                    public void run() {
                        System.out.println("Hello");
                    }
                }
                """);

        CodeIndex index = new CodeIndex();
        int count = scanner.scanDirectory(tempDir.toString(), index);

        assertEquals(1, count);
        assertEquals(1, index.getFileCount());
        assertTrue(index.getSymbolCount() >= 1, "Should find at least the class symbol");
    }

    @Test
    void scanDirectory_scansMultipleFiles() throws IOException {
        Files.writeString(tempDir.resolve("Main.java"), "public class Main {}");
        Files.writeString(tempDir.resolve("Util.java"), "public class Util {}");
        Files.writeString(tempDir.resolve("App.py"), "class App: pass");

        CodeIndex index = new CodeIndex();
        int count = scanner.scanDirectory(tempDir.toString(), index);

        assertEquals(3, count);
        assertEquals(3, index.getFileCount());
    }

    @Test
    void scanDirectory_skipsNonSourceFiles() throws IOException {
        Files.writeString(tempDir.resolve("Main.java"), "public class Main {}");
        Files.writeString(tempDir.resolve("readme.txt"), "text file");
        Files.writeString(tempDir.resolve("data.json"), "{}");

        CodeIndex index = new CodeIndex();
        int count = scanner.scanDirectory(tempDir.toString(), index);

        assertEquals(1, count);
        assertEquals(1, index.getFileCount());
    }

    // ——— extractSymbols — Java ———

    @Test
    void extractSymbols_java_class() {
        String content = "public class MyClass {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("MyClass", cls.getName());
    }

    @Test
    void extractSymbols_java_interface() {
        String content = "public interface MyInterface {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol iface = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.INTERFACE)
                .findFirst()
                .orElseThrow();
        assertEquals("MyInterface", iface.getName());
    }

    @Test
    void extractSymbols_java_enum() {
        String content = "public enum Status { ACTIVE, INACTIVE }";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol enumSym = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.ENUM)
                .findFirst()
                .orElseThrow();
        assertEquals("Status", enumSym.getName());
    }

    @Test
    void extractSymbols_java_method() {
        String content = """
                public class Service {
                    public void process() {}
                }
                """;
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol method = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.METHOD)
                .findFirst()
                .orElseThrow();
        assertEquals("process", method.getName());
    }

    @Test
    void extractSymbols_java_package() {
        String content = "package com.example.service;";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol pkg = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.PACKAGE)
                .findFirst()
                .orElseThrow();
        assertEquals("com.example.service", pkg.getName());
    }

    @Test
    void extractSymbols_java_field() {
        String content = """
                public class User {
                    private String name = "";
                }
                """;
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol field = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.FIELD)
                .findFirst()
                .orElseThrow();
        assertEquals("name", field.getName());
    }

    @Test
    void extractSymbols_java_privateClass() {
        String content = "private class Inner {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("Inner", cls.getName());
        assertEquals("private", cls.getAccessModifier());
    }

    @Test
    void extractSymbols_java_abstractClass() {
        String content = "public abstract class AbstractService {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("AbstractService", cls.getName());
    }

    // ——— extractSymbols — Python ———

    @Test
    void extractSymbols_python_class() {
        String content = "class MyClass:\n    pass";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.py", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("MyClass", cls.getName());
    }

    @Test
    void extractSymbols_python_method() {
        String content = "class Service:\n    def process(self):\n        pass";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.py", content);

        CodeSymbol method = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.METHOD)
                .findFirst()
                .orElseThrow();
        assertEquals("process", method.getName());
        assertEquals("Service", method.getParentClass());
    }

    @Test
    void extractSymbols_python_multipleClasses() {
        String content = """
                class User:
                    def name(self):
                        pass
                
                class Admin:
                    def role(self):
                        pass
                """;
        List<CodeSymbol> symbols = scanner.extractSymbols("test.py", content);

        long classCount = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .count();
        assertEquals(2, classCount);
    }

    // ——— extractSymbols — Kotlin ———

    @Test
    void extractSymbols_kotlin_class() {
        String content = "class MyClass {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.kt", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("MyClass", cls.getName());
    }

    @Test
    void extractSymbols_kotlin_dataClass() {
        String content = "data class User(val name: String)";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.kt", content);

        CodeSymbol cls = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.CLASS)
                .findFirst()
                .orElseThrow();
        assertEquals("User", cls.getName());
    }

    @Test
    void extractSymbols_kotlin_package() {
        String content = "package com.example";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.kt", content);

        CodeSymbol pkg = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.PACKAGE)
                .findFirst()
                .orElseThrow();
        assertEquals("com.example", pkg.getName());
    }

    @Test
    void extractSymbols_kotlin_interface() {
        String content = "interface Repository {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.kt", content);

        CodeSymbol iface = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.INTERFACE)
                .findFirst()
                .orElseThrow();
        assertEquals("Repository", iface.getName());
    }

    // ——— extractSymbols — empty/edge cases ———

    @Test
    void extractSymbols_emptyContent() {
        List<CodeSymbol> symbols = scanner.extractSymbols("test.java", "");

        assertTrue(symbols.isEmpty());
    }

    @Test
    void extractSymbols_unknownLanguage() {
        String content = "func main() {}";
        List<CodeSymbol> symbols = scanner.extractSymbols("test.unknown", content);

        // Generic extraction should find the function
        CodeSymbol func = symbols.stream()
                .filter(s -> s.getType() == CodeSymbol.SymbolType.METHOD)
                .findFirst()
                .orElseThrow();
        assertEquals("main", func.getName());
    }
}
