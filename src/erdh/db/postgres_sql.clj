(ns erdh.db.postgres-sql
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "erdh/db/sql/postgres.sql")
