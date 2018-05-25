test database

 - bringup db with
      $ docker-compose up db
 - connect to create db and user
      $ mysql -u root -proot -h 127.0.0.1 -P3301
	create database gotztest;
	CREATE USER 'foo'@'localhost' IDENTIFIED BY 'bar';
	GRANT ALL PRIVILEGES ON gotz.* TO 'foo'@'localhost';
	CREATE USER 'foo'@'%' IDENTIFIED BY 'bar';
	GRANT ALL PRIVILEGES ON gotz.* TO 'foo'@'%';
 - bring down db
      $ docker-compose down

