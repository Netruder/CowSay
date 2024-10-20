CREATE TABLE IF NOT EXISTS `cow_say`
(
    `uuid`      varchar(36) NOT NULL,
    `last_say`  varchar(256) NULL DEFAULT NULL,
    `count`     int(11) NOT NULL,
    PRIMARY KEY (`uuid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
