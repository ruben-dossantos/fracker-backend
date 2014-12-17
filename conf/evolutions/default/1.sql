# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `groups` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`password` VARCHAR(254) NOT NULL);
create unique index `name` on `groups` (`name`);
create table `users_groups` (`user` BIGINT NOT NULL,`group` BIGINT NOT NULL);
create unique index `user_group` on `users_groups` (`user`,`group`);
create table `users` (`id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,`username` VARCHAR(254) NOT NULL,`first_name` VARCHAR(254) NOT NULL,`last_name` VARCHAR(254) NOT NULL,`password` VARCHAR(254),`lat` VARCHAR(254),`lon` VARCHAR(254),`timestamp` BIGINT);
create unique index `username` on `users` (`username`);

# --- !Downs

drop table `users`;
drop table `users_groups`;
drop table `groups`;

