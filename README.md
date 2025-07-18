# customer-chat-demo
 proof-of-concept real-time customer support chat application built with Angular 18 (frontend) and Spring Boot 3 (backend) using WebSocket (STOMP protocol).

## 1. Installation Guide

Clone the repository by:

`git clone https://github.com/Xinhe-Yu/customer-chat-demo`

or

`gh repo clone Xinhe-Yu/customer-chat-demo`

### Back-end
Ensure that you have :

- Java 17 (check it with `java -version`)
- Apache Maven 3.x.x (check it with `mvn -v`)
- PostgreSQL (version 16 or higher is recommended, please also check if it is running and accessible, `sudo systemctl status postgresql` for Linux, `brew services list | grep postgresql` for macOS brew.)

Get into the back-end folder by:

`cd back`

1. Install Dependencies with Maven

`mvn clean install -DskipTests`

2. Configure environment variables

Locate the `env_template` file. Make a copy in the same folder and rename it `.env`. Add your PostgreSQL root's username and password, and a Encryption Key for encrypting and decrypting the JWT.

3. Initialize the Database Schema With PostgreSQL installed and running, load the initial database schema:

`sudo -u postgres psql`

Then inside the prompt:

`CREATE DATABASE ycyw;`
`\q`

Now execute the SQL file:
`sudo -u postgres psql -d ycyw -f src/main/static/templates/schema.sql`

4. (Optional) If you want to populate your database with initial data, you can use the seeding feature:
`sudo -u postgres psql -d ycyw -f src/main/static/templates/seeds.sql`

You can set your own password by uncomment `CommandLineRunner` Bean in main method, and get the encoded string from terminal.

5. Run the application:

`mvn spring-boot:run`

The application will start on the port 8001 (localhost). should see output in the terminal indicating the application has started successfully.
After starting the application, you can view the API documentation at:

API Documentation: http://localhost:8001/api/swagger-ui/index.html
