curl -XDELETE 's100:9200/dynamic?pretty'  -H 'Content-Type: application/json'
curl -XPUT 's100:9200/dynamic?pretty' -H 'Content-Type: application/json' -d'
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
                "f" : {
                    "type" : "string",
                    "analyzer" : "trigrams"
                },
                "t" : {
                    "type" : "date",
                    "format": "yyyy-MM-dd HH:mm:ss"
                },               
                "fea" : {
                    "type" : "long",
                    "index" : "not_analyzed"
                }
            }
        }
    }
}
'
