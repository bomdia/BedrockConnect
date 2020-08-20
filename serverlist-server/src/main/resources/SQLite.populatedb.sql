DROP TABLE IF EXISTS servers;

CREATE TABLE "servers" (
	"serverAddress"	TEXT NOT NULL,
	"serverPort"	INT NOT NULL,
	"iconPath"		TEXT,
	PRIMARY KEY("serverAddress","serverPort")
);

DROP TABLE IF EXISTS globalServers;

CREATE TABLE "globalServers" (
	"serverAddress"	TEXT NOT NULL,
	"serverPort"	INT NOT NULL,
	"serverName"	TEXT NOT NULL,
	"iconPath"		TEXT,
	PRIMARY KEY("serverAddress","serverPort")
);

DROP TABLE IF EXISTS users;

CREATE TABLE "users" (
	"xuid"	NUMERIC NOT NULL,
	"userName"	TEXT NOT NULL,
	PRIMARY KEY("xuid")
);

DROP TABLE IF EXISTS usersServers;

CREATE TABLE "usersServers" (
	"xuid"			NUMERIC NOT NULL,
	"serverAddress"	TEXT NOT NULL,
	"serverPort"	INT NOT NULL,
	"serverName"	TEXT NOT NULL,
	FOREIGN KEY("serverAddress") REFERENCES "servers"("serverAddress"),
	FOREIGN KEY("serverPort") REFERENCES "servers"("serverPort"),
	FOREIGN KEY("xuid") REFERENCES "users"("xuid"),
	PRIMARY KEY("xuid","serverAddress","serverPort")
);

DROP VIEW IF EXISTS usersWithServers;

CREATE VIEW usersWithServers as 
	select 
		a.xuid,
		a.name as userName,
		b.serverAddress,
		b.serverPort,
		s.serverName,
		b.iconPath 
	from users a 
	inner join usersServers s on a.xuid = s.xuid 
	INNER join servers b on s.serverAddress = b.serverAddress 
			and s.serverPort = b.serverPort;


insert into globalServers values 
('54.39.75.136', 19132, 'The Hive',''),
('108.178.12.125', 19132,'Mineplex', ''),
('213.32.11.233', 19132,'CubeCraft Games', ''),
('63.143.40.66', 19132,'Lifeboat Network', ''),
('52.234.131.7', 19132,'Mineville City', ''),
('51.89.152.241', 19132,'Galaxite', '');