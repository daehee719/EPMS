@echo off
setlocal ENABLEDELAYEDEXPANSION

set DB_CMD=%DB_CMD%
if "%DB_CMD%"=="" (
  where mariadb >nul 2>nul && set DB_CMD=mariadb
  if "%DB_CMD%"=="" (
    where mysql >nul 2>nul && set DB_CMD=mysql
  )
)
if "%DB_CMD%"=="" (
  echo ERROR: mariadb/mysql client not found in PATH.
  exit /b 1
)

set DB_HOST=%DB_HOST%
if "%DB_HOST%"=="" set DB_HOST=127.0.0.1
set DB_PORT=%DB_PORT%
if "%DB_PORT%"=="" set DB_PORT=3306
set DB_ROOT_USER=%DB_ROOT_USER%
if "%DB_ROOT_USER%"=="" set DB_ROOT_USER=root

set EPMS_DEV_DB=%EPMS_DEV_DB%
if "%EPMS_DEV_DB%"=="" set EPMS_DEV_DB=epms_dev
set EPMS_PROD_DB=%EPMS_PROD_DB%
if "%EPMS_PROD_DB%"=="" set EPMS_PROD_DB=epms_prod
set EPMS_DEV_USER=%EPMS_DEV_USER%
if "%EPMS_DEV_USER%"=="" set EPMS_DEV_USER=epms_dev_user
set EPMS_PROD_USER=%EPMS_PROD_USER%
if "%EPMS_PROD_USER%"=="" set EPMS_PROD_USER=epms_prod_user

REM NOTE: Use Maven profiles (resources-dev/resources-prod) for DB config.

if not defined DB_ROOT_PASS (
  set /p DB_ROOT_PASS=DB root password (leave empty for socket auth):
)
set /p EPMS_DEV_PASS=DEV user password (%EPMS_DEV_USER%) [default: epms_dev_pass1!]:
if "%EPMS_DEV_PASS%"=="" set EPMS_DEV_PASS=epms_dev_pass1!
set /p EPMS_PROD_PASS=PROD user password (%EPMS_PROD_USER%) [default: epms_prod_pass1!]:
if "%EPMS_PROD_PASS%"=="" set EPMS_PROD_PASS=epms_prod_pass1!

set PASS_OPT=
if not "%DB_ROOT_PASS%"=="" set PASS_OPT=-p%DB_ROOT_PASS%

%DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% -e "SELECT 1" >nul 2>nul
if errorlevel 1 (
  echo WARN: Unable to connect as %DB_ROOT_USER%. Check DB_ROOT_USER/DB_ROOT_PASS/DB_HOST/DB_PORT.
  echo WARN: Skipping DB setup.
  exit /b 0
)

echo ==> Creating databases and users...
%DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% ^
  -e "CREATE DATABASE IF NOT EXISTS %EPMS_DEV_DB% CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; ^
      CREATE DATABASE IF NOT EXISTS %EPMS_PROD_DB% CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci; ^
      CREATE USER IF NOT EXISTS '%EPMS_DEV_USER%'@'localhost' IDENTIFIED BY '%EPMS_DEV_PASS%'; ^
      CREATE USER IF NOT EXISTS '%EPMS_PROD_USER%'@'localhost' IDENTIFIED BY '%EPMS_PROD_PASS%'; ^
      GRANT ALL PRIVILEGES ON %EPMS_DEV_DB%.* TO '%EPMS_DEV_USER%'@'localhost'; ^
      GRANT ALL PRIVILEGES ON %EPMS_PROD_DB%.* TO '%EPMS_PROD_USER%'@'localhost'; ^
      FLUSH PRIVILEGES;"
if errorlevel 1 (
  echo WARN: Failed to create databases/users. You may lack privileges.
  echo WARN: Skipping DDL/DML apply.
  exit /b 0
)

set DDL_FILE=%~dp0..\..\script\ddl\maria\com_DDL_maria.sql
set DML_FILE=%~dp0..\..\script\dml\maria\com_DML_maria.sql

if not exist "%DDL_FILE%" (
  echo WARN: DDL file not found: %DDL_FILE%
  exit /b 0
)
if not exist "%DML_FILE%" (
  echo WARN: DML file not found: %DML_FILE%
  exit /b 0
)

set /p APPLY_DEV=Apply DDL+DML to DEV DB (%EPMS_DEV_DB%)? [Y/n]:
if "%APPLY_DEV%"=="" set APPLY_DEV=Y
if /I "%APPLY_DEV%"=="Y" (
  echo ==> Applying DDL to %EPMS_DEV_DB%...
  %DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% --force %EPMS_DEV_DB% < "%DDL_FILE%"
  echo ==> Applying DML to %EPMS_DEV_DB%...
  %DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% --force %EPMS_DEV_DB% < "%DML_FILE%"
)

set /p APPLY_PROD=Apply DDL+DML to PROD DB (%EPMS_PROD_DB%)? [Y/n]:
if "%APPLY_PROD%"=="" set APPLY_PROD=Y
if /I "%APPLY_PROD%"=="Y" (
  echo ==> Applying DDL to %EPMS_PROD_DB%...
  %DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% --force %EPMS_PROD_DB% < "%DDL_FILE%"
  echo ==> Applying DML to %EPMS_PROD_DB%...
  %DB_CMD% -h %DB_HOST% -P %DB_PORT% -u %DB_ROOT_USER% %PASS_OPT% --force %EPMS_PROD_DB% < "%DML_FILE%"
)

echo Done.
endlocal
