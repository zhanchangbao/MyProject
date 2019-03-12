package scala.com.jiyong.scala

object function {

  def add() = {
    println("just a test")
  }

  def sayname(name:String) = {
    println("hello"+" "+ name)
  }

  def sayhello(name:String,age:Int) ={
    if(age >= 18){
      println(s"hello,$name,you are $age years old")
    }else{
      age
    }
  }

  def max(a:Int,b:Int) = {
    if(a > b){
      a
    }else{
      b
    }
  }

/*  def fab(n:Int) = {
    if(n < 2) {
      1
    }else{
     fab(n-1) + fab(n-2)
    }
  }*/

  def hello(name:String="leo") = {
    println("hello" + " " + name)
  }

  def printcourse(name:String*) = {
    name.foreach(x => println(x))
  }

  def sayhelloall(firstname:String,middlename:String,lastname:String) = {
    println(firstname + " " + middlename + " " + lastname)
  }





  def main(args: Array[String]): Unit = {

    sayhelloall(firstname = "monkey",middlename = "D",lastname = "luffy")

    /*hello()
    hello("zhangsan")*/

//    add()
//    sayname("zhangsan")
/*
    sayhello("zhangsan",19)

    max(12,19)

    (a:Int,b:Int) => a+b
*/



  }

}