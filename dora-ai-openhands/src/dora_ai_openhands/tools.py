"""
Custom tools for DoraAI agent.

Extends OpenHands' built-in tools with project-specific capabilities
like code search and project structure exploration.
"""

import os
import re
from pathlib import Path
from typing import Optional


class CodeSearchTool:
    """
    Search file contents using regex patterns (grep-like functionality).
    
    Similar to Junie's grep_search tool, this searches for patterns
    across the codebase and returns structured results.
    """
    
    name = "code_search"
    description = "Search file contents using PCRE regex patterns. Returns matching lines with file paths and line numbers."
    
    @staticmethod
    def search(
        pattern: str,
        path: str = ".",
        file_extensions: Optional[list[str]] = None,
        case_sensitive: bool = False,
        max_results: int = 100
    ) -> list[dict]:
        """
        Search for a regex pattern in files.
        
        Args:
            pattern: PCRE regex pattern to match
            path: Directory to search in
            file_extensions: Optional list of extensions to filter (e.g., ["*.java", "*.kt"])
            case_sensitive: Whether to match case sensitively
            max_results: Maximum number of results to return
            
        Returns:
            List of match results with file path, line number, and matching text
        """
        results = []
        search_path = Path(path)
        
        if not search_path.exists():
            return [{"error": f"Path does not exist: {path}"}]
        
        # Compile regex
        flags = 0 if case_sensitive else re.IGNORECASE
        try:
            regex = re.compile(pattern, flags)
        except re.error as e:
            return [{"error": f"Invalid regex pattern: {e}"}]
        
        # Determine file extensions filter
        ext_filter = set()
        if file_extensions:
            for ext in file_extensions:
                # Normalize extension format
                ext = ext.lstrip("*").lower()
                ext_filter.add(ext)
        
        # Walk directory and search
        for root, dirs, files in os.walk(search_path):
            # Skip hidden directories and common non-source directories
            dirs[:] = [
                d for d in dirs
                if not d.startswith(".") 
                and d not in ("node_modules", "target", "build", "out", ".gradle", ".idea")
            ]
            
            for filename in files:
                # Check extension filter
                if ext_filter:
                    file_ext = Path(filename).suffix.lower()
                    if file_ext not in ext_filter:
                        continue
                
                file_path = Path(root) / filename
                try:
                    # Read file with encoding detection
                    with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                        for line_num, line in enumerate(f, 1):
                            if regex.search(line):
                                results.append({
                                    "file": str(file_path),
                                    "line": line_num,
                                    "text": line.rstrip()
                                })
                                if len(results) >= max_results:
                                    return results
                except (PermissionError, IsADirectoryError):
                    continue
        
        return results


class ProjectStructureTool:
    """
    Explore and analyze project structure.
    
    Provides file listing, dependency analysis, and project metadata extraction.
    """
    
    name = "project_structure"
    description = "Explore project structure, list files, and analyze project metadata."
    
    @staticmethod
    def list_files(
        path: str = ".",
        pattern: Optional[str] = None,
        max_depth: int = 5
    ) -> list[dict]:
        """
        List files in a directory, optionally filtered by glob pattern.
        
        Args:
            path: Directory to list
            pattern: Optional glob pattern (e.g., "*.java", "**/*.py")
            max_depth: Maximum directory depth to traverse
            
        Returns:
            List of file info with path, type, and size
        """
        results = []
        base_path = Path(path).resolve()
        
        if not base_path.exists():
            return [{"error": f"Path does not exist: {path}"}]
        
        # If pattern doesn't contain ** or /, treat it as a filename-only match
        # so "*.java" matches at any depth
        filename_only = pattern and "**" not in pattern and "/" not in pattern and "\\" not in pattern
        
        for root, dirs, files in os.walk(base_path):
            current_depth = len(Path(root).parts) - len(base_path.parts)
            if current_depth >= max_depth:
                dirs.clear()
                continue
            
            # Sort dirs for consistent output
            dirs.sort()
            
            for filename in sorted(files):
                # Apply filename-only filter first (faster)
                if filename_only:
                    import fnmatch
                    if not fnmatch.fnmatch(filename, pattern):
                        continue
                
                file_path = Path(root) / filename
                rel_path = file_path.relative_to(base_path)
                
                # Apply full glob pattern filter if provided and not filename-only
                if pattern and not filename_only:
                    if not rel_path.match(pattern):
                        continue
                
                try:
                    size = file_path.stat().st_size
                    results.append({
                        "path": str(rel_path),
                        "type": "file",
                        "size": size,
                        "extension": file_path.suffix
                    })
                except (PermissionError, OSError):
                    continue
        
        return results
    
    @staticmethod
    def analyze_project(path: str = ".") -> dict:
        """
        Analyze a project and return metadata.
        
        Args:
            path: Project root directory
            
        Returns:
            Dictionary with project metadata (language, build system, dependencies, etc.)
        """
        project_path = Path(path)
        info = {
            "path": str(project_path.resolve()),
            "languages": {},
            "build_system": None,
            "package_manager": None,
            "total_files": 0,
            "total_lines": 0,
        }
        
        # Detect build system
        build_files = {
            "build.gradle": "Gradle (Groovy)",
            "build.gradle.kts": "Gradle (Kotlin)",
            "pom.xml": "Maven",
            "package.json": "npm",
            "pyproject.toml": "uv/pip",
            "Cargo.toml": "Cargo",
            "go.mod": "Go modules",
            "CMakeLists.txt": "CMake",
        }
        
        for bf, build_name in build_files.items():
            if (project_path / bf).exists():
                info["build_system"] = build_name
                break
        
        # Count files by extension
        extension_count = {}
        for root, dirs, files in os.walk(project_path):
            # Skip hidden and build directories
            dirs[:] = [
                d for d in dirs
                if not d.startswith(".") and d not in ("node_modules", "target", "build", "out")
            ]
            
            for filename in files:
                ext = Path(filename).suffix.lower() or "(no extension)"
                extension_count[ext] = extension_count.get(ext, 0) + 1
                info["total_files"] += 1
                
                # Count lines for source files
                if ext in (".java", ".kt", ".py", ".js", ".ts", ".go", ".rs", ".c", ".cpp", ".h"):
                    try:
                        file_path = Path(root) / filename
                        with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
                            info["total_lines"] += sum(1 for _ in f)
                    except (PermissionError, OSError):
                        continue
        
        info["languages"] = extension_count
        return info
