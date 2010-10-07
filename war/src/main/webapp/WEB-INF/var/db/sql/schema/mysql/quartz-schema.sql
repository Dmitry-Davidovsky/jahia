#
# In your Quartz properties file, you'll need to set 
# org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#
#
# By: Ron Cordell - roncordell
#  I didn't see this anywhere, so I thought I'd post it here. This is the script from Quartz to create the tables in a MySQL database, modified to use INNODB instead of MYISAM.

DROP TABLE IF EXISTS JAHIA_QRTZ_JOB_LISTENERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_TRIGGER_LISTENERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_FIRED_TRIGGERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_PAUSED_TRIGGER_GRPS;
DROP TABLE IF EXISTS JAHIA_QRTZ_SCHEDULER_STATE;
DROP TABLE IF EXISTS JAHIA_QRTZ_LOCKS;
DROP TABLE IF EXISTS JAHIA_QRTZ_SIMPLE_TRIGGERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_CRON_TRIGGERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_BLOB_TRIGGERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_TRIGGERS;
DROP TABLE IF EXISTS JAHIA_QRTZ_JOB_DETAILS;
DROP TABLE IF EXISTS JAHIA_QRTZ_CALENDARS;
CREATE TABLE JAHIA_QRTZ_JOB_DETAILS(
JOB_NAME VARCHAR(180) NOT NULL,
JOB_GROUP VARCHAR(180) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
JOB_CLASS_NAME VARCHAR(250) NOT NULL,
IS_DURABLE VARCHAR(1) NOT NULL,
IS_VOLATILE VARCHAR(1) NOT NULL,
IS_STATEFUL VARCHAR(1) NOT NULL,
REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (JOB_NAME,JOB_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_JOB_LISTENERS (
JOB_NAME VARCHAR(180) NOT NULL,
JOB_GROUP VARCHAR(180) NOT NULL,
JOB_LISTENER VARCHAR(180) NOT NULL,
PRIMARY KEY (JOB_NAME,JOB_GROUP,JOB_LISTENER),
INDEX (JOB_NAME, JOB_GROUP),
FOREIGN KEY (JOB_NAME,JOB_GROUP)
REFERENCES JAHIA_QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_TRIGGERS (
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
JOB_NAME VARCHAR(180) NOT NULL,
JOB_GROUP VARCHAR(180) NOT NULL,
IS_VOLATILE VARCHAR(1) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
NEXT_FIRE_TIME BIGINT(13) NULL,
PREV_FIRE_TIME BIGINT(13) NULL,
PRIORITY INTEGER NULL,
TRIGGER_STATE VARCHAR(16) NOT NULL,
TRIGGER_TYPE VARCHAR(8) NOT NULL,
START_TIME BIGINT(13) NOT NULL,
END_TIME BIGINT(13) NULL,
CALENDAR_NAME VARCHAR(180) NULL,
MISFIRE_INSTR SMALLINT(2) NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
INDEX (JOB_NAME, JOB_GROUP),
FOREIGN KEY (JOB_NAME,JOB_GROUP)
REFERENCES JAHIA_QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_SIMPLE_TRIGGERS (
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
REPEAT_COUNT BIGINT(7) NOT NULL,
REPEAT_INTERVAL BIGINT(12) NOT NULL,
TIMES_TRIGGERED BIGINT(10) NOT NULL,
PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
INDEX (TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_CRON_TRIGGERS (
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
CRON_EXPRESSION VARCHAR(120) NOT NULL,
TIME_ZONE_ID VARCHAR(80),
PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
INDEX (TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_BLOB_TRIGGERS (
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
BLOB_DATA BLOB NULL,
PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
INDEX (TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_TRIGGER_LISTENERS (
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
TRIGGER_LISTENER VARCHAR(180) NOT NULL,
PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_LISTENER),
INDEX (TRIGGER_NAME, TRIGGER_GROUP),
FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP)
REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_CALENDARS (
CALENDAR_NAME VARCHAR(180) NOT NULL,
CALENDAR BLOB NOT NULL,
PRIMARY KEY (CALENDAR_NAME))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_PAUSED_TRIGGER_GRPS (
TRIGGER_GROUP VARCHAR(180) NOT NULL,
PRIMARY KEY (TRIGGER_GROUP))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_FIRED_TRIGGERS (
ENTRY_ID VARCHAR(95) NOT NULL,
TRIGGER_NAME VARCHAR(180) NOT NULL,
TRIGGER_GROUP VARCHAR(180) NOT NULL,
IS_VOLATILE VARCHAR(1) NOT NULL,
INSTANCE_NAME VARCHAR(180) NOT NULL,
FIRED_TIME BIGINT(13) NOT NULL,
PRIORITY INTEGER NOT NULL,
STATE VARCHAR(16) NOT NULL,
JOB_NAME VARCHAR(180) NULL,
JOB_GROUP VARCHAR(180) NULL,
IS_STATEFUL VARCHAR(1) NULL,
REQUESTS_RECOVERY VARCHAR(1) NULL,
PRIMARY KEY (ENTRY_ID))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_SCHEDULER_STATE (
INSTANCE_NAME VARCHAR(180) NOT NULL,
LAST_CHECKIN_TIME BIGINT(13) NOT NULL,
CHECKIN_INTERVAL BIGINT(13) NOT NULL,
PRIMARY KEY (INSTANCE_NAME))
TYPE=InnoDB;

CREATE TABLE JAHIA_QRTZ_LOCKS (
LOCK_NAME VARCHAR(40) NOT NULL,
PRIMARY KEY (LOCK_NAME))
TYPE=InnoDB;

INSERT INTO JAHIA_QRTZ_LOCKS values('TRIGGER_ACCESS');
INSERT INTO JAHIA_QRTZ_LOCKS values('JOB_ACCESS');
INSERT INTO JAHIA_QRTZ_LOCKS values('CALENDAR_ACCESS');
INSERT INTO JAHIA_QRTZ_LOCKS values('STATE_ACCESS');
INSERT INTO JAHIA_QRTZ_LOCKS values('MISFIRE_ACCESS');
commit; 
