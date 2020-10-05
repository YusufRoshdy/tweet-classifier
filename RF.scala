import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.feature.{HashingTF, Tokenizer, IDF}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.SparkSession

object RFModel {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder.appName("RFModel").getOrCreate()
        import spark.implicits._

        // Prepare dataset
        val data = spark.read.format("csv")
            .option("header", "true")
            .load(args(0))
            .withColumn("label", 'label cast DoubleType)
        val Array(training, test) = data.randomSplit(Array(0.7, 0.3))

        // Configure an ML pipeline with 4 stages: tokenizer, hasingTF, idf, rf
        val tokenizer = new Tokenizer()
            .setInputCol("text")
            .setOutputCol("words")
        val hashingTF = new HashingTF()
            .setInputCol(tokenizer.getOutputCol)
            .setOutputCol("rawFeatures")
        val idf = new IDF()
            .setInputCol(hashingTF.getOutputCol)
            .setOutputCol("features")
        val rf = new RandomForestClassifier()
            .setNumTrees(10)
        val pipeline = new Pipeline()
            .setStages(Array(tokenizer, hashingTF, idf, rf))

        // Construct a grid of parameter to search over
        val paramGrid = new ParamGridBuilder()
            .addGrid(hashingTF.numFeatures, Array(10, 100, 1000))
            .build()

        // Treat the Pipeline as an Estimator, wrapping it in a CrossValidator instance.
        val evaluator = new MulticlassClassificationEvaluator()
        val cv = new CrossValidator()
            .setEstimator(pipeline)
            .setEvaluator(evaluator)
            .setEstimatorParamMaps(paramGrid)
            .setNumFolds(5)
            .setParallelism(2)  // Evaluate up to 2 parameter settings in parallel

        // Run cross-validation, choose the best
        val model = cv.fit(training)

        // Save fitted pipeline to disk
        model.write.overwrite().save(args(1))

        // Load it back during production
        // val sameModel = CrossValidatorModel.load("cv-logistic-regression-model")

        // Make prediction on test
        val predictions = model.transform(test)
        predictions.select("text", "label", "prediction").write.csv(args(2))

        // Evaluate model
        val f1 = evaluator.evaluate(predictions);
        println(s"\nRandom Forest Model's F1: $f1\n")
        spark.stop()
    }
}

