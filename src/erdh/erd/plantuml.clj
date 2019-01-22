(ns erdh.erd.plantuml)

(def convert-map
  {
   :start "@startuml"
   :end "@enduml"
   :cardinality {
                 :center "--"
                 :this {:one "--"
                        :onlyone "||"
                        :zero-or-one "|o"
                        :many "}-"
                        :one-more "}|"
                        :zero-many "}o"}
                 :that {:one "--"
                        :onlyone "||"
                        :zero-or-one "o|"
                        :many "-{"
                        :one-more "|{"
                        :zero-many "o{"}
                 }
   :package-start "package \"%s \" as %s {"
   :package-end "}"
   :master-entity-start "  entity \"%s\" as %s <<M,MASTER_MARK_COLOR>> {"
   :master-entity-end "  }"
   :normal-entity-start "  entity \"%s\" as %s <<D,TRANSACTION_MARK_COLOR>> {"
   :normal-entity-end "  }"
   :primary-column "    + %s [PK]"
   :primary-column-end "    --"
   :foregin-key-column "    # %s"
   :normal-column "    %s"
   :column-omission "    ..."
   })

(defn convert-column-into-columns
  [result column]
  (if (= (:key column) "PRI")
    (-> result
        (conj (format (:primary-column convert-map) (:name column)))
        (conj (:primary-column-end convert-map)))
    (-> result
        (conj (format (:normal-column convert-map) (:name column))))))

(defn convert-columns-into-entity
  [result columns max]
  (loop [result result
         cnt 0]
    (if (nth columns cnt nil)
      (if (> cnt max) (conj result (:column-omission convert-map))
          (recur (into result (convert-column-into-columns [] (nth columns cnt))) (inc cnt)))
      result)))

; この行数を超えるカラムは省略され、省略を示す文字列が表示される。
(def omission-line-count 2)

(defn convert-table-to-entity
  [result table]
  (-> result
      (conj (format (:normal-entity-start convert-map) (:table table) (:table table)))
      (convert-columns-into-entity (:columns table) omission-line-count)
      (conj (format (:normal-entity-end convert-map) (:table table))))
  )

(defn convert-tables-to-entities
  [result tables]
  (loop [result result
         cnt 0]
    (if (nth tables cnt nil)
      (recur (into result (convert-table-to-entity [] (nth tables cnt))) (inc cnt))
      result)))

(defn get-cardinality
  [this_conn that_conn]
  (str (get-in convert-map [:cardinality :this (keyword this_conn)])
       (get-in convert-map [:cardinality :center])
       (get-in convert-map [:cardinality :that (keyword that_conn)])))

(defn convert-ex-relations-to-cardinalities
  [result table-ex-relations]
  (into result (for [table-ex-relations table-ex-relations
                     table-ex-relation table-ex-relations
                     ex-relations (:ex-relations table-ex-relation)]
                       (str (:table table-ex-relation) "  " (get-cardinality (:this_conn ex-relations) (:that_conn ex-relations)) "  " (:referenced_table_name ex-relations))))
                 )

(defn get-all-ex-relations
  [db-data]
  (remove nil? (into []
                     (for [table (:tables db-data)]
                       (if (> (count (:ex-relations table)) 0)
                         (conj [] {:table (:table table), :ex-relations (:ex-relations table)})
                         nil)))))

(defn convert
  "グループ分け（パッケージ分け）をせずにpumlデータを返す"
  [db-data]
  (-> []
      (conj (:start convert-map))
      (conj (format (:package-start convert-map) (:db_name db-data) (:db_name db-data)))
      (convert-tables-to-entities (:tables db-data))
      (convert-ex-relations-to-cardinalities (get-all-ex-relations db-data))
      (conj (:package-end convert-map))
      (conj (:end convert-map))))

(defn- get-by-group
  [db-data]
  (let [group-list (distinct (into [nil] (for [t (:tables db-data)] (:group t))))]
    (for [group group-list]
      {
       :group group
       :tables (into [] (filter (fn [t] (= (:group t) group)) (:tables db-data)))
       })))

(defn convert-for-group
  "グループ分け（パッケージ分け）をしたpumlデータを返す"
  [db-data]
  (let [body (into []
                   (for [group (get-by-group db-data)
                         :when (> (count (:tables group)) 0)]
                     (let [package (if (empty? (:group group)) (:db_name db-data) (:group group))]
                       (-> []
                           (conj (format (:package-start convert-map) package package))
                           (convert-tables-to-entities (:tables group))
                           (conj (:package-end convert-map))
                           (convert-ex-relations-to-cardinalities (get-all-ex-relations group))
                           )
                       )))
        result-body (flatten body)]
    (into (into [(:start convert-map)] result-body) [(:end convert-map)]))
  )
