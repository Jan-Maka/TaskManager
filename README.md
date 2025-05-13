# Task Manager
Final year Bachelor of Software Engineering Project that was designed and created by me.


## Technology Stack
- Language: Java 17
- Build Tool: Gradle
- Framework: Spring Boot 3.0.2 https://spring.io/projects/spring-boot
- Template Engine: ThymeLeaf https://www.thymeleaf.org/
- Frontend: HTML/CSS
    - HTML
    - CSS
    - JavaScript
    - Bootstrap 5.3.0 Alpha https://getbootstrap.com/docs/5.3/getting-started/introduction/
  - JQuery 3.6.4 https://jquery.com

- Database: MySQL

## Running the Application
This Application is made using SpringMVC and a MySQL databse.

### Requisites
---
- Install Java 17+
- Install MySQL
- PayPal Developer Account

### Development User Account Login Information
---
There are two user accounts which have different roles to represent the differnce between subscribed users and regular users.
#### Member User
- Email: johnny@joestar.com
- Password: password

#### Regular User
- Email: jan@pawel.com
- Password: password

### Database Setup
---
The application requires a connection to an MySQL database as without it you won't be able to use the application.

#### Create Database
---
Create a databse that your willing to by changing {database} to the database name you want and the {username} and {password} to what you want.
```sql
CREATE DATABASE '{database}';
USE '{database}'
CREATE USER '{username}'@'localhost' IDENTIFIED BY '{password}';
GRANT ALL PRIVILEGES ON *.* TO '{username}'@'localhost';
QUIT;
```

#### Connect to Database
---
To connect the application with thedatabase you can replace the {database} with one you have created and the {username} and {password} for the databse in the application.properties file of the project (Located: [TaskManager/src/main/resources](./TaskManager/src/main/resources/application.properties)).

```yaml
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/{database}
spring.datasource.username={username}
spring.datasource.password={password}
```

### PayPal API Setup
---
**NOTE: This project is not configured to handle real world transactions only testing using PayPal sanbox.**


If you have a PayPal account the login into [PayPal Developer](https://developer.paypal.com/home) and select sandbox mode (For testing) and go to [Apps & Redentials](https://developer.paypal.com/dashboard/applications/sandbox) and create an application. Once this is done you'll see that it has a client Id and a secret which will be needed for the configuration from the app you created.

Once you've acquired these details in the application.properties file (Located: [TaskManager/src/main/resources](./TaskManager/src/main/resources/application.properties)) add the client-id and secret. The mode is set to sandbox which is used only for testing with fake money.

```yaml
paypal.client-id= {client-id}
paypal.client-secret= {secret}
paypal.mode= sandbox
```

In order to test payments, PayPal sandbox creates two accounts one for business (where money is recieved) and a personal account (the account used to make payments) which can be found [here](https://developer.paypal.com/dashboard/accounts).

Whever a transaction is initaited you would login with the personal account which makes a payment to the business account.

To view if payment has been successful you can login into either account [here](https://www.sandbox.paypal.com/uk/home).



### Deployment of Application
---
**Note it might take a minute for the application to fully start up due to dependicies needing to be configured.**

#### Deploy in IDE
---
If you have a Java Development Environment setup with IntelliJ and you configured the MySQL databse then you can simply load the project using IntelliJ.

#### Deploy Application on Windows
---
In your command line change the directory to the project root folder ([TaskManager](./TaskManager/)) and run the command.
```
gradlew.bat clean bootjar
```
Then change the directory to access the jar file and type the following which would start running the application on your device.
```
cd build\libs
java -jar TaskManager-0.0.1-SNAPSHOT.jar
```
To see if it works go to your browser and vist <https://localhost:8443/login> to arrive at the login page.

#### Deploy Application on Linux and macOS
---
In your command line change the directory to the project root folder ([TaskManager](./TaskManager/)) and run the command.
```
./gradlew clean bootJar
```
Then change the directly to access the jar file and type the following which would start running th application on your device.

```
cd build/libs
java -jar TaskManager-0.0.1-SNAPSHOT.jar
```
To see if it works go to your browser and vist <https://localhost:8443/login> to arrive at the login page.
