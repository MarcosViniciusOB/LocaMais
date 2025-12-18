create database locamais
default character set utf8
default collate utf8_general_ci;

use locamais;

create table tab_veiculos(
	marca varchar(50) not null,
    modelo varchar(50) not null,
    ano numeric(4,0) not null,
    quantidade int not null,
    id int auto_increment,
    cor varchar(15),
    primary key(id)
);

create table tab_clientes(
	nome varchar(100) not null,
    cpf char(11),
    data_nasc date not null,
    id int unique not null auto_increment,
    primary key(cpf)
);

create table tab_reservas(
	id_cliente int,
    id_veiculo int,
    data_reserva date,
    data_entrega date,
    id int auto_increment,
    placa varchar(8),
    situacao enum('Entregue', 'Utilizando', 'Em atrazo', 'Entregue com atrazo'),
    foreign key(id_cliente) references tab_clientes(id),
    foreign key(id_veiculo) references tab_veiculos(id),
    primary key(id)
);

create table tab_usuarios(
	email varchar(150),
    senha varchar(20) not null,
    nome varchar(50) not null,
    id int unique not null auto_increment,
    tipo enum('Admin', 'Colaborador') not null,
    primary key(email)
);

#dados para testes
insert into tab_usuarios values ('admin', 'admin', 'ADM', 1, 'Admin');

insert into tab_veiculos values ('Fiat', 'Mobi', '2024', '10', default, 'Branco'), ('Fiat', 'Mobi', '2024', '10', default, 'Preto'), ('Wolksvagem', 'Polo', '2025', '12', default, 'Branco'), 
('Wolksvagem', 'Polo', '2025', '12', default, 'Preto'), ('Jeep', 'Compass', '2025', '7', default, 'Cinza'), ('Audi', 'A3', '2026', '5', default, 'Preto'), ('Fiat', 'Toro', '2026', '4', default, 'Cinza'),
('Hyundai', 'HB20', '2024', '12', default, 'Brancos'), ('Hyundai', 'HB20', '2024', '8', default, 'Pretos');

insert into tab_clientes values ('Marcos Vinicius', '50582693098', '2005-07-06', default), ('Iranilda', '37169471051', '1979-07-04', default), ('Marcos', '23636311026', '1978-05-18', default),
('Arlindo', '50009773002', '1999-02-16', default), ('Carol', '04839201005', '2001-09-27', default), ('Douglas', '17388922040', '1962-10-05', default);

insert into tab_reservas values (1, 5, '2025-11-28', '2025-12-05', default, 'KDD-0579', 'Utilizando'), (2, 4, '2025-11-29', '2025-12-04', default, 'KEO-3082', 'Utilizando');