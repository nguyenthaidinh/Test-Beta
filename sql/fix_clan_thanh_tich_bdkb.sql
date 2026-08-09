-- Fix clan BDKB achievement data that makes Manager.loadDatabase crash.
-- Safe to run multiple times.

UPDATE `clan`
SET `thanhTichBDKB` = '[0,0]'
WHERE `thanhTichBDKB` IS NULL
   OR TRIM(`thanhTichBDKB`) = ''
   OR LOWER(TRIM(`thanhTichBDKB`)) = 'null';
