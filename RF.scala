import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.feature.{HashingTF, Tokenizer, IDF}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.tuning.{CrossValidator, ParamGridBuilder}
import org.apache.spark.sql.SparkSession

object RFModel {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder.appName("RFModel").getOrCreate()
        import spark.implicits._

        // Prepare training documents
        val pos = spark.read.textFile("train/pos/").map((_, 0.0)).toDF("text", "label")
        val neg = spark.read.textFile("train/neg/").map((_, 1.0)).toDF("text", "label")
        val training = pos.unionByName(neg)

        // Configure an ML pipeline with 3 stages: tokenizer, hasingTF, idf, rf
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
        model.write.overwrite().save("rf-model")

        // Load it back during production
        // val sameModel = CrossValidatorModel.load("cv-logistic-regression-model")

        // Prepare test documents
        val test_pos = spark.read.textFile("test/pos/").map((_, 0.0)).toDF("text", "label")
        val test_neg = spark.read.textFile("test/neg/").map((_, 1.0)).toDF("text", "label")
        val test = test_pos.unionByName(test_neg)

        // Make prediction on test
        val predictions = model.transform(test)
        predictions.select("text", "label", "prediction").write.csv("rf-output")

        // Evaluate model
        val f1 = evaluator.evaluate(predictions);
        println(s"\nRandom Forest Model's F1: $f1\n")
        spark.stop()
    }
}

