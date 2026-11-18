insert into tipo_ingrediente (dt_cadastro, nome, capacidade, unidade)
values
    (current_timestamp, 'SECO', 600, 'KG'),
    (current_timestamp, 'LIQUIDO', 500, 'LT'),
    (current_timestamp, 'REFRIGERADO', 400, 'KG');

insert into compartimento (dt_cadastro, nome)
values
    (current_timestamp, 'C1'),
    (current_timestamp, 'C2'),
    (current_timestamp, 'C3'),
    (current_timestamp, 'C4'),
    (current_timestamp, 'C5');
