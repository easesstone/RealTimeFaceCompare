参考链接：  
http://www.jianshu.com/p/d9fb7afa634d  
https://www.elastic.co/blog/to-shade-or-not-to-shade  
https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/_embedding_jar_with_dependencies.html  

java 依赖包冲突，使用maven的Shade方式解决

[TOC]

问题描述

程序中同时使用了hadoop工具包与ElasticSearch工具导致jar包。
程序报错：

java.lang.NoSuchMethodError: com.google.common.util.concurrent.MoreExecutors.directExecutor()Ljava/util/concurrent/Executor;
内容如下：

java.lang.NoSuchMethodError: com.google.common.util.concurrent.MoreExecutors.directExecutor()Ljava/util/concurrent/Executor;
at org.elasticsearch.threadpool.ThreadPool.(ThreadPool.java:190)
原因分析

通过对上述错误进行google可以判断是由于Elasticsearch引用的guava包版本不正确而导致。程序中hadoop依赖的guava包版本为11版本，而ES所需要的版本为18以上。因此我们首先在maven中将guava的版本强制指定为18版本，但是将程序打包后上传到linux生成环境程序仍然无法正常运行。

解决方案

根据官网博客说明，我们将ElasticSearch以及它的相关依赖包以shade的打包成一个独立的jar包，对应ElasticSearch相关类的使用均从此jar包引用。

1、shade Elasticsearch包

首先创建新的maven工程，pom.xml文件如下：
```
<groupId>my.elasticsearch</groupId>
  <artifactId>es-shaded</artifactId>
  <version>1.0-SNAPSHOT</version>
  <properties>
      <elasticsearch.version>2.1.2</elasticsearch.version>
  </properties>
  <dependencies>
      <dependency>
          <groupId>org.elasticsearch</groupId>
          <artifactId>elasticsearch</artifactId>
          <version>${elasticsearch.version}</version>
      </dependency>
      <dependency>
          <groupId>com.google.guava</groupId>
          <artifactId>guava</artifactId>
          <version>18.0</version>
      </dependency>
  </dependencies>
  <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-shade-plugin</artifactId>
              <version>2.4.1</version>
              <configuration>
                  <createDependencyReducedPom>false</createDependencyReducedPom>
              </configuration>
              <executions>
                  <execution>
                      <phase>package</phase>
                      <goals>
                          <goal>shade</goal>
                      </goals>
                      <configuration>
                          <relocations>
                              <relocation>
                                  <pattern>com.google.guava</pattern>
                                  <shadedPattern>my.elasticsearch.guava</shadedPattern>
                              </relocation>
                              <relocation>
                                  <pattern>org.joda</pattern>
                                  <shadedPattern>my.elasticsearch.joda</shadedPattern>
                              </relocation>
                              <relocation>
                                  <pattern>com.google.common</pattern>
                                  <shadedPattern>my.elasticsearch.common</shadedPattern>
                              </relocation>
                              <relocation>
                                  <pattern>org.elasticsearch</pattern>
                                  <shadedPattern>my.elasticsearch</shadedPattern>
                              </relocation>
                          </relocations>
                          <transformers>
                              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer" />
                          </transformers>
                      </configuration>
                  </execution>
              </executions>
          </plugin>
      </plugins>
  </build>
 ```
在pom.xml中我们指定了该项目依赖org.elasticsearch包，且版本为2.1.2，并强制指定了guava的版本为18（此处若不指定应该也会自行依赖18以上的包，但并未进行测试）。然后在build标签中可以看出，我们利用maven的shade工具完成打包情况如下：
org.joda映射为my.elasticsearch.joda
com.google.guava映射为my.elasticsearch.guava
com.google.common映射为my.elasticsearch.common
org.elasticsearch映射为my.elasticsearch
然后利用mvn clean install命令进行打包得到es-shaded-1.0-SNAPSHOT.jar，创建一个属于你自己版本的Elasticsearch包。之后将该包上传到私服maven镜像。

2、在工程中使用自己的Elasticsearch包

完成上数对Elasticsearch的打包之后，在自己工程中的pom.xml中，我们引用此包方式如下：
```
<dependencies>
       ...
    <dependency>
      <groupId>org.apache.hadoop</groupId>
      <artifactId>hadoop-client</artifactId>
      <version>${hadoop.version}</version>
    </dependency>
    <dependency>
      <groupId>org.apache.hive</groupId>
      <artifactId>hive-exec</artifactId>
      <version>${hive.version}</version>
    </dependency>
    <dependency>
      <groupId>org.antlr</groupId>
      <artifactId>ST4</artifactId>
      <version>4.0.8</version>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>my.elasticsearch</groupId>
      <artifactId>es-shaded</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>
  </dependencies>
```
在使用上述方式引用了Elasticsearch包之后，在程序中我们可以这样对Elasticsearch包进行引用
代码如下：
```
import my.elasticsearch.ElasticsearchException;
import my.elasticsearch.action.bulk.BulkItemResponse;
import my.elasticsearch.action.bulk.BulkRequestBuilder;
import my.elasticsearch.action.bulk.BulkResponse;
import my.elasticsearch.action.index.IndexRequest;
import my.elasticsearch.client.transport.NoNodeAvailableException;
```
这样确保了我们使用的elasticsearch包是我们之前创建的。对Elasticsearch所依赖版本的 joda相关包的引用方式也是类似：

import my.elasticsearch.joda.time.DateTime;
这样就不会出现Elasticsearch依赖包不正确的情况。

