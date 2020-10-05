import org.apache.spark.ml.tuning.CrossValidatorModel
import org.apache.spark.ml.PipelineModel
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}

object Stream {
    def main(args: Array[String]): Unit = {
        // Create a local StreamingContext with two working thread
        // and batch interval of 60 seconds.
        // The master requires 2 cores to prevent starvation
        val conf = new SparkConf()
            .setAppName("Stream")
        val ssc = new StreamingContext(conf, Seconds(60))

        // Create a DStream that will connect to hostname:port
        val lines = ssc.socketTextStream("10.90.138.32", 8989)

        // Load model
        val lr_model = CrossValidatorModel.load("lr-cv-model")
        val rf_model = PipelineModel.load("rf-model")

        lines.foreachRDD { (rdd: RDD[String], time: Time) =>
            // Get the singleton instance of SparkSession
            val spark = SparkSessionSingleton.getInstance(rdd.sparkContext.getConf)
            import spark.implicits._

            // Convert RDD[String] to DataFrame
            val linesDataFrame = rdd.map((time.toString(), _)).toDF("time", "text")

            // Make prediction
            val lr_predictions = lr_model.transform(linesDataFrame)
            val rf_predictions = rf_model.transform(linesDataFrame)

            // Write output
            lr_predictions.select("time", "text", "prediction")
                .write.mode("append").csv("lr-pred")
            rf_predictions.select("time", "text", "prediction")
                .write.mode("append").csv("rf-pred")
        }

        // Start computation
        ssc.start()
        // Wait for a day or the computation to terminate
        ssc.awaitTerminationOrTimeout(86400000)
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
