```
curl -XPUT 's100:9200/objectinfo?pretty' -H 'Content-Type: application/json' -d'
{                   
    "settings": {
        "analysis": {
            "filter": {
                "trigrams_filter": {
                    "type":     "ngram",
                    "min_gram": 2,
                    "max_gram": 20
                }
            },
            "analyzer": {
                "trigrams": {
                    "type":      "custom",
                    "tokenizer": "standard",
                    "filter":   [
                        "lowercase",
                        "trigrams_filter"
                    ]
                },
                "ik": {
                    "tokenizer" : "ik_max_word"
                }
            }	
        }
    },
    "mappings": {
        "person": {
            "properties": {
                "name" : {
                    "type" : "string",
                    "analyzer" : "ik_max_word"
                },
                "idcard" : {
                    "type" : "string",
                    "analyzer": "trigrams"
                },               
                "sex" : {
                    "type" : "long",
                    "index" : "not_analyzed"
                },
                "reson" : {
                    "type" : "string",
                    "analyzer" : "ik_max_word"
                },
                "pkey" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "tag" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "creator" : {
                    "type" : "string",
                    "analyzer" : "ik_max_word"
                },
                "cphone" : {
                    "type" : "string",
                    "analyzer": "trigrams"
                },
                "platformid" : {                          
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "feature" : {
                    "type" : "string",
                    "index" : "not_analyzed"
                },
                "createtime" : {
                    "type" : "date",
                    "format": "yyyy-MM-dd HH:mm:ss"
                },
                "updatetime" : {
                    "type" : "date",
                    "format": "yyyy-MM-dd HH:mm:ss"
                }	
            }
        }
    }
}
'
```
