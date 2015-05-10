CREATE TABLE IF NOT EXISTS sky_Group(
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  owner VARCHAR(50) NOT NULL,
  subject VARCHAR(100) NULL,
  category INT DEFAULT 0 ,
  logo VARCHAR(200),
  description TEXT,
  openness TINYINT NOT NULL,
  createTime DATETIME NOT NULL,
  numberOfMembers INT NOT NULL DEFAULT 0,
  status VARCHAR(2) NOT NULL DEFAULT "0"
  ) DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS sky_GroupMembers(
  id INT PRIMARY KEY AUTO_INCREMENT,
  groupId INT NOT NULL ,
  userName VARCHAR(50) NOT NULL,
  nickName VARCHAR(50) NOT NULL,
  joinTime DATETIME NOT NULL,
  UNIQUE uidx_group_and_user(groupId, userName)
) DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS sky_GroupHistoryMessages (
  id INT PRIMARY KEY AUTO_INCREMENT,
  groupId INT NOT NULL,
  sender VARCHAR (100) NOT NULL ,
  sendTime DATETIME NOT NULL ,
  body TEXT NOT NULL ,
  inputTime DATETIME NOT NULL,
  INDEX idx_groupId(groupId),
  INDEX idx_sendTime(sendTime)
) DEFAULT CHARSET=utf8;
