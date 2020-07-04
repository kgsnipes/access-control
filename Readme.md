# AccessControl API
Every application today that is built from scratch requires some type of user access control service to be developed or integrated. I have as a developer has always come across this necessity in all of my personal projects. This led me to create this library that has constitutes of a simple user service that takes care of all the user management and access control aspects for an application.

This need led me to develop this simple yet effective library which can fast-track development of your projects and let you focus on the business logic for the apps you build rather than worry about coding the user management aspects of you app.

The API is developed using Spring DI container, Spring JPA and a few utility libraries. The code is available as open source for anybody to customize as per needs with the base framework available. There is not much of a learning curve as it uses libraries that almost all JAVA developers are used to.

**Getting Started with this API is very easy**
```
import com.accesscontrol.services
// load up the API with the Spring Container
AccessControlService accessControlService=new DefaultAccessControlService();
//access the user service from the API
UserService userService= accessControlService.getUserService();

//set the CRUD Context
AccessControlContext ctx=new AccessControlContext("system-user",null);

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

This API is thoroughly tested and you can find the JUnit test cases in the project.

For more help on using the API explore the [cookbook](https://github.com/kgsnipes/access-control/wiki/Cookbook).


