CREATE SCHEMA IF NOT EXISTS app_schema;

create table if not exists category
(
    id    serial
        primary key,
    title varchar(255)
);

create table if not exists book
(
    id          serial
        primary key,
    author      varchar(255),
    title       varchar(255),
    category_id integer
        constraint fkam9riv8y6rjwkua1gapdfew4j
            references category
);

insert into category(id, title) values(3, 'Category1');
insert into category(id, title) values(4, 'Category2');

insert into book(id, author, title, category_id) values(3, 'Author1', 'TitleBook1', 3);
insert into book(id, author, title, category_id) values(4, 'Author1', 'TitleBook2', 3);
insert into book(id, author, title, category_id) values(5, 'Author2', 'TitleBook3', 4);