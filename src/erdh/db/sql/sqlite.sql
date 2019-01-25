-- :name tables
-- :command :query
SELECT tbl_name FROM sqlite_master WHERE type = "table";

-- :name table-columns
-- :command :query
SELECT sql FROM sqlite_master WHERE type = "table" AND tbl_name = :tablename;

-- :name table-indexes
-- :command :query
SELECT type, name, tbl_name
  FROM sqlite_master
 WHERE type = "index"
   AND tbl_name = :tablename;

-- :name table-foreign-keys
-- :command :query
PRAGMA foreign_key_list(:tablename);


