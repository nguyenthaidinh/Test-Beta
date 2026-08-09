SET NAMES utf8mb4;

-- Do than thanh uses option 233 with params 101-109 as a server-side marker.
-- The marker is hidden from client option lists so option 233 can stay Set Gohan for old gear.
-- Do not insert item_option_template ids above 250, because old clients can break display.
DELETE FROM `item_option_template` WHERE `id` IN (251, 252, 253, 254);
UPDATE `item_option_template` SET `NAME` = 'Set Gohan' WHERE `id` = 233;
