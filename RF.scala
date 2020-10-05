import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.feature.{HashingTF, Tokenizer}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.sql.SparkSession

object RFModel {
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder.appName("RFModel").getOrCreate()
        import spark.implicits._

        // Prepare training documents
        val pos = spark.read.textFile("train/pos/").map((_, 0.0)).toDF("text", "label")
        val neg = spark.read.textFile("train/neg/").map((_, 1.0)).toDF("text", "label")
        val training = pos.unionByName(neg)

        // Configure an ML pipeline with 3 stages: tokenizer, hasingTF, rf
        val tokenizer = new Tokenizer()
            .setInputCol("text")
            .setOutputCol("words")
        val hashingTF = new HashingTF()
            .setInputCol(tokenizer.getOutputCol)
            .setOutputCol("features")
        val rf = new RandomForestClassifier()
            .setNumTrees(10)
        val pipeline = new Pipeline()
            .setStages(Array(tokenizer, hashingTF, rf))

        // Train model
        val model = pipeline.fit(training)

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
        val evaluator = new MulticlassClassificationEvaluator()
        val f1 = evaluator.evaluate(predictions);
        println(s"Random Forest Model's F1: $f1")
        spark.stop()
    }
}

