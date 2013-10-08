# --- !Ups

insert into USER_ACCOUNT (id, username, password, is_admin) values (1, 'admin'  ,'$2a$10$E.BQyiuE71861n0UeZAV6u94rkVyeaN4f1lWjV6F9I.3Vra1oYPEG', TRUE);


# --- !Downs
delete from USER_ACCOUNT where username ='admin';
