import org.apache.spark.sql._
import com.google.cloud.spark.bigquery._
import java.nio.file.{Paths, Files}
import scala.util.matching.Regex
import org.apache.spark.sql.functions.col
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SessionModel {

  private var session = SparkSession.builder()
    .appName("tlspark")
    .config("spark.master", "local")
    .config("credentialsFile", sys.env("GOOGLE_APPLICATION_CREDENTIALS"))
    .getOrCreate()

  private var currentDF: DataFrame = _

  private var filteredDF: DataFrame = _

  var dfName = "currentDF"

  var bqTable = "bigquery-public-data.epa_historical_air_quality.air_quality_annual_summary"

  var csvPath = "input_files/input.csv"

  val columnFilterPattern = raw"([a-z_]+\s*)*([a-z_]+)"

  val orderFilterPattern = raw"([a-z_]+\s*)*([a-z_]+)"

  val limitFilterPattern = raw"(\d+)"

  var columnStr = "*"

  var orderStr = ""

  var limitStr = ""

  var sqlFilter = s"SELECT * FROM $dfName"

  def setBigQueryTable(project: String, dataset: String, table: String) = {
    val projectN = if (project.isEmpty) "bigquery-public-data"       else project
    val datasetN = if (dataset.isEmpty) "epa_historical_air_quality" else dataset
    val tableN   = if (table.isEmpty)   "air_quality_annual_summary" else table

    bqTable = s"$projectN.$datasetN.$tableN"
  }

  def openBigQueryTable: Boolean = {
    try {
      currentDF = session.read.bigquery(bqTable)
      currentDF.createOrReplaceTempView(dfName)
      filteredDF = currentDF
      return true
    } catch {
      case _: Throwable => println("Couldn't open table!")
        return false
    }
  }

  def setCSVPath(path: String) = {
    csvPath = if (path.isEmpty) "input_files/input.csv" else path
  }

  def openCSVFile: Boolean = {
    if (Files.exists(Paths.get(csvPath))) {
      currentDF = session.read
        .option("header",true)
        .csv(csvPath)
      currentDF.createOrReplaceTempView(dfName)
      filteredDF = currentDF
      return true
    } else {
      println(csvPath + "doesn't exist!")
      return false
    }
  }

  def writeCSVFile = {
    val timestamp = LocalDateTime.now.format(DateTimeFormatter.ofPattern("YYYY_MM_dd_HH_mm_ss"))
    filteredDF.write
      .option("header", true)
      .csv(s"output_files/csv_out_$timestamp")
  }

  def setColumnFilter(columnFilter: String): Boolean = {
    if (!columnFilter.isEmpty) {
      if (columnFilter.matches(columnFilterPattern)) {
        if (columnFilter.split(" ").forall(col => filteredDF.columns.contains(col))) {
          filteredDF = filteredDF.select(columnFilter.split(" ").map(m=>col(m)):_*)
          columnStr = columnFilter.split(" ").mkString(", ")
          return true
        } else {
          println("No such column names!")
          return false
        }
      } else {
        println("Wrong column filter format!")
        return false
      }
    }
    return true
  }

  def setOrderFilter(orderFilter: String): Boolean = {
    if (!orderFilter.isEmpty) {
      if (orderFilter.matches(orderFilterPattern)) {
        if (orderFilter.split(" ").forall(col => filteredDF.columns.contains(col))) {
          filteredDF = filteredDF.orderBy(orderFilter.split(" ").map(m=>col(m)):_*)
          orderStr = " ORDER BY " + orderFilter.split(" ").mkString(", ")
          return true
        } else {
          println("No such column names!")
          return false
        }
      } else {
        println("Wrong order filter format!")
        return false
      }
    }
    return true
  }

  def setLimitFilter(limitFilter: String): Boolean = {
    if (!limitFilter.isEmpty) {
      if (limitFilter.matches(limitFilterPattern)) {
        filteredDF = filteredDF.limit(limitFilter.toInt)
        limitStr = " LIMIT " + limitFilter
        return true
      } else {
        println("Wrong limit filter format!")
        return false
      }
    }
    return true
  }

  def resetFilter = {
    columnStr = "*"
    orderStr = ""
    limitStr = ""
    filteredDF = currentDF
  }

  def setSQLFilter = {
    sqlFilter = s"SELECT $columnStr FROM $dfName$orderStr$limitStr"
  }

  def printSchema = {
    filteredDF.printSchema()
  }

  def printTable = {
    filteredDF.show()
  }

  def getRowCount = {
    filteredDF.count()
  }

  def close = {
    session.stop()
  }

}