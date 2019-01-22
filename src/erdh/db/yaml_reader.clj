(ns erdh.db.yaml-reader
  (:require [yaml.core :as yaml]))

(defn read-ex-table-info
  [path]
  (yaml/from-file path))
