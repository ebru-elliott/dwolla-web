# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table USER_ACCOUNT (
  username                  varchar(255) not null,
  pin                       varchar(255),
  password                  varchar(255),
  token                     varchar(255),
  constraint pk_USER_ACCOUNT primary key (username))
;

create sequence USER_ACCOUNT_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists USER_ACCOUNT;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists USER_ACCOUNT_seq;

