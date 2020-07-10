# AccessControl API
Every JAVA application that is built from scratch requires some type of user access control service to be developed or integrated. I have as a developer has always come across this necessity in all of my personal projects. This led me to create this library that has constitutes of a simple user service that takes care of all the user management and access control aspects for an application.

This need led me to develop this simple yet effective library which can fast-track development of your projects and let you focus on the business logic for the apps you build rather than worry about coding the user management aspects of you app.

The API is developed using Spring DI container, Spring JPA and a few utility libraries. The code is available as open source for anybody to customize as per needs with the base framework available. There is not much of a learning curve as it uses libraries that almost all JAVA developers are used to.

Extensive code coverage provided with 100+ unit tests.

**Feature List**

- Password encryption with configurable SALT and PEPPER.
- User Credentials Encryption is configurable supporting the below algorithms
    - MD2
    - MD5
    - SHA-1
    - SHA-224
    - SHA-256
    - SHA-384
    - SHA-512
    - SHA-512/224
    - SHA-512/256
    - SHA3-256
    - SHA3-384
    - SHA3-512
- Configurable Change logs for any and every change that is made on the data. Helps with better auditing.
- Hierarchial usergroups.
- Easy integration with Spring Security. The API provides custom UserDetailsService and PasswordEncoder.
- Simple Flat Data Structure for ease of customization and easy of portability.
- Bulk import & export operations supported for all data models with CSV format.**TODO**
- Reporting of the changelog for auditing purposes CSV and PDF formats supported.**TODO**


**Getting Started with this API is very easy**
```

//Download the JAR  access-control-1.0-SNAPSHOT-jar-with-dependencies.jar under the lib(create if not present) folder in your Maven Project

//Configure the POM dependency for Maven Projects

<dependency>
    <groupId>accesscontrol</groupId>
    <artifactId>access-control</artifactId>
    <scope>system</scope>
    <version>1.0</version>
    <systemPath>${basedir}/lib/access-control-1.0-SNAPSHOT-jar-with-dependencies.jar</systemPath>
</dependency>

//Start Coding

import com.accesscontrol.services
// load up the API with the Spring Container
AccessControlService accessControlService=new DefaultAccessControlService();
//access the user service from the API
UserService userService= accessControlService.getUserService();

//set the CRUD Context
AccessControlContext ctx=new AccessControlContext("system-user","system running tests");

//Create a user model
User user=new User();
user.setPassword("123456");
user.setEnabled(true);
user.setFirstName("test");
user.setLastName("user");
user.setUserId("testuser1@test.com");
//persist the user
User persistedUser=userService.createUser(user,ctx);

//fetch the user
User user=userService.getUserById("testuser5@test.com");

//delete user
userService.deleteUser("testuser3300@test.com",ctx);

//enable the user
String userId="testuser2@test.com";
userService.enableUser(userId,ctx);

//disable the user
String userId="testuser2@test.com";
userService.disableUser(userId,ctx);

//search for the users
PageResult<User> userResults=userService.findUsers("test",1);

//create a usergroup
UserGroup userGroup=new UserGroup();
userGroup.setCode("admingroup");
userGroup.setName("Admin Group");
userGroup.setEnabled(true);
//persist the usergroup
userService.createUserGroup(userGroup,ctx)

```



For more help on using the API explore the [cookbook](https://github.com/kgsnipes/access-control/wiki/Cookbook).



