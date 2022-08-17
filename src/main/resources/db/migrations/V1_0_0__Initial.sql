create table users (
    id int primary key,
    username varchar(50) not null,
    password varchar(60) not null
);

create table todo (
    id int primary key,
    title varchar(50) not null,
    content varchar(60) not null,
    parent_id int not null,
    foreign key (parent_id) references users(id)
);