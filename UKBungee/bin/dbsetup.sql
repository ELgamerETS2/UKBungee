CREATE TABLE IF NOT EXISTS points_data
(
	uuid		CHAR(36)	NOT NULL,
	points		INT			NOT NULL,
	messages	INT			NOT NULL,
	add_points	INT			NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS player_data
(
	uuid			CHAR(36)	NOT NULL,
	name			VARCHAR(36)	NOT NULL,
	building_time	INT			NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS weekly_data
(
	uuid		CHAR(36)	NOT NULL,
	points		INT			NOT NULL,
	monday		INT			NOT NULL,
	tuesday		INT			NOT NULL,
	wednesday	INT			NOT NULL,
	thursday	INT			NOT NULL,
	friday		INT			NOT NULL,
	saturday	INT			NOT NULL,
	sunday		INT			NOT NULL,
	PRIMARY KEY (uuid)
);

CREATE TABLE IF NOT EXISTS data
(
	data		VARCHAR(36)	NOT NULL,
	value		VARCHAR(36)	NOT NULL,
	PRIMARY KEY (data)
);