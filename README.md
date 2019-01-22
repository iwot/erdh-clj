# erdh-clj
ERD出力のためのヘルパー（現在はPlantUML形式の出力のみ）

## 実行のサンプル

以下のようにすることで、
DBへの接続情報に"C:\\path\\to\\db_con.yaml"を使用し、  
追加の情報を"C:\\path\\to\\ex_table_info.yaml"から取得し、  
中間形式を"C:\\path\\to\\db_intermediate.yaml"に出力し、  
結果を"C:\\path\\to\\result.puml"に出力します。  

```
java -jar erdh-0.1.0-SNAPSHOT-standalone.jar -s mysql -f "C:\\path\\to\\db_con.yaml" -e "C:\\path\\to\\ex_table_info.yaml" -i yaml -o "C:\\path\\to\\db_intermediate.yaml" "C:\\path\\to\\result.puml"
```
