[![Build Status](https://travis-ci.org/sinnlabs/dbvim.svg?branch=master)](https://travis-ci.org/sinnlabs/dbvim)

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
