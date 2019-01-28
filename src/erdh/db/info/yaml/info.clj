(ns erdh.db.info.yaml.info
  (:require [yaml.core :as yaml]))

(defn gen-db-yaml
  [source-from]
  (yaml/from-file source-from))

(defn get-table-ex-info
  [table-name ex-info]
  (let [result-list (for [inf ex-info]
                      (if (= (:table inf) table-name)
                        inf
                        nil))]
    (nth (remove nil? result-list) 0 nil)))

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
                       (merge-ex-table-info-to-db-inner-for-group (:group (get-table-ex-info (:table table) ex-info))))))})

