(ns erdh.db.info.sqlite.reader
  (:require [erdh.db.sqlite-sql :as sql]
            [clojure.java.jdbc :as j]
            [clojure.string :as str]
            [instaparse.core :as insta]))

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
  [v]
  (for [r v
        :when (or (= (nth r 0) :COLUMN_LINE) (= (nth r 0) :LAST_COLUMN_LINE))]
    {:column_name (nth (nth r 1) 1)
     :data_type (nth (nth r 2) 1)
     }))

(defn table-columns-reader
  [db table-name]
  (let [parser (insta/parser (clojure.java.io/resource "sqlite_create.bnf"))]
    (->>
     (sql/table-columns db {:tablename table-name})
     first
     :sql
     parser
     get-columns)))

;; TEST ctrl-alt-v alt-e
; (table-columns-reader
;  {:dbtype "sqlite" :dbname "D:\\works\\github\\erdh-clj-doc\\test.sqlite3"}
;  "members")

; (table-names-reader {:dbtype "sqlite" :dbname "D:\\works\\github\\erdh-clj-doc\\test.sqlite3"})


(defn table-indexes-reader
  [db table-name]
  (->>
   (sql/table-indexes db {:tablename table-name})))

(defn foreign-keys-reader
  [db table-name]
  ; (->>
  ;  (sql/table-foreign-keys db {:tablename table-name}))
  ;; SQLiteはクエリーで外部キー制約を取得できない。
  nil)
