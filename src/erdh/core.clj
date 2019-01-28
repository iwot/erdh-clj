(ns erdh.core
  (:require [erdh.db.mysql-info :as mysql-info]
            [erdh.db.psql-info :as psql-info]
            [erdh.db.info.sqlite.info :as sqlite-info]
            [erdh.db.info.yaml.info :as yaml-info]
            [erdh.db.db-connection :as connection]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [yaml.core :as yaml]
            [erdh.db.yaml-reader :as yaml-reader]
            [erdh.erd.plantuml :as puml])
  (:gen-class))

(def cli-options
  ;; 引数が必要なオプション
  [["-s" "--source SOURCE-TYPE" "Source type"
    :validate [#(some #{%} '("mysql" "postgres" "sqlite" "yaml")) "Must be in [mysql postgres sqlite yaml]"]]
   ["-f" "--source-from SOURCE-FILE-PATH" "Source file path"
    :validate [#(.exists (io/as-file %)) "file not found"]]
   ["-i" "--intermediate INTERMIDIATE-TYPE" "Intermidiate data type"
    :default "yaml"
    :validate [#(some #{%} '("yaml")) "Must be in [yaml]"]]
   ["-o" "--intermediate-to INTERMIDIATE-SAVE-TO" "Intermidiate save path"]
   ["-t" "--type ERD-DATA-TYPE" "ERD data type"
    :default "puml"
    :validate [#(some #{%} '("puml")) "Must be in [puml]"]]
   ["-e" "--ex EXTRA-TABLE-INFO-FILE-PATH" "ex-table-info file path"
    :validate [#(.exists (io/as-file %)) "file not found"]]
   ;; デフォルトがnilのbooleanオプション
   ["-h" "--help"]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn get-db-data-from-source
  "sourceからDBのデータを取得する"
  [options]
  (cond (= "mysql" (:source options)) (mysql-info/gen-db-mysql
                                       (connection/get-db-from-yaml (:source-from options)))
        (= "yaml" (:source options)) (yaml-info/gen-db-yaml (:source-from options))
        (= "postgres" (:source options)) (psql-info/gen-db-postgres
                                          (connection/get-db-from-yaml (:source-from options)))
        (= "sqlite" (:source options)) (sqlite-info/gen-db-sqlite
                                          (connection/get-db-from-yaml (:source-from options)))
        :else {}))

(defn save-to-file
  [path data print-message]
  (if (count print-message) (println print-message))
  (spit path data))

(defn save-intermediate-if-opt-in
  "intermediateが指定されており、
   intermediate-toで保存先が指定されていれば保存する。
   そうでなければprintする。"
  [db-data options]
  (cond (= "yaml" (:intermediate options)) (if (not (empty? (:intermediate-to options)))
                                             (save-to-file (:intermediate-to options) (yaml/generate-string db-data) (str "saving intermediate data to " (:intermediate-to options)))
                                             (println "printing intermediate data" \newline "```yaml" \newline (yaml/generate-string db-data) \newline "```"))
        :else nil))

(defn get-extra-info
  [options]
  (if-let [ex-info-path (:ex options)]
    (yaml-reader/read-ex-table-info ex-info-path)
    nil))

(defn save-erd-if-opt-in
  [db-data output-path]
  (let [output (string/join "\n" (puml/convert-for-group db-data))]
    (if (count output-path)
      (save-to-file output-path output (str "saving plantuml data to " output-path))
      (println "printing plantuml data" \newline "```puml" \newline output \newline "```")))
  )

(defn merge-ex-table-info-to-db
  [options db-data ex-info]
  (cond (= "mysql" (:source options)) (mysql-info/merge-ex-table-info-to-db db-data ex-info)
        (= "yaml" (:source options)) (yaml-info/merge-ex-table-info-to-db db-data ex-info)
        (= "postgres" (:source options)) (psql-info/merge-ex-table-info-to-db db-data ex-info)
        (= "sqlite" (:source options)) (sqlite-info/merge-ex-table-info-to-db db-data ex-info)
        :else {}))

(defn main-proc
  [output-path options]
  (let [db-data (get-db-data-from-source options)
        ex-info (get-extra-info options)
        db-data-wtih-ex (merge-ex-table-info-to-db options db-data ex-info)]
    (save-intermediate-if-opt-in db-data-wtih-ex options)
    (save-erd-if-opt-in db-data-wtih-ex output-path)))

(defn -main
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      errors (println (error-msg errors))
      :else (main-proc (if (empty? arguments) nil (nth arguments 0)) options))))
