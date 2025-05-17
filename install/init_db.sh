#!/bin/bash

set -e

# CREATE DATABASE ADMIN IF ONT EXISTS
psql -U postgres -c "DO \$\$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = 'pawa_admin') THEN
      CREATE USER pawa_admin WITH PASSWORD '123';
   END IF;
END
\$\$;"

# CREATE DATABASE IF NOT EXISTS
psql -U postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'pawa_be_db'" | grep -q 1 || \
psql -U postgres -c "CREATE DATABASE pawa_be_db OWNER pawa_admin;"

# GRANT PRIVILEGE
psql -U postgres -d pawa_be_db -c "GRANT ALL PRIVILEGES ON DATABASE pawa_be_db TO pawa_admin;"
psql -U postgres -d pawa_be_db -c "GRANT ALL ON SCHEMA public TO pawa_admin;"

# CREATE TABLE IF NOT EXISTs
psql -U pawa_admin -d pawa_be_db -c "
CREATE TABLE IF NOT EXISTS invoice (
    total_price NUMERIC(38,2) NOT NULL,
    purchased_at TIMESTAMP(6),
    invoiceid UUID NOT NULL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    passenger_id VARCHAR(255),
    stripe_id VARCHAR(255)
);
"

psql -U pawa_admin -d pawa_be_db -c "
CREATE TABLE IF NOT EXISTS invoice_item (
    duration INTEGER NOT NULL,
    price NUMERIC(38,2) NOT NULL,
    activated_at TIMESTAMP(6),
    expired_at TIMESTAMP(6),
    purchased_at TIMESTAMP(6),
    invoice_id UUID NOT NULL,
    invoice_itemid UUID NOT NULL PRIMARY KEY,
    end_station VARCHAR(255),
    line_name VARCHAR(255) NOT NULL,
    lineid VARCHAR(255) NOT NULL,
    start_station VARCHAR(255),
    ticket_type VARCHAR(255) NOT NULL,
    status VARCHAR(255) CHECK (status IN ('ACTIVE', 'INACTIVE', 'EXPIRED'))
);
"

psql -U pawa_admin -d pawa_be_db -c "
CREATE TABLE IF NOT EXISTS passenger (
    has_disability BOOLEAN NOT NULL,
    is_revolutionary BOOLEAN NOT NULL,
    passenger_date_of_birth DATE NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6),
    googleid VARCHAR(255),
    nationalid VARCHAR(255),
    passenger_address VARCHAR(255) NOT NULL,
    passenger_first_name VARCHAR(255) NOT NULL,
    passenger_last_name VARCHAR(255) NOT NULL,
    passenger_middle_name VARCHAR(255) NOT NULL,
    passenger_phone VARCHAR(255) NOT NULL,
    passengerid VARCHAR(255) NOT NULL PRIMARY KEY,
    studentid VARCHAR(255)
);
"

psql -U pawa_admin -d pawa_be_db -c "
CREATE TABLE IF NOT EXISTS user_auth (
    email VARCHAR(255) UNIQUE,
    google_id VARCHAR(255),
    password VARCHAR(255),
    role VARCHAR(255) CHECK (role IN ('PASSENGER','ADMIN','GUEST','OPERATOR','TICKET_AGENT')),
    user_id VARCHAR(255) NOT NULL PRIMARY KEY
);
"

# INSERT SAMPLE DATA
psql -U pawa_admin -d pawa_be_db <<EOF

INSERT INTO user_auth (user_id, email, password, role, google_id)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'an.nguyenvan@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000002', 'binh.lethi@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000003', 'cuong.phamminh@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000004', 'linh.tranngoc@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000005', 'hieu.doquy@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000006', 'giang.hoangthi@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000007', 'khanh.nguyenvan@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000008', 'linh.vothuy@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000009', 'minh.buinhat@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL),
    ('00000000-0000-0000-0000-000000000010', 'ngoc.danganh@gmail.com', '$2a$10$rH4iP4KPMO2OLnelKKnfD.4szQ.ds7M1ie4yKPQfbCrHqjyssecV', 'PASSENGER', NULL)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO passenger (
    passengerid, passenger_first_name, passenger_middle_name, passenger_last_name,
    passenger_phone, passenger_address, passenger_date_of_birth,
    nationalid, studentid, has_disability, is_revolutionary,
    created_at, updated_at
) VALUES
    ('00000000-0000-0000-0000-000000000001', 'An', 'Văn', 'Nguyễn', '0911111111', '123 Hoang Dieu', '1950-01-01', '001122334455', NULL, FALSE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000002', 'Bình', 'Thị', 'Lê', '0922222222', '456 Tran Hung Dao', '1952-05-12', '223344556677', NULL, FALSE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000003', 'Cường', 'Minh', 'Phạm', '0933333333', '789 Le Loi', '2002-03-03', '334455667788', 'ST12345678', FALSE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000004', 'Linh', 'Ngọc', 'Trần', '0944444444', '135 Nguyen Trai', '2003-04-04', '445566778899', 'ST23456789', FALSE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000005', 'Hiếu', 'Quý', 'Đỗ', '0955555555', '246 Bach Dang', '1975-06-06', '556677889900', NULL, FALSE, TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000006', 'Giang', 'Thị', 'Hoàng', '0966666666', '357 Hai Ba Trung', '1980-07-07', '667788990011', NULL, FALSE, TRUE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000007', 'Khánh', 'Văn', 'Nguyễn', '0977777777', '468 Phan Boi Chau', '1990-08-08', '778899001122', NULL, TRUE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000008', 'Linh', 'Thuy', 'Võ', '0988888888', '579 Ly Thuong Kiet', '1985-09-09', '889900112233', NULL, TRUE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000009', 'Minh', 'Nhật', 'Bùi', '0999999999', '680 Dien Bien Phu', '1995-10-10', '990011223344', NULL, FALSE, FALSE, NOW(), NOW()),
    ('00000000-0000-0000-0000-000000000010', 'Ngọc', 'Anh', 'Đặng', '0900000000', '791 Le Thanh Ton', '2000-11-11', '101112131415', NULL, FALSE, FALSE, NOW(), NOW())
ON CONFLICT (passengerid) DO NOTHING;

EOF

FIRST_METRO_LINE_ID=$(curl -s 'http://localhost:8081/api/metro_line' | jq -r '.[0].metroLine.id')
FIRST_METRO_LINE_FIRST_STATION_ID=$(curl -s 'http://localhost:8081/api/metro_line' | jq -r '.[0].firstStation.id')
FIRST_METRO_LINE_LAST_STATION_ID=$(curl -s 'http://localhost:8081/api/metro_line' | jq -r '.[0].lastStation.id')

# ADD INVOICE ITEMS / TICKETS
psql -U pawa_admin -d pawa_be_db <<SQL
\set iid 11111111-1111-1111-1111-111111111111
\set line  '$FIRST_METRO_LINE_ID'
\set ssta  '$FIRST_METRO_LINE_FIRST_STATION_ID'
\set esta  '$FIRST_METRO_LINE_LAST_STATION_ID'

INSERT INTO invoice (invoiceid, passenger_id, email, total_price)
VALUES ('11111111-1111-1111-1111-111111111111',
        '00000000-0000-0000-0000-000000000005',
        'hieu.doquy@gmail.com',
        1480000)
ON CONFLICT (invoiceid) DO NOTHING;

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','FREE','EXPIRED',0,
   '2024-05-10 08:00','2024-05-11 08:00',
   :'line','Metro Line', :'ssta', :'esta', 1,'2024-05-10 07:00'),
  (gen_random_uuid(), :'iid','FREE','EXPIRED',0,
   '2024-05-20 08:00','2024-05-21 08:00',
   :'line','Metro Line', :'ssta', :'esta', 1,'2024-05-20 07:00');

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','DAILY','EXPIRED',40000,
   '2024-05-10 08:00','2024-05-11 08:00',
   :'line','Metro Line', :'ssta', :'esta', 1,'2024-05-10 07:00'),
  (gen_random_uuid(), :'iid','DAILY','EXPIRED',40000,
   '2024-05-20 08:00','2024-05-21 08:00',
   :'line','Metro Line', :'ssta', :'esta', 1,'2024-05-20 07:00');

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','ONE_WAY_4','INACTIVE',8000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 1,NOW()),
  (gen_random_uuid(), :'iid','ONE_WAY_8','INACTIVE',12000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 1,NOW());

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','MONTHLY_ADULT','INACTIVE',300000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 30,NOW()),
  (gen_random_uuid(), :'iid','MONTHLY_ADULT','INACTIVE',300000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 30,NOW());

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','THREE_DAY','INACTIVE',90000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 3,NOW()),
  (gen_random_uuid(), :'iid','THREE_DAY','INACTIVE',90000,
   NULL,NULL, :'line','Metro Line', :'ssta', :'esta', 3,NOW());

INSERT INTO invoice_item(invoice_itemid,invoice_id,ticket_type,status,
                         price,activated_at,expired_at,lineid,line_name,start_station,end_station,
                         duration,purchased_at)
VALUES
  (gen_random_uuid(), :'iid','MONTHLY_ADULT','ACTIVE',300000,
   NOW() - INTERVAL '5 days', NOW() + INTERVAL '25 days',
   :'line','Metro Line', :'ssta', :'esta', 30,NOW() - INTERVAL '5 days'),
  (gen_random_uuid(), :'iid','MONTHLY_ADULT','ACTIVE',300000,
   NOW() - INTERVAL '3 days', NOW() + INTERVAL '27 days',
   :'line','Metro Line', :'ssta', :'esta', 30,NOW() - INTERVAL '3 days');
SQL



