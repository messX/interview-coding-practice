@rem Gradle startup script for Windows
@rem This file is intentionally minimal - download from https://github.com/gradle/gradle if needed
@echo off
SET DIRNAME=%~dp0
java -jar "%DIRNAME%\gradle\wrapper\gradle-wrapper.jar" %*
