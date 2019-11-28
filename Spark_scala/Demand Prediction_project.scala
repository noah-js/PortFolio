package com.haiteam

import java.text.SimpleDateFormat
import java.util.Calendar

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.avg

object Seasonaility_project {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().
      setAppName("DataLoading").
      setMaster("local[*]")
    var sc = new SparkContext(conf)
    val spark = new SQLContext(sc)
    import spark.implicits._

    //실제 판매량 데이터 추출
    var salesFile = "pro_actual_sales.csv"
    // 절대경로 입력
    var salesDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + salesFile)

    // 데이터 확인 (3)
    print(salesDf.show(2))

    var salesColums = salesDf.columns.map(x => {
      x.toLowerCase()
    })
    var regionidno1 = salesColums.indexOf("regionseg1")
    var productno1 = salesColums.indexOf("productseg1")
    var productno2 = salesColums.indexOf("productseg2")
    var regionidno2 = salesColums.indexOf("regionseg2")
    var regionidno3 = salesColums.indexOf("regionseg3")
    var productno3 = salesColums.indexOf("productseg3")
    var yearweekno = salesColums.indexOf("yearweek")
    var yearno = salesColums.indexOf("year")
    var weekno = salesColums.indexOf("week")
    var qtyno = salesColums.indexOf("qty")


    //실제 프로모션 적용 데이터 추출
    var promotionFile = "pro_promotion.csv"

    var promotionDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + promotionFile)

    print(promotionDf.show(2))

    var promotionColums = promotionDf.columns.map(x => {
      x.toLowerCase()
    })
    var regionidno = promotionColums.indexOf("regionseg")
    var salesidno = promotionColums.indexOf("salesid")
    var productgroup2 = promotionColums.indexOf("productgroup")
    var itemno = promotionColums.indexOf("item")
    var targetweekno3 = promotionColums.indexOf("targetweek")
    var planweekno = promotionColums.indexOf("planwee")
    var map_priceno = promotionColums.indexOf("map_price")
    var irno = promotionColums.indexOf("ir")
    var pmapno = promotionColums.indexOf("pmap")
    var pmap10no = promotionColums.indexOf("pmap10")
    var pro_percentno = promotionColums.indexOf("pro_percent")

    //promotion이 적용된 주차(planWeek)를 targetWeek와 맞추기 위해 데이터 가공
    //201601부터 promotion이 시작됬기 때문에 tartgetWeek와 planWeek를 같은 주차에 적용하는 과정
    var promotionRdd = promotionDf.rdd

    var minPlanWeek = promotionRdd.map(x => {
      x.getString(planweekno).toInt
    }).min()

    var filteredPromotion = promotionRdd.filter(x => {
      var check = false
      var targetWeek = x.getString(targetweekno3)
      var map_price = x.getString(map_priceno)
      if (targetWeek.toInt >= minPlanWeek) {
        check = true
      }
      check
    })

    //할인가격(map_price)이 0이라면 할인된가격이(ir) 0이여야 하기에 이상데이터 가공
    //최종 할인된가격(pmap) 최종할인된가격의 10%할인가격(pmap10) 할인율 재계산
    var processRdd = filteredPromotion.groupBy(x => {
      (x.getString(productgroup2), x.getString(itemno))
    }).flatMap(x => {
      var key = x._1
      var data = x._2

      var mapPrice = data.map(x => {
        x.getString(map_priceno)
      }).toArray

      var mapSum = data.map(x => {
        x.getString(map_priceno).toDouble
      }).sum

      //처음부터 비교값이 0인 컬럼들을 찾고 따로 size를 조정
      var count_nz = data.filter(x => {
        var checkValid = false
        if (x.getString(map_priceno).toInt > 0) {
          checkValid = true
        }
        checkValid
      }).size

      //0이 포함된 값을 0으로 재정의 하고, 원래값들 주기
      var new_mapPrice =
        if (mapPrice.contains("0")) {
          if (count_nz != 0) {
            mapSum / count_nz
          } else {
            0
          }
        } else {
          mapPrice(0)
        }

      var result = data.map(x => {
        var pmap = if (new_mapPrice == 0) {
          0
        } else {
          new_mapPrice.toString.toDouble - x.getString(irno).toDouble
        }
        var pmap10 = if (pmap == 0) {
          0
        } else {
          pmap * 0.9
        }
        var pro_percent = if (pmap10 == 0) {
          0
        } else {
          1 - (pmap10 / new_mapPrice.toString.toDouble)
        }
        var ir = if (new_mapPrice == 0) {
          0
        } else {
          x.getString(irno)
        }

        (x.getString(regionidno),
          x.getString(salesidno),
          x.getString(productgroup2),
          x.getString(itemno),
          x.getString(targetweekno3),
          x.getString(planweekno),
          new_mapPrice.toString.toDouble,
          ir.toString.toDouble,
          pmap,
          math.round(pmap10),
          pro_percent)
      })
      result
    })

    //Row재정의 해서 dataFrame으로 만들기
    var resultMap = processRdd.map(x => {
      (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11)
    })

    //가공된 RDD를 데이터프레임으로 만들어 준 후 실제 판매데이터(pro_actual_sales)와 leftJoin을 통해 하나의 데이터로 만든다.
    var testDf = resultMap.toDF("REGIONSEG", "SALESID", "PRODUCTGROUP", "ITEM", "TARGETWEEK", "PLANWEEK", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT")

    testDf.createOrReplaceTempView("filterPromotion")

    salesDf.createOrReplaceTempView("salesData")

    //sparkSQL을 통해 leftJoin
    var leftJoinData = spark.sql("""SELECT A.*,B.MAP_PRICE,B.IR,B.PMAP,B.PMAP10,B.PRO_PERCENT FROM salesData A left join filterPromotion B ON A.regionseg1 = B.REGIONSEG AND A.productseg2 = B.PRODUCTGROUP AND A.regionseg2 = B.SALESID AND A.productseg3 = ITEM and A.yearweek = B.TARGETWEEK""")

    leftJoinData.
      coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/leftJoin.csv") // 저장파일명

    //join한 데이터 확인 후 load
    //데이터 저장시 파일 형식으로 저장되기 때문에 csv이름을 바꿔서 load
    var leftJoinFile = "sales_promotion_leftjoinData.csv"

    var leftJoinDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + leftJoinFile)

    print(leftJoinDf.show(2))

    var leftJoinRdd = leftJoinDf.rdd

    var reftJoinColums = leftJoinDf.columns.map(x => {
      x.toLowerCase()
    })
    var regionidno = reftJoinColums.indexOf("regionseg1")
    var productno = reftJoinColums.indexOf("productseg1")
    var productno2 = reftJoinColums.indexOf("productseg2")
    var regionidno2 = reftJoinColums.indexOf("regionseg2")
    var regionidno3 = reftJoinColums.indexOf("regionseg3")
    var productno3 = reftJoinColums.indexOf("productseg3")
    var yearweekno = reftJoinColums.indexOf("yearweek")
    var yearno = reftJoinColums.indexOf("year")
    var weekno = reftJoinColums.indexOf("week")
    var qtyno = reftJoinColums.indexOf("qty")
    var map_priceno = reftJoinColums.indexOf("map_price")
    var irno = reftJoinColums.indexOf("ir")
    var pmapno = reftJoinColums.indexOf("pmap")
    var pmap10no = reftJoinColums.indexOf("pmap10")
    var pro_percentno = reftJoinColums.indexOf("pro_percent")


    //join된 데이터에 null값에 0으로 채우고, promotion유무 판별
    var fullDataRdd = leftJoinRdd.map(x => {
      var map_price = x.getString(map_priceno)
      var ir = x.getString(irno)
      var pmap = x.getString(pmapno)
      var pmap10 = x.getString(pmap10no)
      var new_pro_percent = x.getString(pro_percentno)
      var promotionCheck = "Y"

      if (map_price == null) {
        map_price = "0"
      }

      if (ir == null) {
        ir = "0"
      }

      if (pmap == null) {
        pmap = "0"
      }

      if (pmap10 == null) {
        pmap10 = "0"
      }

      if (new_pro_percent == null) {
        new_pro_percent = "0"
      }

      if (map_price == "0"){
        promotionCheck = "N"
      }

      (x.getString(regionidno),
        x.getString(productno2),
        x.getString(regionidno2),
        x.getString(regionidno3),
        x.getString(productno3),
        x.getString(yearweekno),
        x.getString(yearno),
        x.getString(weekno),
        x.getString(qtyno),
        map_price,
        ir,
        pmap,
        pmap10,
        new_pro_percent,
        promotionCheck)
    })

    var testDf = fullDataRdd.toDF("REGIONSEG1", "PRODUCTSEG2", "REGIONSEG2", "REGIONSEG3", "PRODUCTSEG3", "YEARWEEK", "YEAR", "WEEK", "QTY", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT","PROMOTION_CHECK")

    //데이터가 제대로 가공되었는지 확인하기 위해 다시 저장
    testDf.
      coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/testAll.csv") // 저장파일명

    //저장된 데이터를 rename하여 불러옴
    var testFile = "testAllData.csv"

    var testAllDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + testFile)

    print(testAllDf.show(2))

    var testColums = testAllDf.columns.map(x => {
      x.toLowerCase()
    })
    var regionidno = testColums.indexOf("regionseg1")
    var productno2 = testColums.indexOf("productseg2")
    var regionidno2 = testColums.indexOf("regionseg2")
    var regionidno3 = testColums.indexOf("regionseg3")
    var productno3 = testColums.indexOf("productseg3")
    var yearweekno = testColums.indexOf("yearweek")
    var yearno = testColums.indexOf("year")
    var weekno = testColums.indexOf("week")
    var qtyno = testColums.indexOf("qty")
    var map_priceno = testColums.indexOf("map_price")
    var irno = testColums.indexOf("ir")
    var pmapno = testColums.indexOf("pmap")
    var pmap10no = testColums.indexOf("pmap10")
    var pro_percentno = testColums.indexOf("pro_percent")
    var promotionCheck = testColums.indexOf("promotion_check")

    var testAllRdd = testAllDf.rdd

    //이동평균 산출과 이를 통한 계절성 지수 산출을 위해서 비어있는 연주차를 넣어주고
    //해당 주차에 0값을 넣어준다.

    //빠진 주차 구하기
    def postWeek(inputYearWeek: String, gapWeek: Int): String = {
      var currYear = inputYearWeek.substring(0, 4).toInt
      var currWeek = inputYearWeek.substring(4, 6).toInt

      val calendar = Calendar.getInstance();
      calendar.setMinimalDaysInFirstWeek(4);
      calendar.setFirstDayOfWeek(Calendar.MONDAY);

      var dateFormat = new SimpleDateFormat("yyyyMMdd");

      calendar.setTime(dateFormat.parse(currYear + "1231"));

      var maxWeek = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)

      var conversion = currWeek + gapWeek
      if (maxWeek < conversion) {
        while (maxWeek < conversion) {
          currWeek = conversion - maxWeek
          currYear = currYear + 1
          calendar.setTime(dateFormat.parse(currYear + "1231"));
          maxWeek = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
          conversion = currWeek
        }
        return currYear.toString() + "%02d".format((currWeek))
      } else {
        return currYear.toString() + "%02d".format((currWeek + gapWeek))
      } // end of if
    }

    //빠진 주차에 값 넣어주는 로직
    var testAllMap = testAllRdd.groupBy(x => {
      (x.getString(regionidno),
        x.getString(productno2),
        x.getString(regionidno2),
        x.getString(regionidno3),
        x.getString(productno3))
    }).flatMap(x => {
      var key = x._1
      var data = x._2
      var yearweek = data.map(x => x.getString(yearweekno)).toArray.sorted
      var yearweekMax = data.map(x => x.getString(yearweekno)).max
      var yearweekMin = data.map(x => x.getString(yearweekno)).min

      var i = 1
      var tempYearweek = Array(yearweekMin)
      while (tempYearweek.last < yearweekMax) {
        tempYearweek ++= Array(postWeek(yearweekMin.toString, i))
        i = i + 1
      }
      var conversionArray = tempYearweek.diff(yearweek)

      val tmpMap = conversionArray.map(x => {
        val year = x.substring(0, 4)
        val week = x.substring(4, 6)
        val yearweek = year+week
        val qty = 0
        val map_price = 0
        val ir = 0
        val pmap = 0d
        val pmap10 = 0d
        val pro_percent = 0d
        val promotion_check = "N"
        (key._1, key._2, key._3, key._4, key._5, yearweek, year, week, qty.toString, map_price.toString,ir.toString, pmap.toString, pmap10.toString, pro_percent.toString,promotion_check.toString)
      })

      var resultMap = data.map(x=>{
        (x.getString(regionidno),
          x.getString(productno2),
          x.getString(regionidno2),
          x.getString(regionidno3),
          x.getString(productno3),
          x.getString(yearweekno),
          x.getString(yearno),
          x.getString(weekno),
          x.getString(qtyno),
          x.getString(map_priceno),
          x.getString(irno),
          x.getString(pmapno),
          x.getString(pmap10no),
          x.getString(pro_percentno),
          x.getString(promotionCheck)
        )
      })
      tmpMap ++ resultMap
    })

    //오름 차순으로 컬럼 정렬
    var sortedData = testAllMap.sortBy(x=>(x._2,x._4,x._5,x._6))

    var sortAllDf = sortedData.toDF("REGIONSEG1", "PRODUCTSEG2", "REGIONSEG2", "REGIONSEG3", "PRODUCTSEG3", "YEARWEEK", "YEAR", "WEEK", "QTY", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT","PROMOTION_CHECK")

    //데이터 저장
    sortAllDf.
      coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/test.csv") // 저장파일명

    //데이터 확인후 load
    var testFile = "test.csv"
    // 절대경로 입력
    var AllDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + testFile)


    //5일 기준으로 이동평균 qty구하기
    var refinedproductData = AllDf.withColumn("QTY",$"QTY".cast("Double")).sort("REGIONSEG1","PRODUCTSEG2","REGIONSEG2","REGIONSEG3","PRODUCTSEG3","YEARWEEK")
    var movingAvg = refinedproductData.withColumn("MOVING_QTY",avg(refinedproductData("QTY")).over(Window.partitionBy("REGIONSEG1","REGIONSEG3","PRODUCTSEG2").rowsBetween(-2,2)))

    //이동평균 구한 데이터 재정의
    var sortAllDf = movingAvg.toDF("REGIONSEG1", "PRODUCTSEG2", "REGIONSEG2", "REGIONSEG3", "PRODUCTSEG3", "YEARWEEK", "YEAR", "WEEK", "QTY", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT","PROMOTION_CHECK","MOVING_QTY")

    //데이터 저장
    sortAllDf.
      coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/movingResult.csv") // 저장파일명

    //데이터 load
    var movingData = "movingResultData.csv"
    // 절대경로 입력
    var movingDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + movingData)

    var movingColums = movingDf.columns.map(x => {x.toLowerCase()})
    var regionidno = movingColums.indexOf("regionseg1")
    var productno2 = movingColums.indexOf("productseg2")
    var regionidno2 = movingColums.indexOf("regionseg2")
    var regionidno3 = movingColums.indexOf("regionseg3")
    var productno3 = movingColums.indexOf("productseg3")
    var yearweekno = movingColums.indexOf("yearweek")
    var yearno = movingColums.indexOf("year")
    var weekno = movingColums.indexOf("week")
    var qtyno = movingColums.indexOf("qty")
    var map_priceno = movingColums.indexOf("map_price")
    var irno = movingColums.indexOf("ir")
    var pmapno = movingColums.indexOf("pmap")
    var pmap10no = movingColums.indexOf("pmap10")
    var pro_percentno = movingColums.indexOf("pro_percent")
    var promotionCheck = movingColums.indexOf("promotion_check")
    var movingQty = movingColums.indexOf("moving_qty")

    var movingRdd = movingDf.rdd

    //계절성 지수 구해서 전체 컬럼 재정의
    var seasonalityRdd = movingRdd.groupBy(x=>{
      (x.getString(regionidno),x.getString(productno2),x.getString(regionidno3)
      )
    }).flatMap(x=>{
      var key = x._1
      var data = x._2

      var finalData = data.map(x=>{
        var ratio = 1.0d
        var each_movingqty = x.getString(movingQty).toDouble

        if(each_movingqty != 0){
          ratio = x.getString(qtyno).toDouble / each_movingqty
        }else{
          ratio
        }
        (x.getString(regionidno),
          x.getString(productno2),
          x.getString(regionidno2),
          x.getString(regionidno3),
          x.getString(productno3),
          x.getString(yearweekno),
          x.getString(yearno),
          x.getString(weekno),
          x.getString(qtyno),
          x.getString(map_priceno),
          x.getString(irno),
          x.getString(pmapno),
          x.getString(pmap10no),
          x.getString(pro_percentno),
          x.getString(promotionCheck),
          x.getString(movingQty),
          ratio.toString)
      })
      finalData
    })

    //productSeg2,regionidSeg3,productSeg3,yearweek기준 오름차순 정렬
    var sortedData = seasonalityRdd.sortBy(x=>(x._2,x._4,x._5,x._6))
    var sortDf = sortedData.toDF("REGIONSEG1", "PRODUCTSEG2", "REGIONSEG2", "REGIONSEG3", "PRODUCTSEG3", "YEARWEEK", "YEAR", "WEEK", "QTY", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT","PROMOTION_CHECK","MOVING_AVG","SEASONALITY")

    sortDf.
      coalesce(1). // 파일개수
      write.format("csv"). // 저장포맷
      mode("overwrite"). // 저장모드 append/overwrite
      option("header", "true"). // 헤더 유/무
      save("c:/spark/bin/data/modifySeasonalityData.csv") // 저장파일명

    // 계절성 지수를 구한다음에 최종 목표인 예측판매량을 구하기 위한 로직
    var seasonFile = "seasonalityData.csv"

    var seasonDf =
      spark.read.format("csv").
        option("header", "true").
        option("Delimiter", ",").
        load("C:/spark_orgin_2.2.0/bin/data/" + seasonFile)

    //과거 데이터를 통해 미래 4주차를 예측하기 위해서
    //미래 4주차를 가져오기 위한 함수
    def postWeek(inputYearWeek: String, gapWeek: Int): String = {
      var currYear = inputYearWeek.substring(0, 4).toInt
      var currWeek = inputYearWeek.substring(4, 6).toInt

      val calendar = Calendar.getInstance();
      calendar.setMinimalDaysInFirstWeek(4);
      calendar.setFirstDayOfWeek(Calendar.MONDAY);

      var dateFormat = new SimpleDateFormat("yyyyMMdd");

      calendar.setTime(dateFormat.parse(currYear + "1231"));

      var maxWeek = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)

      var conversion = currWeek + gapWeek
      if (maxWeek < conversion) {
        while (maxWeek < conversion) {
          currWeek = conversion - maxWeek
          currYear = currYear + 1
          calendar.setTime(dateFormat.parse(currYear + "1231"));
          maxWeek = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR)
          conversion = currWeek
        }
        return currYear.toString() + "%02d".format((currWeek))
      } else {
        return currYear.toString() + "%02d".format((currWeek + gapWeek))
      } // end of if
    }

    var seasonColums = seasonDf.columns.map(x => {x.toLowerCase()})
    var regionidno = seasonColums.indexOf("regionseg1")
    var productno2 = seasonColums.indexOf("productseg2")
    var regionidno2 = seasonColums.indexOf("regionseg2")
    var regionidno3 = seasonColums.indexOf("regionseg3")
    var productno3 = seasonColums.indexOf("productseg3")
    var yearweekno = seasonColums.indexOf("yearweek")
    var yearno = seasonColums.indexOf("year")
    var weekno = seasonColums.indexOf("week")
    var qtyno = seasonColums.indexOf("qty")
    var map_priceno = seasonColums.indexOf("map_price")
    var irno = seasonColums.indexOf("ir")
    var pmapno = seasonColums.indexOf("pmap")
    var pmap10no = seasonColums.indexOf("pmap10")
    var pro_percentno = seasonColums.indexOf("pro_percent")
    var promotionCheck = seasonColums.indexOf("promotion_check")
    var movingAvg = seasonColums.indexOf("moving_avg")
    var seasonalityNo = seasonColums.indexOf("seasonality")

    var seasonRdd = seasonDf.rdd

    //데이터의 201627주차 기준 과거 8주내의 데이터가 존재하지 않는 경우 단종된 제품으로 처리

    // 단종로직 step1 => 201620이 있는 제품들만 추출
    var validYearWeek = "201620"
    var exceptionFiltered = seasonRdd.groupBy(x=>{
      (x.getString(regionidno),
        x.getString(productno2),
        x.getString(regionidno2),
        x.getString(regionidno3),
        x.getString(productno3))
    }).filter(x=>{
      var checkValid = false
      var key = x._1
      var data = x._2
      var yearWeek = data.map(x=>{x.getString(yearweekno)}).max

      if(yearWeek >= validYearWeek){
        checkValid = true
      }
      checkValid
    }).flatMap(x=>{
      var key = x._1
      var data = x._2
      var result = data.map(x=>{
        (x.getString(regionidno),
          x.getString(productno2),
          x.getString(regionidno2),
          x.getString(regionidno3),
          x.getString(productno3),
          x.getString(yearweekno),
          x.getString(yearno),
          x.getString(weekno),
          x.getString(qtyno),
          x.getString(map_priceno),
          x.getString(irno),
          x.getString(pmapno),
          x.getString(pmap10no),
          x.getString(pro_percentno),
          x.getString(promotionCheck),
          x.getString(movingAvg),
          x.getString(seasonalityNo))
      })
      result
    })

    var sortedData = exceptionFiltered.sortBy(x=>(x._1,x._2,x._3,x._4,x._5,x._6))

    var sortDf = sortedData.toDF("REGIONSEG1", "PRODUCTSEG2", "REGIONSEG2", "REGIONSEG3", "PRODUCTSEG3", "YEARWEEK", "YEAR", "WEEK", "QTY", "MAP_PRICE", "IR", "PMAP", "PMAP10", "PRO_PERCENT","PROMOTION_CHECK","MOVING_AVG","SEASONALITY")

    var forecastRdd = sortDf.rdd
    var season1Colums = sortDf.columns.map(x => {x.toLowerCase()})
    var regionidno0 = season1Colums.indexOf("regionseg1")
    var productno20 = season1Colums.indexOf("productseg2")
    var regionidno20 = season1Colums.indexOf("regionseg2")
    var regionidno30 = season1Colums.indexOf("regionseg3")
    var productno30 = season1Colums.indexOf("productseg3")
    var yearweekno0 = season1Colums.indexOf("yearweek")
    var yearno0 = season1Colums.indexOf("year")
    var weekno0 = season1Colums.indexOf("week")
    var qtyno0 = season1Colums.indexOf("qty")
    var map_priceno0 = season1Colums.indexOf("map_price")
    var irno0 = season1Colums.indexOf("ir")
    var pmapno0 = season1Colums.indexOf("pmap")
    var pmap10no0 = season1Colums.indexOf("pmap10")
    var pro_percentno0 = season1Colums.indexOf("pro_percent")
    var promotionCheck0 = season1Colums.indexOf("promotion_check")
    var movingAvg0 = season1Colums.indexOf("moving_avg")
    var seasonalityNo0 = season1Colums.indexOf("seasonality")

    //28,29,30,31주차를 예상하기 위해서 4개의 주차 예상해주는 로직 start!
    var outYearWeek = "201631"

    var maxYearWeek = forecastRdd.map(x=>{
      var yearweek = x.getString(yearweekno0).toInt
      yearweek
    }).max

    var maxWeek = forecastRdd.map(x=>{
      var week = x.getString(weekno0).toInt
      week
    }).max

    var forecastMap = forecastRdd.groupBy(x=>{
      (x.getString(regionidno0),
        x.getString(productno20),
        x.getString(regionidno20),
        x.getString(regionidno30),
        x.getString(productno30)
      )
    }).flatMap(x=>{
      var key = x._1
      var data = x._2

      //fcst구하기 201624,201625,201626,201627의 qty를 보두 합한 후 4로 나눈 값
      var filtered = data.filter(x=>{
        var yearweek = x.getString(yearweekno0).toInt
        maxYearWeek - yearweek <= 3
      })
      var size = 4
      var qtySum = if(filtered.size != 0){
        filtered.map(x=>{
          var qty = x.getString(qtyno0).toDouble
          qty
        }).sum
      }else{
        0d
      }
      var fcst = qtySum / size

      //201624,201625,201626,201627의 계절성지수 합에 4로 나누어서 시계열 계산에 들어감
      var seasonality = if(filtered.size != 0){
        filtered.map(x=>{
          var tempseason = x.getString(seasonalityNo0).toDouble
          tempseason
        }).sum
      }else{
        0d
      }
      var originAvgSeason = seasonality/size

      //201628,201629,201630,201631 행 추가
      var yearweekMin = data.map(x => x.getString(yearweekno0)).min
      var yearweek = data.map(x=>{x.getString(yearweekno0)}).toArray.sorted

      var i = 1
      var tempYearweek = Array(yearweekMin)
      while (tempYearweek.last < outYearWeek) {
        tempYearweek ++= Array(postWeek(yearweekMin.toString, i))
        i = i + 1
      }
      var conversionArray = tempYearweek.diff(yearweek)

      //이전 28,29,30,31주차 개별 seasonality 가져오기
      var weektemp = conversionArray.map(x=>{x.substring(4,6)})
      var temp = Array("28")
      var originWeek = data.map(x=>{x.getString(yearweekno0).substring(4,6)}).toArray.sorted

      var seasonaltyfiltered = data.filter(x=>{
        var check = false
        for (i <- temp) {
          if (originWeek.contains(i)) {
            check = true
          }
        }
        check
      })

      var seasonaltyValue = if(seasonaltyfiltered.size != 0){
        seasonaltyfiltered.map(x=>{
          var seasonality = x.getString(seasonalityNo0).toDouble
          seasonality
        }).sum
      }else{
        0d
      }
      var realseasonality = seasonaltyValue / size

      //시계열 구하기
      var fcst_timeseries = if (originAvgSeason != 0){
        ((fcst * seasonaltyValue) / originAvgSeason)
      }else{
        0d
      }

      val tmpMap = conversionArray.map(x => {
        val year = x.substring(0, 4)
        val week = x.substring(4, 6)
        val yearweek = year+week
        val qty = 0
        val map_price = 0
        val ir = 0
        val pmap = 0d
        val pmap10 = 0d
        val pro_percent = 0d
        val promotion_check = "N"
        val movingAVG =0d
        (key._1,
          key._2,
          key._3,
          key._4,
          key._5,
          yearweek,
          year,
          week,
          qty.toString,
          map_price.toString,
          ir.toString,
          pmap.toString,
          pmap10.toString,
          pro_percent.toString,
          promotion_check.toString,
          movingAVG.toString,
          realseasonality.toString,
          fcst.toString,
          fcst_timeseries.toString
        )
      })
      var result = data.map(x=>{
        var Allfcst = "0"
        var timeseies = "0"
        (x.getString(regionidno),
          x.getString(productno2),
          x.getString(regionidno2),
          x.getString(regionidno3),
          x.getString(productno3),
          x.getString(yearweekno),
          x.getString(yearno),
          x.getString(weekno),
          x.getString(qtyno),
          x.getString(map_priceno),
          x.getString(irno),
          x.getString(pmapno),
          x.getString(pmap10no),
          x.getString(pro_percentno),
          x.getString(promotionCheck),
          x.getString(movingAvg),
          x.getString(seasonalityNo),
          Allfcst,
          timeseies)
      })
      tmpMap ++ result
    })


  }
}
