SET NAMES utf8mb4;

-- Do than thanh now uses option 233 with params 101-109 in code.
-- Do not insert item_option_template ids above 250, because old clients can break display.
DELETE FROM `item_option_template` WHERE `id` IN (251, 252, 253, 254);
