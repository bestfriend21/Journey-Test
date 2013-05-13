/*
Navicat SQLite Data Transfer

Source Server         : journey
Source Server Version : 30623
Source Host           : localhost:0

Target Server Type    : SQLite
Target Server Version : 30623
File Encoding         : 65001

Date: 2013-05-13 23:52:28
*/

PRAGMA foreign_keys = OFF;

-- ----------------------------
-- Table structure for "main"."tbl_checkin"
-- ----------------------------
DROP TABLE "main"."tbl_checkin";
CREATE TABLE "tbl_checkin" (
"id"  INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
"lat"  INTEGER,
"lng"  INTEGER,
"time"  INTEGER,
"place"  TEXT
);

-- ----------------------------
-- Records of tbl_checkin
-- ----------------------------
