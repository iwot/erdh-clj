-- :name db-name
-- :command :query
-- :doc select db name
SELECT database() AS db_name;

-- :name tables
-- :command :query
SHOW TABLES;

-- :name table-columns
-- :command :query
SELECT COLUMN_NAME
     , COLUMN_TYPE
     , COLUMN_KEY
     , EXTRA
     , COLUMN_DEFAULT
     , IS_NULLABLE
  FROM information_schema.columns c
 WHERE c.table_schema = :tableschema
   AND c.table_name = :tablename
 ORDER BY ordinal_position;

-- :name table-indexes
-- :command :query
SELECT INDEX_NAME
     , COLUMN_NAME
     , SEQ_IN_INDEX
  FROM information_schema.statistics
 WHERE table_schema = :tableschema
   AND table_name = :tablename;

-- :name table-foreign-keys
-- :command :query
SELECT CONSTRAINT_NAME
     , COLUMN_NAME
     , ORDINAL_POSITION
     , POSITION_IN_UNIQUE_CONSTRAINT
     , REFERENCED_TABLE_NAME
     , REFERENCED_COLUMN_NAME
  FROM information_schema.key_column_usage
 WHERE constraint_schema = :schema
   AND table_name = :tablename
   AND CONSTRAINT_NAME <> 'PRIMARY'
 ORDER BY CONSTRAINT_NAME;


