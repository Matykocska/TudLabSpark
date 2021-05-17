import scala.io.StdIn
import scala.util.{Try, Success, Failure}

class Menu {

  val sessionModel = new SessionModel()

  var info = new DataInfo()

  var sourceType: String = _

  val titleStr = """**************************************************************************
                    |*    ______        ____        __   ____              __     ___   ___   *
                    |*   /_  __/_ _____/ / /  ___ _/ /  / __/__  ___ _____/ /__  / _ \ <  /   *
                    |*    / / / // / _  / /__/ _ `/ _ \_\ \/ _ \/ _ `/ __/  '_/ / // / / /    *
                    |*   /_/  \_,_/\_,_/____/\_,_/_.__/___/ .__/\_,_/_/ /_/\_\  \___(_)_/     *
                    |*                                   /_/                                  *
                    |**************************************************************************""".stripMargin

 def infoStr = {
   s"""Current DataFrame: ${info.currentDF}
      |  Source: ${info.source}
      |  Filter: ${info.filter}
      |  Row count: ${info.rowCount}""".stripMargin
 }

  val optionsStr = """1. Read from BigQuery
                      |2. Read from CSV
                      |3. Set filter
                      |4. Reset filter
                      |5. Print schema
                      |6. Print to console
                      |7. Write to CSV
                      |8. Exit""".stripMargin

  val promptStr = "Input: "

  val gap = "\n\n"

  def printOptions() = {
    print("\u001b[2J")
    println(titleStr)
    println(gap)
    println(infoStr)
    println(gap)
    println(optionsStr)
    println(gap)
  }

  def parseInput(): Boolean = {
    val option = getOption()
    var exit = false
    option match {
      case Success(opt) =>
        opt match {
          case 1 => println(gap) // BigQuery read
            val bqProject = StdIn.readLine("Project (default: bigquery-public-data): ")
            val bqDataset = StdIn.readLine("Dataset (default: epa_historical_air_quality): ")
            val bqTable = StdIn.readLine("Table (default: air_quality_annual_summary): ")
            sessionModel.setBigQueryTable(bqProject, bqDataset, bqTable)
            if (sessionModel.openBigQueryTable) sourceType = "BQ" else sourceType = "N/A"
          case 2 => println(gap) // CSV read
            val csvPath = StdIn.readLine("CSV file path (default: ./input_files/input.csv): ")
            sessionModel.setCSVPath(csvPath)
            if (sessionModel.openCSVFile) sourceType = "CSV" else sourceType = "N/A"
          case 3 => println(gap) // Filter set
            var ret = false
            do {
              val columnFilter = StdIn.readLine("Filter columns (default: ALL): ")
              ret = sessionModel.setColumnFilter(columnFilter)
            } while (!ret)
            do {
              val orderFilter = StdIn.readLine("Order by (default: NONE): ")
              ret = sessionModel.setOrderFilter(orderFilter)
            } while (!ret)
            do {
              val limitFilter = StdIn.readLine("Limit rows (default: NONE): ")
              ret = sessionModel.setLimitFilter(limitFilter)
            } while (!ret)
            sessionModel.setSQLFilter
          case 4 => println(gap) // Filter reset
            sessionModel.resetFilter
            sessionModel.setSQLFilter
          case 5 => println(gap) // Schema print
            sessionModel.printSchema
            StdIn.readLine("Press ENTER to continue... ")
          case 6 => println(gap) // Table print
            sessionModel.printTable
            StdIn.readLine("Press ENTER to continue... ")
          case 7 => println(gap) // CSV write
            sessionModel.writeCSVFile
          case 8 => println(gap) // Exit
            sessionModel.close
            exit = true
        }
      case Failure(e) =>
        println("Bad option: " + e.getMessage)
    }
    if ( !exit && sourceType != "N/A" ) { updateInfo }
    exit
  }

  def getOption(): Try[Int] = {
    val input = Try(StdIn.readLine(promptStr).toInt)
    val option = input.filter(x => (1 <= x && 8 >= x))
    option
  }

  def updateInfo = {
    info.currentDF = sessionModel.dfName
    sourceType match {
      case "BQ"  => info.source = sessionModel.bqTable
      case "CSV" => info.source = sessionModel.csvPath
      case _ => info.source = "N/A"
    }
    info.filter = sessionModel.sqlFilter
    info.rowCount = sessionModel.getRowCount
  }
}