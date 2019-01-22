(ns erdh.db.db-connection
  (:require [yaml.core :as yaml]))

(defn get-db-from-yaml
  [path]
  (if-let [db (yaml/from-file path)]
    db
    (throw (Exception. "db configuration not found"))))
