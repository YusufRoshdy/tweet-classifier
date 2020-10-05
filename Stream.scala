import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
//import java.text.SimpleDateFormat

object Stream {
    def main(args: Array[String]): Unit = {
        // Create a local StreamingContext with two working thread
        // and batch interval of 120 seconds.
        // The master requires 2 cores to prevent starvation
        val conf = new SparkConf()
            .setAppName("Stream")
        val ssc = new StreamingContext(conf, Seconds(120))

        // Create a DStream that will connect to hostname:port
        val lines = ssc.socketTextStream("10.90.138.32", 8989)

        // Load model
        val model = CrossValidatorModel.load(args(0))
        // val rf_model = CrossValidatorModel.load("rf-model")

        lines.foreachRDD { (rdd: RDD[String], time: Time) =>

            //val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

            // Get the singleton instance of SparkSession
            val spark = SparkSessionSingleton.getInstance(rdd.sparkContext.getConf)
            import spark.implicits._

            // Convert RDD[String] to DataFrame
            val linesDataFrame = rdd.map((time.toString(), _)).toDF("time", "text")

            // Make prediction
            val predictions = model.transform(linesDataFrame)
            // val rf_predictions = rf_model.transform(linesDataFrame)

            // Write output
            predictions.select("time", "text", "prediction")
                .write.mode("append").csv(args(2))
            // rf_predictions.select("time", "text", "prediction")
            //    .write.mode("append").csv("rf-pred")
        }

        // Start computation
        ssc.start()
        // Wait for a day or the computation to terminate
        ssc.awaitTerminationOrTimeout(args(1).toLong)
        // ssc.awaitTerminationOrTimeout(180000)
    }
}

/** Lazily instantiated singleton instance of SparkSession */
object SparkSessionSingleton {

    @transient  private var instance: SparkSession = _

    def getInstance(sparkConf: SparkConf): SparkSession = {
        if (instance == null) {
            instance = SparkSession
                .builder
                .config(sparkConf)
                .getOrCreate()
        }
        instance
    }
}
