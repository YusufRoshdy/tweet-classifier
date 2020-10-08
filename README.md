# IBD Assignment 2: Stream Processing with Spark

## Introduction
Team: `coonhound`

Members:
- Hussein Younes `BS18-DS-02`
- Trang Nguyen `BS18-DS-01`
- Utkarsh Kalra `BS18-DS-02`
- Yusuf Mesbah `BS18-DS-02`

This project is a *Tweet Sentiment Classifier*, which will run on Innopolis Hadoop cluster for learning and predicting a live stream of tweets using Spark. It is the final project for *Introduction to Big Data* course at Innopolis University. The goal of the project is to perform sentiment analysis and define the emotional coloring of a stream of tweets using Apache Spark, Scala, and Machine Learning.

GitHub repository for the project is available at [Tweet Sentiment Classifier](https://github.com/YusufRoshdy/tweet-classifier).

## Architecture

### Building the Classifier

#### Selecting Datasets
Three different datasets ([Large Movie Review Dataset](http://ai.stanford.edu/~amaas/data/sentiment/), [setiment140](https://www.kaggle.com/kazanova/sentiment140), and [the one that was provided in the cluster](https://github.com/YusufRoshdy/tweet-classifier/blob/main/input%2C%20output%20and%20labeled%20files%20CSV/data.csv)) were tried, the one provided in the cluster gave the best results (based on the `F1-score`), therefore it was the used for training our model, it has 100000 entries (from which 56457 entries are labeled 1 - Positive and 43532 are labeled 0 - Negative).

![](https://i.imgur.com/w92Goce.png)


#### Building the Model
After preprocessing the data by removing repetitive characters, punctuations, and trailing whitespaces, the data was split into two sets of train and test with a ratio of 70:30. Then cross-Validation and grid-search were used to train the model with a pipeline of feature extraction and the specified model, then tested on the test set to evaluate the model's performance.

The diagram below describes the architecture of training the model.

![Model](https://i.imgur.com/k7DwNWu.png)

### Feeding the Stream to the Model
After the model was trained, it was used to classify the stream of tweets which was preprocessed with the same technique as mentioned previously.

The diagram below describes the process of streaming the data to the model.

![Stream](https://i.imgur.com/RnoMbVu.png)


## Method

In our project, we used grid search and cross-validation on 3 different ML models: 

* Logistic Regression
* Random Forest 
* Linear SVC

### Preprocessing Steps
* Removing Repetitive words
* Removing Punctuations
* Removing Trailing white spaces
### Classification Models
* Logistic Regression

  Logistic regression is the appropriate regression analysis to conduct when the dependent variable is dichotomous (binary). Logistic regression is used to describe data and to explain the relationship between one dependent binary variable and one or more nominal, ordinal, interval, or ratio-level independent variables.

* Random Forest

    The random forest is a classification algorithm consisting of many decision trees. It uses bagging and feature randomness when building each individual tree to try to create an uncorrelated forest of trees whose prediction by committee is more accurate than that of any individual tree.
    
* Linear SVC

    The objective of a Linear SVC (Support Vector Classifier) is to fit the data we provide, returning a "best fit" hyperplane that divides or categorizes our data. From there, after getting the hyperplane, we can then feed some features to our classifier to see what the "predicted" class is.
### Hyperparameters Tuning
The method used for hyperparameters tuning is **Cross-Validation** and **Grid Search**.
It selects the best model and the best set of hyperparameters according to the validation results.

## Testing

### Procedure

First we need to train model on the dataset using following command:
```
spark-submit --master yarn --class <CLASS_NAME> <JAR_FILE_NAME>.jar <INPUT_DATA> <SAVED_MODEL_NAME> <MODEL_OUTPUT>
```
**Parameters**
- `<CLASS_NAME>`: class name aka model name (`LRModel`, `RFModel`, `SVCModel`).
- `<JAR_FILE_NAME>`: name of the compiled binary.
- `<INPUT_DATA>`: name of the CSV input file with headers `text`, `label`.
- `<SAVED_MODEL_NAME>`: name of the folder to save the trained model.
- `<MODEL_OUTPUT>`: name of the folder to save the prediction output of the model on the test set.


After that, we stream the data to the pre-trained model as follows:
```
spark-submit --master yarn --class <CLASS_NAME> <JAR_FILE_NAME>.jar <SAVED_MODEL_NAME> <TIME> <MODEL_STREAM_OUTPUT>
```

**Parameters**
- `<CLASS_NAME>`: class name (`Stream`).
- `<JAR_FILE_NAME>`: name of the compiled binary.
- `<SAVED_MODEL_NAME>`: name of the pre-trained model that will classify the streamed data.
- `<TIME>`: for how long we will monitor the twitter stream (in Milliseconds).
- `<MODEL_STREAM_OUTPUT>`: name of the folder that will store the prediction output of the model.

In our case, the commands for submitting jobs were as follows:

```
spark-submit --master yarn --class LRModel stream.jar data.csv model model-output
spark-submit --master yarn --class RFModel stream.jar data.csv model model-output
spark-submit --master yarn --class SVCModel stream.jar data.csv model model-output
spark-submit --master yarn --class Stream stream.jar model time model-stream-output
```

**NOTE:**
* There is a README file in `coonhound`'s home directory giving information on submitting a job on the cluster.
* Full outputs, trained models are stored in `coonhound` group folder in HDFS (`svc`, `lr`, `rf`). Training dataset `data.csv` can also be found there.

Streaming data after that is labeled manually to evaluate models' performance.

### Output Example

An example from the output is:

`1601942280000 ms,"\"@bombalicous I'll blog about you in exchange for some cupcakes.    They would make great baby shower favors",1.0`

This is taken from the csv file of the output and it shows the timestamp of the tweet, the tweet itself, and the prediction of the tweet (**1** means happy or neutral and **0** means sad).
In this case, the tweet is correctly predicted **1** - happy or neutral.

### Evaluation

The streamed data predicted from the model were manually labeled then compaired to the model's predictions.

**Note:** only the output of SVC were fully hand labeled, for random forest and logistic regression only 100 label were manualy done just to estimate there performance.

The following table shows the results analysis:
| Model               | Test F1 score | Stream F1 score | Precission | Recall | Accuracy |
| ------------------- | ------------- | --------------- | ---------- | ------ | -------- |
| Logistic Regression | 0.7065        | 0.8391          | 0.8695     | 0.8108 | 0.7722   |
| Random Forest       | 0.5293        | 0.8104          | 0.7380     | 0.8985 | 0.7128   |
| Linear SVC          | 0.7093        | 0.8423          | 0.8632     | 0.8224 | 0.7766   |


* This Pie chart below shows the true or the hand labeled values of the tweets, in which 27% are Negative and 73% are Positive.

![](https://i.imgur.com/HMq37mB.png)

* This Pie chart below shows the predictions of our model and it classifies the stream into 35% Negative and 65% Positive.

![](https://i.imgur.com/Jqmq3l9.png)


## Contributions

The project was divided in 4 phases, all members actively participated. The following graph demonstrates the contributions (names are placed in alphabetical order).

![](https://i.imgur.com/PZu1TrY.png)


## Conclusion and Further Improvements

We have built a model that classifies data coming from a stream of tweets, we tried three models: `Logistic Regression`, `Random Forest`, and `Linear SVC`. The models were first trained on the chosen dataset, the models were validated using Grid Search and cross validation, and tested using the test data (a subset of the data set).

The results were analyzed and the confusion matrix was calculated to get the `accuracy score`, `F1-score`, `precistion`, and `recall`.


A lot of room for improvement exists for this project, such as:

* Updating the time in the streaming output to a human-readable format.
* Extracting more features from input, such as N-Grams.
* Using a model with RNN to get a higher accuracy.

## Resources used / References
* https://spark.apache.org/docs/latest/streaming-programming-guide.html#a-quick-example
* https://spark.apache.org/docs/latest/ml-pipeline.html#example-pipeline
* https://spark.apache.org/docs/latest/ml-tuning.html#cross-validation
* https://app.diagrams.net/
* https://hackmd.io/