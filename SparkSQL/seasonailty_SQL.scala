package com.haiteam

object seasonailty_sql {
  def main(args: Array[String]): Unit = {
    import java.util.Calendar

    import org.apache.spark.sql.SparkSession

    var spark = SparkSession.builder().config("spark.master", "local").getOrCreate()

    var salesFile = "kopo_channel_seasonality_new.csv"
    // 절대경로 입력
    var salesDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + salesFile)


    salesDf.createOrReplaceTempView("rawData")

    // 이동평균을 위한 함수, 쿼리 제작
    def wingLength(window: Int): Int = {
      if (window % 2 == 0) {
        throw new IllegalArgumentException("Only an odd number, please.")
      }
      var wing = (window - 1) / 2
      wing
    }

    var window1 = 17 // 4달

    var window2 = 5  // 1달

    var wing1 = wingLength(window1).toString() // 값 : 8

    var wing2 = wingLength(window2).toString() // 값 : 2

    var validWeek = 52

    var query_rolling1 = """OVER (PARTITION BY REGIONID, PRODUCT
                        ORDER BY REGIONID, PRODUCT, YEARWEEK
                        ROWS BETWEEN """ + wing1 + " PRECEDING AND " + wing1 + " FOLLOWING) "

    var query_rolling2 = """OVER (PARTITION BY REGIONID, PRODUCT
                        ORDER BY REGIONID, PRODUCT, YEARWEEK
                        ROWS BETWEEN """ + wing2 + " PRECEDING AND " + wing2 + " FOLLOWING) "

    // 계절성 지수 산출 sparkSQL 로직

    var query_cleansing = """SELECT REGIONID
                        ,PRODUCT
                        ,YEARWEEK
                        ,SUBSTR(YEARWEEK,5,2) AS WEEK
                        ,CASE WHEN QTY<0 THEN 0
                        ELSE QTY END AS QTY
                        FROM rawData
                        WHERE SUBSTR(YEARWEEK,5,2) <= """ + validWeek.toString()

    var data_cleansed = spark.sql(query_cleansing)

    println("...")
    println("=== Loading data from Oracle DB O.K. ===")

    data_cleansed.createOrReplaceTempView("data_cleansed")

    var query_ma = "SELECT A.*, STDDEV(MA) " + query_rolling2 + """ AS STDDEV
                    FROM ( SELECT REGIONID
                    ,PRODUCT
                    ,YEARWEEK
                    ,QTY
                    ,AVG(QTY) """ + query_rolling1 + " AS MA " + "FROM data_cleansed) A"

    var data_ma = spark.sql(query_ma)

    println("...")
    println("=== Data Cleansing Process O.K. ===")

    data_ma.createOrReplaceTempView("data_ma")

    var query_smoothing = "SELECT A.* " + ",AVG(REFINED) " + query_rolling2 + """ AS SMOOTHED
                          FROM ( SELECT REGIONID
                            ,PRODUCT
                            ,YEARWEEK
                            ,QTY
                            ,MA
                            ,STDDEV
                            ,(MA + STDDEV) AS UPPER_BOUND
                            ,(MA - STDDEV) AS LOWER_BOUND
                            ,CASE WHEN QTY > (MA + STDDEV) THEN (MA + STDDEV)
                                WHEN QTY < (MA - STDDEV) THEN (MA - STDDEV)
                                ELSE QTY END AS REFINED FROM data_ma) A"""

    var data_smoothed = spark.sql(query_smoothing)

    data_smoothed.createOrReplaceTempView("data_smoothed")

    var query_seasonal = """SELECT A.*
                           ,CASE WHEN SMOOTHED = 0 THEN 1
                               ELSE (QTY / SMOOTHED) END AS SEASONALITY_STABLE
                           ,CASE WHEN SMOOTHED = 0 THEN 1
                               ELSE (REFINED / SMOOTHED) END AS SEASONALITY_UNSTABLE
                           FROM data_smoothed A"""

    var data_seasonal = spark.sql(query_seasonal)

    println("...")
    println("=== Calculating Seasonality Index Process O.K. ===")

    data_seasonal.createOrReplaceTempView("data_seasonal")

    println("...")
    println("=== Showing Seasonality Index Table O.K. ===")
    data_seasonal.show(10)

    var query_seasonal_final = """SELECT REGIONID
                                  ,PRODUCT
                                  ,QTY
                                  ,SUBSTR(YEARWEEK, 5, 2) AS WEEK
                                  ,AVG(SEASONALITY_STABLE) AS SEASONALITY_STABLE
                                  ,AVG(SEASONALITY_UNSTABLE) AS SEASONALITY_UNSTABLE
                                FROM data_seasonal
                                GROUP BY REGIONID, PRODUCT, QTY, SUBSTR(YEARWEEK, 5, 2)"""

    var data_seasonal_final = spark.sql(query_seasonal_final)

    data_seasonal_final.createOrReplaceTempView("data_seasonal_final")

    println("...")
    println("=== Showing Final Seasonality Index Table O.K. ===")
    data_seasonal_final.show(10)

    data_seasonal_final.
    coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/data_seasonal_final.csv") // 저장파일명
  }
}
