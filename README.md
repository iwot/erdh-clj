# erdh-clj
ERD出力のためのヘルパー（現在はPlantUML形式の出力のみ）

## 実行のサンプル

以下のようにすることで、
DBへの接続情報に"C:\\path\\to\\db_con.yaml"を使用し、  
追加の情報を"C:\\path\\to\\ex_table_info.yaml"から取得し、  
中間形式を"C:\\path\\to\\db_intermediate.yaml"に出力し、  
結果を"C:\\path\\to\\result.puml"に出力します。  

```
java -jar erdh-0.1.8-SNAPSHOT-standalone.jar -c "C:\\path\\to\\config_mysql.yaml" "C:\\path\\to\\result_m.puml"
```

config_mysql.yaml
```config_mysql.yaml
# mysql,postgres,sqliteから選択
source: mysql
# DBへの接続情報（yamlの場合は中間形式ファイル）
source-from: "C:\\path\\to\\db_con_mysql.yaml"
# 出力対象とするグループ。すべてを対象とする場合はgroup自体を削除
group:
- DATA
- MASTER
# 中間形式の出力先
intermediate:
  save-to: "C:\\path\\to\\db_intermediate_m.yaml"
# テーブル等の追加情報
ex-info: "C:\\path\\to\\ex_table_info.yaml"
```

db_con_mysql.yaml
```db_con_mysql.yaml
dbtype: mysql
host: localhost
dbname: TESTDB
user: root
password: password
```

ex_table_info.yaml
```ex_table_info.yaml
tables:
- table: member_items
  is_master: true
  group: DATA
  relations:
    - referenced_table_name: members
      columns:
        - from: "member_id"
          to: "id"
      this_conn: "one"
      that_conn: "zero-or-one"
    - referenced_table_name: items
      columns:
        - from: "item_id"
          to: "id"
      this_conn: "onlyone"
      that_conn: "many"
- table: items
  is_master: true
  group: DATA
- table: item_types
  is_master: true
  group: MASTER
- table: member_items
  group: DATA
- table: members
  group: DATA
```

this_conn,that_conn に使用するカーディナリティマップ(Clojure内での定義)
```
{
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
```
