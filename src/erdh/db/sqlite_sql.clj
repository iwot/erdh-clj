(ns erdh.db.sqlite-sql
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "erdh/db/sql/sqlite.sql")
