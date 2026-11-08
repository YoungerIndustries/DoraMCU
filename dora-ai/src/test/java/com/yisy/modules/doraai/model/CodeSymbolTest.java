package com.yisy.modules.doraai.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodeSymbolTest {

    @Test
    void constructor_setsAllFields() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/MyClass.java", 1, 50, "public", null
        );

        assertEquals(CodeSymbol.SymbolType.CLASS, symbol.getType());
        assertEquals("MyClass", symbol.getName());
        assertEquals("public class MyClass", symbol.getSignature());
        assertEquals("src/MyClass.java", symbol.getFilePath());
        assertEquals(1, symbol.getStartLine());
        assertEquals(50, symbol.getEndLine());
        assertEquals("public", symbol.getAccessModifier());
        assertNull(symbol.getParentClass());
    }

    @Test
    void symbolType_enum_values() {
        assertSame(CodeSymbol.SymbolType.CLASS, CodeSymbol.SymbolType.CLASS);
        assertSame(CodeSymbol.SymbolType.INTERFACE, CodeSymbol.SymbolType.INTERFACE);
        assertSame(CodeSymbol.SymbolType.ENUM, CodeSymbol.SymbolType.ENUM);
        assertSame(CodeSymbol.SymbolType.METHOD, CodeSymbol.SymbolType.METHOD);
        assertSame(CodeSymbol.SymbolType.FIELD, CodeSymbol.SymbolType.FIELD);
        assertSame(CodeSymbol.SymbolType.CONSTRUCTOR, CodeSymbol.SymbolType.CONSTRUCTOR);
        assertSame(CodeSymbol.SymbolType.ANNOTATION, CodeSymbol.SymbolType.ANNOTATION);
        assertSame(CodeSymbol.SymbolType.PACKAGE, CodeSymbol.SymbolType.PACKAGE);
    }

    @Test
    void method_symbol_withParentClass() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.METHOD, "process", "public void process(",
                "src/Service.java", 10, 10, "public", "Service"
        );

        assertEquals(CodeSymbol.SymbolType.METHOD, symbol.getType());
        assertEquals("process", symbol.getName());
        assertEquals("Service", symbol.getParentClass());
    }

    @Test
    void field_symbol() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.FIELD, "name", "private String name",
                "src/User.java", 5, 5, "private", "User"
        );

        assertEquals(CodeSymbol.SymbolType.FIELD, symbol.getType());
        assertEquals("name", symbol.getName());
        assertEquals("private", symbol.getAccessModifier());
    }

    @Test
    void interface_symbol() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.INTERFACE, "Repository", "public interface Repository",
                "src/Repository.java", 1, 1, "public", null
        );

        assertEquals(CodeSymbol.SymbolType.INTERFACE, symbol.getType());
        assertEquals("Repository", symbol.getName());
    }

    @Test
    void enum_symbol() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.ENUM, "Status", "public enum Status",
                "src/Status.java", 1, 1, "public", null
        );

        assertEquals(CodeSymbol.SymbolType.ENUM, symbol.getType());
        assertEquals("Status", symbol.getName());
    }

    @Test
    void package_symbol() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.PACKAGE, "com.example.service", "com.example.service",
                "src/Service.java", 1, 1, null, null
        );

        assertEquals(CodeSymbol.SymbolType.PACKAGE, symbol.getType());
        assertEquals("com.example.service", symbol.getName());
        assertNull(symbol.getAccessModifier());
    }

    @Test
    void null_signature_allowed() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "Test", null,
                "src/Test.java", 1, 1, null, null
        );

        assertNull(symbol.getSignature());
    }

    @Test
    void zero_line_numbers() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.METHOD, "test", "void test(",
                "src/Test.java", 0, 0, null, null
        );

        assertEquals(0, symbol.getStartLine());
        assertEquals(0, symbol.getEndLine());
    }

    @Test
    void toString_containsKeyInfo() {
        CodeSymbol symbol = new CodeSymbol(
                CodeSymbol.SymbolType.CLASS, "MyClass", "public class MyClass",
                "src/MyClass.java", 1, 50, "public", null
        );

        String str = symbol.toString();
        assertTrue(str.contains("CLASS"));
        assertTrue(str.contains("MyClass"));
        assertTrue(str.contains("src/MyClass.java"));
        assertTrue(str.contains("1-50"));
    }

    @Test
    void symbolType_name_matches() {
        assertEquals("CLASS", CodeSymbol.SymbolType.CLASS.name());
        assertEquals("METHOD", CodeSymbol.SymbolType.METHOD.name());
        assertEquals("FIELD", CodeSymbol.SymbolType.FIELD.name());
    }
}
