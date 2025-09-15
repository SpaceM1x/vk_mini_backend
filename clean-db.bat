@echo off
echo Cleaning database files...
if exist data\testdb.mv.db del data\testdb.mv.db
if exist data\testdb.trace.db del data\testdb.trace.db
echo Database files cleaned.
pause
