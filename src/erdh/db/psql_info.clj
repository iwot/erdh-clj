(ns erdh.db.psql-info
  (:require [erdh.db.postgres-reader :as reader]))

(declare get-ex-relations-groupby-referenced-table-name
         gen-postgres-tables gen-postgres-table-one
         gen-postgres-table-columns gen-postgres-table-indexes
         gen-postgres-table-foreign-keys)

(defn gen-db-postgres
  [db]
  (let [dbname (reader/db-name-reader db)]
    {:db_name dbname
     :tables (vec (gen-postgres-tables db dbname))}))

(defn gen-postgres-tables
  [db db-name]
  (if-let [tblnames (reader/table-names-reader db)]
    (map (fn
           [row]
           (gen-postgres-table-one db db-name row)) tblnames)
    []))

(defn gen-postgres-table-one
  [db db-name table-name]
  (let [columns (gen-postgres-table-columns db db-name table-name)
        indexes (gen-postgres-table-indexes db db-name table-name)
        foreign-keys (gen-postgres-table-foreign-keys db db-name table-name)]
    {:table table-name
     :group db-name
     :columns (vec columns)
     :indexes (vec indexes)
     :foreign-keys (vec foreign-keys)
     :ex-relations (get-ex-relations-groupby-referenced-table-name foreign-keys)}))

(defn gen-postgres-table-columns
  [db db-name table-name]
  (if-let [columns (reader/table-columns-reader db db-name table-name)]
    (map (fn
           [row]
           {:name (:column_name row)
            :type (:data_type row)
            :key ""
            :extra ""
            :default (:column_default row)
            :not-null (if (= (:is_nullable row) "NO") true false)}) columns)
    []))

(defn gen-postgres-table-indexes
  [db db-name table-name]
  (if-let [indexes (reader/table-indexes-reader db db-name table-name)]
    (map (fn
           [row]
           {:name (:indexname row)
            :column_name ""
            :seq_in_index ""}) indexes)
    []))

(defn gen-postgres-table-foreign-keys
  [db db-name table-name]
  (if-let [foreign-keys (reader/foreign-keys-reader db db-name table-name)]
    (map (fn
           [row]
           {:constraint_name (:constraint_name row)
            :column_name (:column_name row)
            :ordinal_position 0
            :position_in_unique_constraint 0
            :referenced_table_name (:referenced_table_name row)
            :referenced_column_name (:referenced_column_name row)}) foreign-keys)
    []))

(defn get-table-ex-info
  [table-name ex-info]
  (let [result-list (for [inf ex-info]
                      (if (= (:table inf) table-name)
                        inf
                        nil))]
    (nth (remove nil? result-list) 0 nil)))

(defn merge-ex-table-info-to-db-old
  [db-data ex-info]
  {
   :db_name (:db_name db-data)
   :tables (into []
                 (for [table (:tables db-data)]
                   (if-let [ex (:relations (get-table-ex-info (:table table) ex-info))]
                         (update-in table [:ex-relations] (fn [old] ex))
                         table)))
   })

(defn merge-ex-table-info-to-db-inner-for-relation
  [table ex-relations]
  (if (nil? ex-relations)
    table
    (update-in table [:ex-relations] (fn [old] ex-relations))))

(defn merge-ex-table-info-to-db-inner-for-group
  [table group]
  (if (nil? group)
    table
    (update-in table [:group] (fn [old] group))))

(defn merge-ex-table-info-to-db
  [db-data ex-info]
  {:db_name (:db_name db-data)
   :tables (into []
                 (for [table (:tables db-data)]
                   (-> table
                       (merge-ex-table-info-to-db-inner-for-relation (:relations (get-table-ex-info (:table table) ex-info)))
                       (merge-ex-table-info-to-db-inner-for-group (:group (get-table-ex-info (:table table) ex-info)))
                       )))})

(defn get-ex-relations-groupby-referenced-table-name
  [foreign-keys]
  (let [tablenames (distinct (for [f foreign-keys] (:referenced_table_name f)))]
    (into [] (for [tbl tablenames]
               {:referenced_table_name tbl
                :columns (into []
                               (for [fk foreign-keys
                                     :when (= (:referenced_table_name fk) tbl)]
                                 {:from (:column_name fk) :to (:referenced_column_name fk)}))
                :this_conn "one"
                :that_conn "one"}
               ))))
