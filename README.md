XML 2 JSON conversion in Clojure. A Java-usable jar is available.

This is my first clojure project. I was curious to try storing and searching XML documents in  a Json database. 
MongoDB, as well as other NoSQL databases, are comfortable with Json while not supporting XML at all. 
A well-defined conversion 
between XML and Json would turn any Json database to XML database.

Some conventions are needed for this conversion. The rules I found at 
http://www.xml.com/pub/a/2006/05/31/converting-between-xml-and-json.html 
make sense, except the last one. I copied the rules here:
 
    1. <e/>                        <--->  "e" : null
    2.	<e>text</e>	                <--->  "e" : "text"	
    3.	<e name="value" />	         <--->  "e" : {"@name": "value"}
    4.	<e name="value">text</e>	   <--->  "e"  : { "@name": "value", "#text": "text" }	
    5.	<e> <a>text</a> <b>text</b> </e>	<---> "e" : { "a": "text", "b": "text" }	
    6.	<e> <a>text</a> <a>text</a> </e>	<---> "e": { "a": ["text", "text"] }	
    7.	<e> text <a>text</a> </e>	"e" <---???---> { "#text": "text", "a": "text" }

As an example, the following:
    <person>
     <name>Joe</name>
     <address>
      <street>Main Street</street>
      <city>Atlanta</city>
     </address>
     <id type="passport">34234234324</id>
     <hobbies>
      <hobby>books</hobby>
      <hobby>tv</hobby>
     </hobbies>
    </person>
Becomes:

    {"person": {"hobbies":
     {"hobby":["books","tv"]},
     "id":{"#text":"34234234324","@type":"passport"},
     "address":{"street":"Main Street","city":"Atlanta"},
     "name":"Joe"}}


I've written some tests for each rule, see test/core.clj. Basic tests run OK, no extensive testing has taken place yet. Use at your own risk.

The Clojure code is self-explajing. The Java class supports the following two methods:
  
    public static String x2j(String xml)
    public static String j2x(String jsonContainingOneElement)
    public static String j2x(String jsonContainingManyElements, String elementName)

I still have to figure out how to make the jar available.


