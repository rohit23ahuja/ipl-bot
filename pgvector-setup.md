Ensure C++ support in Visual Studio is installed and run x64 Native Tools Command Prompt for VS [version] as administrator. Then use nmake to build:
Open Start Menu and search for: x64 Native Tools Command Prompt for VS 2022
where nmake
set "PGROOT=C:\Program Files\PostgreSQL\16"
cd %TEMP%
git clone --branch v0.8.0 https://github.com/pgvector/pgvector.git
cd pgvector
nmake /F Makefile.win
nmake /F Makefile.win install

-- it seems gcp and aws have pg vector pre installed.
CREATE EXTENSION vector;
CREATE TABLE items (id bigserial PRIMARY KEY, embedding vector(3));

INSERT INTO items (embedding) VALUES ('[1,2,3]'), ('[4,5,6]');

SELECT * FROM items ORDER BY embedding <-> '[3,1,2]' LIMIT 5;
SELECT * FROM items ORDER BY embedding <#> '[3,1,2]' LIMIT 5;
-- Also supports inner product (<#>), cosine distance (<=>), and L1 distance (<+>)
-- https://github.com/pgvector/pgvector

CREATE EXTENSION IF NOT EXISTS hstore;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1536 is the default embedding dimension
CREATE TABLE IF NOT EXISTS vector_store (
id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
content text,
metadata json,
embedding vector(1536)  
);

CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

select * from vector_store;
drop database ai_learn2;
create database ai_learn2;

* create database then run application
* spring ai will initialize schema w