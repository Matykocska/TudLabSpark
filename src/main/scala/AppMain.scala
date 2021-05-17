import org.apache.spark.sql._

object AppMain extends App {

    System.out.println("Running main method!")

    val mainMenu = new Menu()

    var exitCondition = false

    do {
      mainMenu.printOptions()
      exitCondition = mainMenu.parseInput()
    } while ( !exitCondition )

    System.out.println("Finished main method!")
}