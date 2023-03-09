create table users (
    id int primary key generated always as identity,
    username varchar(50) not null unique,
    password varchar(255) not null
);

create table todo (
    id int primary key generated always as identity,
    title varchar(50) not null,
    content varchar(60) not null,
    parent_id int not null,
    foreign key (parent_id) references users(id)
);