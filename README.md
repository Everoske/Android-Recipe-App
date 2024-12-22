## Android Recipe App

### Overview
This is an Android application that allows users to create recipes, view the recipes other people have created, and download and share recipes. It uses Firebase database to store recipe objects and images. User accounts are handled through Google Authentication. This project was built in 2022 by Elise Kidroske and work on it is discontinued. 
The application can still be downloaded and used and the code is free to clone should anyone wish to work on this application. As of now, the application allows users to create and sign into accounts, create recipe objects, view recipe objects created by other users, copy recipes from other users, and edit recipes.

### Scope of Project
- Users can create accounts to view and create recipes.
- Users can create recipes with steps and ingredients.
- Users can upload images to represent their recipes.
- Users can view recipes created by other users.
- Users can save a copy of a recipe created by another user to their account.
- Copies of existing recipes are marked as private and are not viewable by other users.
- Users can edit and delete recipes.
- Users can view other users' profiles.
- User profiles contain a list of the recipes they have created not marked as private.

### Project Structure
- **Downloadable APK:** /app/recipe-app-demo.apk
- **Main Project Contents:** /app/src/main
- **Java Files:** /app/src/main/java/com/everon/recipeapp/
- **Activity Controller Files:** /app/src/main/java/com/everon/recipeapp/
- **Recycler View Adapter Files:** /app/src/main/java/com/everon/recipeapp/adapters/
- **Data Modeling Files:** /app/src/main/java/com/everon/recipeapp/data/
- **Navigation Bar Files:** /app/src/main/java/com/everon/recipeapp/nav/
- **Activity Layout XML Files:** /app/src/main/res/layout/
- **String, style, color, and theme values:** app/src/main/res/values/
- **Images Used in Project:** app/src/main/res/drawable
- **Navigation Resource File:** app/src/main/res/menu/navigation_items.xml
- **Android Manifest File:** app/src/main/AndroidManifest.xml

### How to Install APK
- 1: Download the recipe-app-demo.apk file located at /app/recipe-app-demo.apk and store locally on your machine.
- 2: Enable developer mode on your Android device if you have not done so already.
- 3: Enable "Install Unknown Apps" on your device.
- 4: Connect your device to your pc and transfer it to your downloads directory on your device.
- 5: Locate and file on your device and install it.

### How to Work with Project
To work with this project, you will need to create your own Firebase project using the Firebase console. You will need to create your own Authentication service, Firebase database, and Firebase storage. 
You will need to configure credentials for working with the SDK and install the appropriate google-services.json file. 

### Features Not Implemented
- Users can subscribe to other users and get alerts when those users create new recipes.
- Users can like recipes.
- Users can comment on recipes.
- Users can search for recipes.
