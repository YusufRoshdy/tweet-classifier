# Tweet Sentiment Classifier
This repo is the final project introduction to big data course at Innopolis University.

The project is a Tweet Sentiment Classifier that will run on a Hadoop cluster for learning and predicting a live stream of tweets using Spark

## Description of the problem
The goal of this assignment is to perform Sentiment Analysis and define the emotional coloring of a stream of tweets using Apache Spark, Scala, and Machine Learning.

The Social Sentiment Analysis problem statement is to analyze the sentiment of social media content, like tweets. The algorithm takes a string, and returns the sentiment rating for the “positive” or “negative”.

## Method
In our project, we used grid search and cross-validation on 3 different ML models: Logistic Regression, Random Forst, and Leaniar SVC, using data provided by the course staff to train and test our model then streamed tweets for 24 hours and we got the following results:
 Model | Test F1 score | Stream F1 score | Precission|Recall | Accuracy |
| -------- | -------- | -------- | -------- | -------- | ------ |
| Logistic Reression    | 0.7065     | text     | text | text | text |
| Random Forest    | 0.5293     | Text     | text | text | text |
| Linear SVC   | 0.7093    | 0.8242   | 0.8717 | 0.7816 |  0.7576 |

