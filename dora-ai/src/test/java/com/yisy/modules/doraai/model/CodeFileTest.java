package com.yisy.modules.doraai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeFileTest {

    @Test
    void constructor_setsPathAndContent() {
        CodeFile file = new CodeFile("src/Main.java", "public class Main {}");

        assertEquals("src/Main.java", file.getPath());
        assertEquals("public class Main {}", file.getContent());
    }

    @Test
    void detectLanguage_java() {
        CodeFile file = new CodeFile("src/Main.java", "");

        assertEquals("java", file.getLanguage());
    }

    @Test
    void detectLanguage_kotlin() {
        CodeFile file = new CodeFile("src/Main.kt", "");

        assertEquals("kotlin", file.getLanguage());
    }

    @Test
    void detectLanguage_python() {
        CodeFile file = new CodeFile("src/main.py", "");

        assertEquals("python", file.getLanguage());
    }

    @Test
    void detectLanguage_javascript() {
        CodeFile file = new CodeFile("src/app.js", "");

        assertEquals("javascript", file.getLanguage());
    }

    @Test
    void detectLanguage_typescript() {
        CodeFile file = new CodeFile("src/app.ts", "");

        assertEquals("javascript", file.getLanguage());
    }

    @Test
    void detectLanguage_go() {
        CodeFile file = new CodeFile("src/main.go", "");

        assertEquals("go", file.getLanguage());
    }

    @Test
    void detectLanguage_rust() {
        CodeFile file = new CodeFile("src/main.rs", "");

        assertEquals("rust", file.getLanguage());
    }

    @Test
    void detectLanguage_cpp() {
        CodeFile file = new CodeFile("src/main.cpp", "");

        assertEquals("cpp", file.getLanguage());
    }

    @Test
    void detectLanguage_c() {
        CodeFile file = new CodeFile("src/main.c", "");

        assertEquals("cpp", file.getLanguage());
    }

    @Test
    void detectLanguage_header() {
        CodeFile file = new CodeFile("src/main.h", "");

        assertEquals("cpp", file.getLanguage());
    }

    @Test
    void detectLanguage_ruby() {
        CodeFile file = new CodeFile("src/app.rb", "");

        assertEquals("ruby", file.getLanguage());
    }

    @Test
    void detectLanguage_csharp() {
        CodeFile file = new CodeFile("src/Program.cs", "");

        assertEquals("csharp", file.getLanguage());
    }

    @Test
    void detectLanguage_php() {
        CodeFile file = new CodeFile("src/index.php", "");

        assertEquals("php", file.getLanguage());
    }

    @Test
    void detectLanguage_sql() {
        CodeFile file = new CodeFile("src/query.sql", "");

        assertEquals("sql", file.getLanguage());
    }

    @Test
    void detectLanguage_html() {
        CodeFile file = new CodeFile("src/index.html", "");

        assertEquals("html", file.getLanguage());
    }

    @Test
    void detectLanguage_htm() {
        CodeFile file = new CodeFile("src/index.htm", "");

        assertEquals("html", file.getLanguage());
    }

    @Test
    void detectLanguage_css() {
        CodeFile file = new CodeFile("src/style.css", "");

        assertEquals("css", file.getLanguage());
    }

    @Test
    void detectLanguage_xml() {
        CodeFile file = new CodeFile("src/config.xml", "");

        assertEquals("xml", file.getLanguage());
    }

    @Test
    void detectLanguage_json() {
        CodeFile file = new CodeFile("src/data.json", "");

        assertEquals("json", file.getLanguage());
    }

    @Test
    void detectLanguage_yml() {
        CodeFile file = new CodeFile("src/config.yml", "");

        assertEquals("yaml", file.getLanguage());
    }

    @Test
    void detectLanguage_yaml() {
        CodeFile file = new CodeFile("src/config.yaml", "");

        assertEquals("yaml", file.getLanguage());
    }

    @Test
    void detectLanguage_markdown() {
        CodeFile file = new CodeFile("README.md", "");

        assertEquals("markdown", file.getLanguage());
    }

    @Test
    void detectLanguage_unknown_extension() {
        CodeFile file = new CodeFile("src/data.bin", "");

        assertEquals("unknown", file.getLanguage());
    }

    @Test
    void detectLanguage_null_path() {
        CodeFile file = new CodeFile(null, "content");

        assertEquals("unknown", file.getLanguage());
        assertNull(file.getPath());
    }

    @Test
    void detectLanguage_no_extension() {
        CodeFile file = new CodeFile("README", "");

        assertEquals("unknown", file.getLanguage());
    }

    @Test
    void detectLanguage_case_insensitive() {
        CodeFile file = new CodeFile("src/Main.JAVA", "");

        assertEquals("java", file.getLanguage());
    }

    @Test
    void toString_containsPathAndLanguage() {
        CodeFile file = new CodeFile("src/Main.java", "content");

        String str = file.toString();
        assertTrue(str.contains("Main.java"));
        assertTrue(str.contains("java"));
    }
}
