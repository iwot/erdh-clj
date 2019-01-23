-- :name db-name
-- :command :query
-- :doc select db name
SELECT current_database() AS db_name;

-- :name tables
-- :command :query
SELECT relname as TABLE_NAME
  FROM pg_stat_user_tables;

-- :name table-columns
-- :command :query
SELECT column_name
     , data_type
     , character_maximum_length
     , numeric_precision
     , udt_name
     , column_default
     , is_nullable
  FROM information_schema.columns 
 WHERE table_catalog = :tableschema
   AND table_name = :tablename
ORDER BY ordinal_position;

-- :name table-indexes
-- :command :query
SELECT tablename
     , indexname
  FROM pg_indexes
 WHERE tablename = :tablename;

-- :name table-foreign-keys
-- :command :query
SELECT tc.table_schema, 
       tc.constraint_name, 
       tc.table_name, 
       kcu.column_name, 
       ccu.table_schema AS REFERENCED_COLUMN_SCHEMA,
       ccu.table_name AS REFERENCED_TABLE_NAME,
       ccu.column_name AS REFERENCED_COLUMN_NAME 
FROM information_schema.table_constraints AS tc 
     JOIN information_schema.key_column_usage AS kcu
       ON tc.constraint_name = kcu.constraint_name
      AND tc.table_schema = kcu.table_schema
     JOIN information_schema.constraint_column_usage AS ccu
       ON ccu.constraint_name = tc.constraint_name
      AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = :tablename;

