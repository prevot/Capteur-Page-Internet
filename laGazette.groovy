@Grapes( @Grab(group='javax.mail', module='mail', version='1.4.7'))
import javax.mail.internet.*;
import javax.mail.*
import javax.activation.*

@Grapes( @Grab('org.jsoup:jsoup:1.8.3'))
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element
import org.jsoup.nodes.Document

def URL_GENERALE = "http://www.lagazettedescommunes.com/rubriques/"

def pathDirectory = "d:\\Documents\\Marc\\Programme\\Groovy\\Leboncoin\\"
//new File(pathDirectory+'log.txt').delete()
def log = new File(pathDirectory+'log.txt')

def toAddress = "marc.prevot888@gmail.com"
//def toAddress = "marc.prevot@free.fr"
def annoncesGlobale = []
def nbArticles = 0
["actualite","actu-juridique","textes-officiels","jurisprudence","reponses-ministerielles","club-technique"].each
{ type ->
    def urlS = getUrlArticles(URL_GENERALE+type,log)
   // annonces     
    println "---------  v2 ${type} (${urlS?.size()})  -------------- "
    log <<  "---------  v2 ${type} (${urlS?.size()})  -------------- \n"
    urlS.each 
    {
        println it
        def article = getArticle(it,log)
        if (article) {annoncesGlobale.add(article);nbArticles++;}
        //println article
    }
}
sendMailFinal(annoncesGlobale,toAddress,nbArticles)

def getArticle(urlArticle,log)
{
    def res= ""

    try
    {
      Document doc = Jsoup.connect(urlArticle).get();
      
      def body = doc.select("div.contenu-tronque").first() ?:doc.select("div").select("[itemprop=articleBody]").first();
      def sstitre = doc.select("p"    ).select("[itemprop=description]"    ).first();
      def titre   = doc.select("h1"   ).select("[itemprop=headline]"       ).first();
      def datePub = doc.select("time" ).select("[itemprop=datePublished]"  ).first()?.text();
      def dateArticle = new java.text.SimpleDateFormat("dd/MM/yy", Locale.FRANCE).parse(datePub)
      def dateJour = new Date()-1
	  dateJour.clearTime()
      if ( dateJour.equals(dateArticle))
      {
          res = titre.toString()+"....................................." + sstitre.toString() + "....................................."  + body.toString()
      }
      else
      {
          println "Article trop vieux !!!   " + datePub
      }
    }
    catch (Exception e) {e.printStackTrace();log << "article illisible " + urlArticle + e.printStackTrace()}   
    res
}


def getUrlArticles(def url,def log)
{
    def res;def urlS
    try
    {
        def doc = Jsoup.connect(url).get()
        urlS = doc.select("li.actus-case1").select("h4 a[href]")*.attr("href")
        
        res  = doc.select("li.actus-case2").select("h4 a[href]")*.attr("href")         ;    urlS.addAll(res)
        res  = doc.select("li.actus-liste-item").select("h3 a[href]")*.attr("href")    ;    urlS.addAll(res)
        res  = doc.select("div.actus-liste-titre").select("h3 a[href]")*.attr("href")  ;    urlS.addAll(res)
    }
    catch (Exception e) {println "url illisible";log <<  "url illisible" + url + ".........."+ e.printStackTrace();}
    urlS
}

def sendMailFinal (def annonces,def toAddress,def nbArticles)
{
    def res = ""
    annonces.each{ res +=it}
    def htmlMsg =  "<ul>"+ res +"</ul>"
    if (htmlMsg)
    {
        def subject = "Gazette articles : " + nbArticles + "  du "+ (new java.text.SimpleDateFormat("dd-MM-yy")).format(new Date())
        sendmail(htmlMsg,subject,toAddress,"gazette@gazette.fr")
    }
}

def sendmail(String message ,String subject, String toAddress,String from)
{
    Properties mprops = new Properties()
    mprops.setProperty("mail.transport.protocol","smtp")
    mprops.setProperty("mail.host"              ,"smtp.bbox.fr")
    mprops.setProperty("mail.smtp.port"         ,"25")

    Session lSession = Session.getDefaultInstance(mprops,null)
    MimeMessage msg = new MimeMessage(lSession)

    StringTokenizer tok = new StringTokenizer(toAddress,";")
    ArrayList emailTos = new ArrayList()
    while(tok.hasMoreElements()){      emailTos.add(new InternetAddress(tok.nextElement().toString()))}
    InternetAddress[] to = new InternetAddress[emailTos.size()]
    to = (InternetAddress[]) emailTos.toArray(to)
    msg.setRecipients(MimeMessage.RecipientType.TO,to)

    msg.setFrom(new InternetAddress(from))
    msg.setSubject(subject)
    msg.setContent(message, "text/html; charset=utf8")

    Transport transporter = lSession.getTransport("smtp")
    transporter.connect()
    transporter.send(msg)
}