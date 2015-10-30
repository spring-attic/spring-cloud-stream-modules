DROP TABLE IF EXISTS test.book;
CREATE TABLE test.book  (
	isbn    	uuid PRIMARY KEY,
	author  	text,
	instock 	boolean,
	pages   	int,
	saledate	timestamp,
	title   	text
);

