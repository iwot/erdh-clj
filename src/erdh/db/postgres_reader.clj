(ns erdh.db.postgres-reader
  (:require [erdh.db.postgres-sql :as sql]))

(require '[clojure.java.jdbc :as j])

(defn db-name-reader
  [db]
  (->>
   (sql/db-name db)
   first
   :db_name))

(defn table-names-reader
  [db]
  (->>
   (sql/tables db)
   (map #(first (vals %)))))

(defn table-columns-reader
  [db db-name table-name]
  (->>
   (sql/table-columns db {:tableschema db-name, :tablename table-name})))

(defn table-indexes-reader
  [db db-name table-name]
  (->>
   (sql/table-indexes db {:tableschema db-name, :tablename table-name})))

(defn foreign-keys-reader
  [db db-name table-name]
  (->>
   (sql/table-foreign-keys db {:schema db-name, :tablename table-name})))
