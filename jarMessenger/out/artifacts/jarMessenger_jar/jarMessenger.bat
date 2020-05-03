@echo off

echo Select your Java version
echo 1. Java 11 (Recommended)
echo 2. Java 8
set /p version="Version ( 1 / 2 ): "

if %version%==1 (
	start javaw -jar jarMessenger11.jar
)

if %version%==2 (
	start javaw -jar jarMessenger8.jar
)
