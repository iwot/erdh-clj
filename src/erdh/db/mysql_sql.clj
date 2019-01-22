(ns erdh.db.mysql-sql
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "erdh/db/sql/mysql.sql")
