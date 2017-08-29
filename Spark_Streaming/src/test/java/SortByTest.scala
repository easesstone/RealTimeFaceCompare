object SortByTest {

  def main(args: Array[String]): Unit = {
    val list = List(("zhangsan", 7), ("lisi", 3), ("wang", 5), ("zhao", 1), ("song", 8), ("cai", 2), ("liu", 11))
    val result = list.sortBy { case (name, num) => num }
    println(result)
    val re = list.sortWith { case (user1, user2) => user1._2 > user2._2 }
    println("---------------------------------------------")
    println(re)


  }

}
