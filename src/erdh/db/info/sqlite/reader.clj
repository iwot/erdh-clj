(ns erdh.db.info.sqlite.reader
  (:require [erdh.db.sqlite-sql :as sql]
            [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [instaparse.core :as insta]))

(defn- debug-output
  [data]
  (println data)
  data)

(defn db-name-reader
  [db]
  (if-let [file (clojure.java.io/file (:dbname db))]
    (.getName file)
    "SQLite DB"))

(defn table-names-reader
  [db]
  (->>
   (sql/tables db)
   (map #(first (vals %)))))

(defn get-columns
  [rows]
  (for [r rows
        :when (= (nth r 0) :CL)]
    {:column_name (nth (nth r 1) 1)
     :data_type (nth (nth r 2) 1)}))

(defn table-columns-reader
  [db table-name]
  (let [parser (insta/parser (clojure.java.io/resource "sqlite_create.bnf"))]
    (->>
     (sql/table-columns db {:tablename table-name})
     first
     :sql
     parser
    ;  debug-output
     get-columns)))

(defn- table-columns-reader-test
  [create]
  (let [parser (insta/parser (clojure.java.io/resource "sqlite_create.bnf"))]
    (parser create)))

; (table-columns-reader-test {:dbtype "sqlite" :dbname "D:\\works\\github\\erdh-clj-doc\\test.sqlite3"} "items")

;; TEST ctrl-alt-v alt-e
; (table-columns-reader
;  {:dbtype "sqlite" :dbname "D:\\works\\github\\erdh-clj-doc\\test.sqlite3"}
;  "members")

; (table-names-reader {:dbtype "sqlite" :dbname "D:\\works\\github\\erdh-clj-doc\\test.sqlite3"})


(defn table-indexes-reader
  [db table-name]
  (->>
   (sql/table-indexes db {:tablename table-name})))

(defn- get-foreign-keys
  [rows]
  (for [r rows
        :when (= (nth r 0) :FK)
        n (range 1 (count (nth r 1)))]
      {:constraint_name "idx"
       :column_name (nth (nth r 1) n)
       :ordinal_position 0
       :position_in_unique_constraint 0
       :referenced_table_name (nth (nth r 2) 1)
       :referenced_column_name (nth (nth r 3) n)}
    )
    )

(defn foreign-keys-reader
  [db table-name]
  (let [parser (insta/parser (clojure.java.io/resource "sqlite_create.bnf"))]
    (->>
     (sql/table-columns db {:tablename table-name})
     first
     :sql
     parser
     get-foreign-keys)))
