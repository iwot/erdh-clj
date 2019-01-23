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
SELECT A.CONSTRAINT_NAME
     , A.COLUMN_NAME
     , A.ORDINAL_POSITION
     , A.POSITION_IN_UNIQUE_CONSTRAINT
     , A.REFERENCED_TABLE_NAME
     , A.REFERENCED_COLUMN_NAME
  FROM information_schema.key_column_usage A
 WHERE A.constraint_schema = :schema
   AND A.table_name = :tablename
   AND A.CONSTRAINT_NAME <> 'PRIMARY'
   AND EXISTS(
     SELECT *
       FROM information_schema.TABLE_CONSTRAINTS AS B
      WHERE B.TABLE_SCHEMA = A.constraint_schema
        AND B.TABLE_NAME = A.table_name
        AND B.CONSTRAINT_TYPE = 'FOREIGN KEY'
   )
 ORDER BY CONSTRAINT_NAME;


