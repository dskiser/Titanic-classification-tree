# Titanic Classification Tree

This is an attempt to create a classification tree in Java that can be used to predict the passengers who survived the sinking of the Titanic. The training data set used is from Kaggle.com.

TreeDemo.java grows the tree.  It requires the file train.csv https://www.kaggle.com/c/titanic/data, with the header removed.

TestTreeDemo.java uses the model built by TreeDemo.java to classify new observations.  It requires the file test.csv from the same site on Kaggle (also with the header removed).  To run TestTreeDemo, use the model as the first argument and the submission file as the second argument.

