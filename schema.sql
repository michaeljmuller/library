create table users (
   id int not null auto_increment,
   email varchar(127) not null unique,
   password varchar(255) not null,
   first_name varchar(255) not null,
   last_name varchar(255) not null,
   enabled boolean not null default true,
   primary key(id),
   index(email)
) CHARACTER SET utf8, engine = innodb;

create table roles (
   id int not null auto_increment,
   user_id int not null,
   role varchar(31) not null,
   constraint fk_user foreign key (user_id) references users(id),
   primary key(id)
) CHARACTER SET utf8, engine = innodb;

create table assets (
    id int not null auto_increment,
    title varchar(255) not null,
    author varchar(255) not null,
    author2 varchar(255),
    author3 varchar(255),
    pub_year int,
    series varchar(255),
    series_sequence int,
    acq_date date,
    alt_title1 varchar(255),
    alt_title2 varchar(255),
    ebook_s3_object_key varchar(255) unique,
    audiobook_s3_object_key varchar(255) unique,
    primary key(id),
    index(ebook_s3_object_key),
    index(audiobook_s3_object_key),
) CHARACTER SET utf8, engine = innodb ;

create table cover_images (
    id int not null auto_increment,
    ebook_s3_object_key varchar(255) unique,
    mime_type varchar(255),
    bits mediumblob,
    primary key(id)
) CHARACTER SET utf8, engine = innodb ;

create table tags (
    id int not null auto_increment,
    asset_id into not null,
    tag varchar(255),
    FOREIGN KEY(asset_id) REFERENCES assets(id)
) CHARACTER SET utf8, engine = innodb ;


