# Tweet Sentiment Classifier
This repo is the final project introduction to big data course at Innopolis University.

The project is a Tweet Sentiment Classifier that will run on a Hadoop cluster for learning and predicting a live stream of tweets using Spark

## Description of the problem
The goal of this assignment is to perform Sentiment Analysis and define the emotional coloring of a stream of tweets using Apache Spark, Scala, and Machine Learning.

The Social Sentiment Analysis problem statement is to analyze the sentiment of social media content, like tweets. The algorithm takes a string, and returns the sentiment rating for the “positive” or “negative”.

## Method
In our project, we used grid search and cross-validation on 3 different ML models: Logistic Regression, Random Forst, and Leaniar SVC, using data provided by the course staff to train and test our model then streamed tweets for 24 hours and we got the following results:

| _Model_               | _Test F1 score_ | _Stream F1 score_ |
| --------------------- | --------------- | ----------------- |
| Logistic Regression   | 0.6893215310    |                   |
| Random Forst          | 0.5128763401    |                   |
| Linear SVC            | 0.6921739994    | 0.8242424242      | 
