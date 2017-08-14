```
curl -XPUT 's100:9200/dynamic/_mapping/person?pretty' -H 'Content-Type: application/json' -d'
{
  "person": {
     "properties": {
        "sj": {
            "type": "long",
            "index": "not_analyzed"
        }
    }
  }
}
'
```
