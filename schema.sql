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

create table books (
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
    epub_object_key varchar(255) unique,
    mobi_object_key varchar(255) unique,
    audiobook_object_key varchar(255) unique,
    asin varchar(64),
    primary key(id),
    index(ebook_s3_object_key),
    index(audiobook_s3_object_key),
) CHARACTER SET utf8, engine = innodb ;

create table cover_images (
    id int not null auto_increment,
    book_id int not null,
    mime_type varchar(255),
    bits mediumblob,
    FOREIGN KEY(book_id) REFERENCES books(id),
    primary key(id)
) CHARACTER SET utf8, engine = innodb ;

create table tags (
    id int not null auto_increment,
    book_id int not null,
    tag varchar(255),
    FOREIGN KEY(book_id) REFERENCES books(id),
    primary key(id)
) CHARACTER SET utf8, engine = innodb ;

create table password_reset_tokens (
    id int not null auto_increment,
    user_id int not null,
    token varchar(64) not null,
    creation_time datetime not null,
    foreign key(user_id) references users(id),
    primary key(id)
) CHARACTER SET utf8, engine = innodb;


