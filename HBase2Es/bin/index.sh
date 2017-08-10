curl -XDELETE 's100:9200/objectinfo?pretty' -H 'Content-Type: application/json'
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
                    "type" : "text",
                    "analyzer" : "ik_max_word"
                },
                "idcard" : {
                    "type" : "text",
                    "analyzer": "trigrams"
                },
                "sex" : {
                    "type" : "long",
                    "index" : "not_analyzed"
                },
                "reson" : {
                    "type" : "text",
                    "analyzer" : "ik_max_word"
                },
                "pkey" : {
                    "type" : "text",
                    "index" : "not_analyzed"
                },
                "tag" : {
                    "type" : "text",
                    "index" : "not_analyzed"
                },
                "creator" : {
                    "type" : "text",
                    "analyzer" : "ik_max_word"
                },
                "cphone" : {
                    "type" : "text",
                    "analyzer": "trigrams"
                },
                "platformid" : {
                    "type" : "text",
                    "index" : "not_analyzed"
                },
                "feature" : {
                    "type" : "text",
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
