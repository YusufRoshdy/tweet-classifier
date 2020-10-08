# IBD Assignment 2: Stream Processing with Spark

## Introduction

Team Members:
- Hussein Younes `BS18-DS-02`
- Trang Nguyen `BS18-DS-01`
- Utkarsh Kalra `BS18-DS-02`
- Yusuf Mesbah `BS18-DS-02`

This project is a *Tweet Sentiment Classifier*, which will run on Innopolis Hadoop cluster for learning and predicting a live stream of tweets using Spark. It is the final project for *Introduction to Big Data* course at Innopolis University. The goal of the project is to perform sentiment analysis and define the emotional coloring of a stream of tweets using Apache Spark, Scala, and Machine Learning.

The GitHub repository for our project is available at [Tweet Sentiment Classifier](https://github.com/YusufRoshdy/tweet-classifier)

## Architecture

### Building classifier

#### Selecting Datasets
Three different datasets were tried and the one provided in the server with 100000 entries (in which 56457 entries are labeled Positive and 43532 are labeled Negative), was the best in distribution and susequently was used for building the model.

![](https://i.imgur.com/w92Goce.png)


#### Building model
After go through preprocessing of removing repetitive characters, punctuations, and trailing whitespaces, the data is splitted into two sets of train and test with ratio of 70:30. Then cross-Validation and grid-search are used to train the model with a pipeline for feature extraction and the spacified model, then tested on the test set to evaluate the model performance. 
![Model](https://i.imgur.com/k7DwNWu.png)

### Feeding the Stream to the Model
After the model is trained, a strem of tweets passed in the same preprocessing used in training to get the output
![Stream](https://i.imgur.com/RnoMbVu.png)


## Method
In our project, we used grid search and cross-validation on 3 different ML models: 
* Logistic Regression
* Random Forest 
* Linear SVC

### Preprocessing Steps
* Removing Repetative words
* Removing Puctuations
* Removing Trailing white spaces
### Classification Models
* Logistic Regression

  Logistic regression is the appropriate regression analysis to conduct when the dependent variable is dichotomous (binary). Logistic regression is used to describe data and to explain the relationship between one dependent binary variable and one or more nominal, ordinal, interval or ratio-level independent variables.

* Random Forest

    The random forest is a classification algorithm consisting of many decisions trees. It uses bagging and feature randomness when building each individual tree to try to create an uncorrelated forest of trees whose prediction by committee is more accurate than that of any individual tree
    
* Linear SVC
    The objective of a Linear SVC (Support Vector Classifier) is to fit to the data you provide, returning a "best fit" hyperplane that divides, or categorizes, your data. From there, after getting the hyperplane, you can then feed some features to your classifier to see what the "predicted" class is.
### Hyperparameters Tuning
The method used for hyperparameters selection is **Cross Validation**.
It selectes the best model and hyperparameters according to the results and uses that.
Basically it makes multiple sets for training and testing from the given data and the given train test split ratio, trains a model on all and takes the best result.
## Testing

### Procedure

First we need to train model on the dataset using following command:
```
spark-submit --master yarn --class <CLASS_NAME> <JAR_FILE_NAME>.jar <INPUT_DATA> <SAVED_MODEL_NAME> <MODEL_OUTPUT>
```
**Parameters**
- `<CLASS_NAME>`: class name aka model name (`LRModel`, `RFModel`, `SVCModel`).
- `<JAR_FILE_NAME>`: name of the compiled binary.
- `<INPUT_DATA>`: name of the csv input file with headers `text`, `label`.
- `<SAVED_MODEL_NAME>`: name of folder to save trained model.
- `<MODEL_OUTPUT>`: name of folder to save the prediction output of the model on test set.


After that, we stream the data to the pre-trained model as follows,
```
spark-submit --master yarn --class <CLASS_NAME> <JAR_FILE_NAME>.jar <SAVED_MODEL_NAME> <TIME> <MODEL_STREAM_OUTPUT>
```

**Parameters**
- `<CLASS_NAME>`: class name (`Stream`).
- `<JAR_FILE_NAME>`: name of the compiled binary.
- `<SAVED_MODEL_NAME>`: name of the pretrained model that will classify the streamed data.
- `<TIME>`: for how long we will monitor the twitter stream (in Milliseconds).
- `<MODEL_STREAM_OUTPUT>`: name of folder that will store the prediction output of the model.

In our case, the commands for submitting jobs were as follows

```
spark-submit --master yarn --class LRModel p2-v3-with-preprocessing.jar data.csv model model-output
spark-submit --master yarn --class RFModel p2-v3-with-preprocessing.jar data.csv model model-output
spark-submit --master yarn --class SVCModel p2-v3-with-preprocessing.jar data.csv model model-output
spark-submit --master yarn --class Stream p2-v3-with-preprocessing.jar model time model-stream-output
```


**NOTE:** There is a README file on the cluster that descibes the process of submitting a job on the cluster.

### Result

The streamed data predicted from the model were manually labeled* then compaired to the model predictions
<p style="font-size:10px;">*Note: only the output of SVC were fully hand labeled, for random forest and logistic regression only 100 label were manualy done just to estimate there performance</p>

| Model               | Test F1 score | Stream F1 score | Precission | Recall | Accuracy |
| ------------------- | ------------- | --------------- | ---------- | ------ | -------- |
| Logistic Regression | 0.7065        | 0.8391          | 0.8695     | 0.8108 | 0.7722   |
| Random Forest       | 0.5293        | 0.8104          | 0.7380     | 0.8985 | 0.7128   |
| Linear SVC          | 0.7093        | 0.8423          | 0.8632     | 0.8224 | 0.7766   |






* This Pie chart shows the true or the hand labeled values of the tweets, in which 27% are Negative and 73% are Positive
    ![](https://i.imgur.com/HMq37mB.png)

* This Pie chart shows the predictions of our model and it classifies the stream into 35% Negative and 65% Positive.
    ![](https://i.imgur.com/Jqmq3l9.png)

### Output Example

An example from the output is:

`1601942280000 ms,"\"@bombalicous I'll blog about you in exchange for some cupcakes.    They would make great baby shower favors",1.0`

This is taken from the csv file of the output and first it shows the timestamp of the tweet then the Tweet itself and then after the "quotes" there is a comma(",") and then the prediction of the tweet. 
**1** means happy or neutral and **0** means sad.
In this case, its 1 that is happy or neutral.


## Contributions

The project was divided in 4 phases, all members actively participated. Following graph demonstrates the contributions (names are placed in alphabet order).

![](https://i.imgur.com/PZu1TrY.png)


## Conclusion and Further Improvements

We have built a model that classifies data coming from a stream of tweets, the tried three models: `Logistic Regression`, `Random Forest`, and `Linear SVC`. The models were first trained on the given data set, the model was validated using Grid Search cross validation, and tested using the test data (a subset of the data set).

The results were analyzed and the confusion matrix was calculated to get the `accuracy score`, `F1-score`, `precistion`, and `recall`.


A lot of rooms for improvement exist for this project, such as:

* Updating the time in the streaming output to a human-readable format.
* Extracting more features from input, such as N-Grams.
* Using a model with RNN to get a higher accuracy.

## Resources used / References
* https://spark.apache.org/docs/latest/streaming-programming-guide.html#a-quick-example
* https://spark.apache.org/docs/latest/ml-pipeline.html#example-pipeline
* https://spark.apache.org/docs/latest/ml-tuning.html#cross-validation
* https://app.diagrams.net/
* https://hackmd.io/
