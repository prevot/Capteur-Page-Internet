package prevot

 import java.util.regex.Pattern
 import java.util.regex.Matcher
 
def chaine

println(Pattern.matches("[a-z]* Wikibooks","Test regex Java pour Wikibooks francophone."))
println(Pattern.matches("a.*","a Quand le ciel bas et lourd"))

println Pattern.compile(".*Wikibooks.*").matcher("Test regex Java pour Wikibooks francophone.").matches() 
println Pattern.compile("a.*").matcher("a Quand le ciel bas et lourd").matches()

println(Pattern.matches("[\\w.-]+@[\\w]+\\.\\w{3,6}","toto-fff@i.frr"))
println(Pattern.matches("[0-9a-f]*","adf14f5"))
println(Pattern.matches("\\?[0-9]*=","?154654="))
println(Pattern.matches("\\?se3*=","?se333333="))
println(Pattern.matches("\\d[^\\d]","7r"))

chaine = "Test regex Java pour <balise1>Wikibooks.\\n..</balise1>blabla <balise1>Wikibooks2...</balise1> francophone."
m = Pattern.compile("<.*?>.*?<.*?>").matcher(chaine)
println Pattern.compile("<.*>(.*)<.*>").split(chaine)
//println m.group()
while(m.find()) println(m.group())

println Pattern.compile("<b.*>(.*)</.*>").matcher(chaine).replaceAll("Toto1")
println Pattern.compile("<balise1>.*</balise1>").matcher(chaine).replaceAll("Toto2")
println Pattern.compile("Wikibooks").matcher(chaine).replaceAll("TotoXX")


 m = Pattern.compile("cat").matcher("one cat two cats in the yard")
 StringBuffer sb = new StringBuffer()
 while (m.find()) {     m.appendReplacement(sb, "dog"); }
 m.appendTail(sb)
 println(sb.toString())
 
 
 chaine = new File('D:\\Documents\\Marc\\Programme\\Groovy\\Leboncoin\\testboncoin.htm').text
 m = Pattern.compile("<a href=\"http:\\/\\/www.leboncoin.fr\\/[^\"].*?title\\=\".*?>([^a-z^&]*?<div class\\=\"lbc\">.*?)</a>",Pattern.DOTALL).matcher(chaine)
 while(m.find()) 
 {  
   /* def m2 = Pattern.compile("<div class\\=\"lbc\".*?>(.*?)",Pattern.DOTALL).matcher(m.group(1))
    while(m2.find()) 
    {  
     println ("***********");println(m2.group().substring(0,Math.min(200,m2.group().size())))
    }*/
    println ("***********");println(m.group(1).substring(0,Math.min(5000,m.group().size())))
 }
// m = Pattern.compile("<a href=.*?>(.*?)</a>",Pattern.DOTALL).matcher(chaine)
// while(m.find()) {  println ("***********");println(m.group())}
 //chaine = new File('D:\\Documents\\Marc\\Programme\\Groovy\\Leboncoin\\test.txt').text
 //m = Pattern.compile("<b.*?>(.+?)</b.*?>",Pattern.DOTALL).matcher(chaine)
 //while(m.find()) { println ("***********");println(m.group())}
 
 
