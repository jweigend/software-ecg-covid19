@echo off
setlocal enabledelayedexpansion

set "CURRENT_DIR=%~dp0"
set "CURRENT_DIR=%CURRENT_DIR:~0,-1%"

REM Use bundled JDK if available, otherwise use system JAVA_HOME or java in PATH
if exist "%CURRENT_DIR%\jdk\bin\java.exe" (
    set "JAVA_HOME=%CURRENT_DIR%\jdk"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        echo Using system JAVA_HOME: %JAVA_HOME%
    ) else (
        echo ERROR: JAVA_HOME is set but java.exe not found
        exit /b 1
    )
) else (
    where java >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        for /f "delims=" %%i in ('where java') do set "JAVA_CMD=%%i"
        for %%i in ("!JAVA_CMD!") do set "JAVA_HOME=%%~dpi.."
        echo Using java from PATH: !JAVA_HOME!
    ) else (
        echo ERROR: No Java installation found. Please install Java 17+ or set JAVA_HOME.
        exit /b 1
    )
)

set "EKG_CONFIG=%CURRENT_DIR%\configs"
set "JRE_HOME=%JAVA_HOME%"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM Build JavaFX module path from libs directory
set "JAVAFX_MODULES="
for %%f in ("%CURRENT_DIR%\libs\javafx-*.jar") do (
    if "!JAVAFX_MODULES!"=="" (
        set "JAVAFX_MODULES=%%f"
    ) else (
        set "JAVAFX_MODULES=!JAVAFX_MODULES!;%%f"
    )
)

"%JAVA_HOME%\bin\java.exe" ^
    --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
    --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED ^
    --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
    --add-opens java.base/java.lang=ALL-UNNAMED ^
    --module-path "%JAVAFX_MODULES%" ^
    --add-modules=javafx.controls,javafx.fxml,javafx.swing ^
    -Xmx8G ^
    -classpath "%CURRENT_DIR%\libs\*" ^
    de.qaware.ekg.awb.application.base.EkgCdiApplication

endlocal