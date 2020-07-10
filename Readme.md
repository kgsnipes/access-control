# AccessControl API
Every JAVA application that is built from scratch requires some type of user access control service to be developed or integrated. I as a developer have always come across this necessity in my personal projects. This led me to creating this API that constitutes of a UserService that is simple to understand and use. It takes care of all the user management and access permisssion persistence and querying that an application might need.

It is a simple yet effective library which can fast-track development of your projects and let you focus on the business logic for the apps rather than worry about coding the user management and access permission CRUD operations of you app.

This API is developed using Spring Context for DI, Spring JPA and a few utility libraries from Apache foundation as well. This code is available for anybody to customize it as per needs with the base framework available. It is easy to get started with.

Extensive Unit testing is provided with 100+ unit tests.

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
- Configurable Change logs for any and every change that is made on the data. Helps with better auditing and change tracking.
- Hierarchial usergroups to allow inheritance of permissions to the Users.
- Easy integration with other Spring Applications as the API provides custom UserDetailsService and PasswordEncoder.
- Simple Flat Data Structure for ease of customization and easy of portability.
- Bulk import & export operations supported for all data models with CSV format.


**Getting Started with this API is very easy**
```

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



For more info on configuration and setup information explore the [cookbook](https://github.com/kgsnipes/access-control/wiki/Cookbook).



