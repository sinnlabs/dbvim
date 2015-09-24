[![Build Status](https://travis-ci.org/sinnlabs/dbvim.svg?branch=master)](https://travis-ci.org/sinnlabs/dbvim) 

[![Contribute](https://rawgit.com/slemeur/4a900bb68300a2643679/raw/1ad2c6d784c92fc21886c765bc6315a1f2ee690c/codenvy-contribute.svg)](https://codenvy.com/f?id=9guh1zr7gc0kuprd) 

#Database Visual Interface Maker
DBVIM allows you to quickly and easily create a web interface for any database that is supported by JDBC.

You create forms for each table without writing any code. Just use the web editor in a browser.

DBVIM supports create, modify, delete and search operations for the end user of the created forms.


##Using DBVIM

For each table in the database, you can create a visual representation of the table. Such representation is called a form.
You can create many forms for one table.

Step by step guide:

1. Add a connection to your database.
2. Explore the connection and select a table for which you want to create a form and click New form button.
3. Create the interface by dragging components from toolbox to the Model tree.
4. Save form.
5. End users can access the form by url appname/data/< formname >

####Default Users
Generic user:<br/>
Login: user
Password: user

Generic admin:<br/>
Login: admin
Password: admin
