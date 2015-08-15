
import javax.mail.internet.*;
import javax.mail.*
import javax.activation.*
import groovy.xml.*
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.dom4j.io.SAXReader
import org.dom4j.Document

def URL_GENERALE = "http://www.lagazettedescommunes.com/rubriques/"

def pathDirectory = "d:\\Documents\\Marc\\Programme\\Groovy\\Leboncoin\\"
//new File(pathDirectory+'log.txt').delete()
def log = new File(pathDirectory+'log.txt')

def toAddress = "marc.prevot888@gmail.com"
//def toAddress = "marc.prevot@free.fr"
def exclusions=["CONCOURS","Concours"]

def annoncesGlobale = []

["actualite","actu-juridique","textes-officiels","jurisprudence","reponses-ministerielles","club-technique"].each
{ type ->
    def annonces = (type == 'actualite') ? getMessages(URL_GENERALE+type) : getMessages2(URL_GENERALE+type)
    annonces     = (type == 'actualite' )? nettoyeur(annonces,type)   : nettoyeur2(annonces,type)
    annonces     = infoComplete(annonces,log)
    println "---------   ${type} (${annonces.size()})  -------------- "
    log <<  "---------   ${type} (${annonces.size()})  -------------- \n"
    annonces.each {annoncesGlobale.add(it)}
}

annoncesGlobale= filtreExlusions(annoncesGlobale,exclusions)

sendMailFinal(annoncesGlobale,toAddress)

def infoComplete(resumesArticles,log)
{
    resultat=[]
    resumesArticles.each
    {resumeArticle ->
        def res = ""; urlInfo = ""
        //lecture de l'url de l'article
        def m = Pattern.compile("<a href=\"(.*?)\">",Pattern.DOTALL).matcher(resumeArticle)
        while(m.find())    {    urlInfo = m.group(1)    }
        println urlInfo
       
        
    }
    resultat
}

def nettoyeur(def annonces,def tag)
{
    def res = [];def m
    annonces.each
    { annonce ->
        m = Pattern.compile("<p class=\"surtitre\">(.*?)</p>",Pattern.DOTALL).matcher(annonce)
        while(m.find())        {    annonce= annonce.replace(m.group(1),m.group(1) +" ["+tag+"]")    }
        m = Pattern.compile("<img .*?>",Pattern.DOTALL).matcher(annonce)
        while(m.find())    {    annonce= annonce.replace(m.group(0),"")    }  //suppression des images
        m = Pattern.compile("<ul>.*?<li class=\"commentaire\">[0-9]</li>.*?</ul>",Pattern.DOTALL).matcher(annonce)
        while(m.find())    {    annonce= annonce.replace(m.group(0),"")    }
        m = Pattern.compile("<p>(<a href.*?>).*?(</a>)",Pattern.DOTALL).matcher(annonce)
        m2 = Pattern.compile("[a-zA-Z](\\?)[a-zA-Z]",Pattern.DOTALL).matcher(annonce)
        annonce = new StringBuffer(annonce)

        
        while(m.find())
        {    
            annonce = annonce.replace(m.start(2),m.end(2),'')
            annonce = annonce.replace(m.start(1),m.end(1),"")
        }

        while(m2.find())
        {    
            annonce= annonce.replace(m2.start(1),m2.end(1),"'")
        }

        annonce = annonce.toString()
        annonce = annonce.replaceAll("’","'")
        annonce = annonce.replaceAll("li>,","li>")        
         
        annonce = annonce.replaceAll("width:220px;float:left;padding-right:20px;","")
        m = Pattern.compile("<li class=\"actus-case.*?>",Pattern.DOTALL).matcher(annonce);
        while(m.find())        {    annonce= annonce.replace(m.group(0),"<li>")        }
        m = Pattern.compile("<div class=\"actus-.*?>",Pattern.DOTALL).matcher(annonce)
        while(m.find())        {    annonce= annonce.replace(m.group(0),"<div>")        }
        m = Pattern.compile("<span.*?>.*?</span>",Pattern.DOTALL).matcher(annonce)
        while(m.find())        {    annonce= annonce.replace(m.group(0),"")        }

        // enlève les retours à la ligne et les espaces vides
        annonce = annonce.replaceAll("\n","")
        annonce = annonce.replaceAll("\t","")
        annonce = annonce.replaceAll("   ","")
        annonce = annonce.replaceAll("<div>","")
        annonce = annonce.replaceAll("</div>","")
        annonce = annonce.replaceAll("<p style=''></span></p>","")
        //annonce = annonce.replaceAll("<div ></div>","")
        
        if (annonce.contains("<p class=\"surtitre\">"))    res.add(annonce)
    }
    res
}

def nettoyeur2(def annonces,def tag)
{
    def res = [];def m
    
    annonces.each
    {annonce->
        if (annonce.contains(new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()-1)))
        {
            m = Pattern.compile("<p class=\"surtitre\">(.*?)</p>",Pattern.DOTALL).matcher(annonce)
            while(m.find())        {    annonce= annonce.replace(m.group(1),m.group(1) +" ["+tag+"]")    }
            m = Pattern.compile("<p class=\"date-par.*?>.*?</p>",Pattern.DOTALL).matcher(annonce)
            while(m.find())        {    annonce= annonce.replace(m.group(0),"")    }
            m = Pattern.compile("<ul.*?>.*?</ul>",Pattern.DOTALL).matcher(annonce)
            while(m.find())        {    annonce= annonce.replace(m.group(0),"")    }
            m = Pattern.compile("<p>.*?<a href=.*?>(.*?)</a>.*?</p>",Pattern.DOTALL).matcher(annonce)
            while(m.find())        {    annonce= annonce.replace(m.group(0),"<p>"+m.group(1)+"</p>")    }
            annonce = annonce.replaceAll(" <!-- /Actus liste Item -->","")
            annonce = annonce.replaceAll(" <!-- Actus liste Item -->","")
            annonce = annonce.replaceAll(" class=\"actus-liste-item clearfix alaune\"","") 
            annonce = annonce.replaceAll(" class=\"actus-liste-item-info-visuel\"","")
            annonce = annonce.replaceAll(" class=\"actus-liste-item-texte\"","")
            annonce = annonce.replaceAll(" class=\"actus-liste-item clearfix\"","")
            annonce = annonce.replaceAll(" class=\"actus-liste-titre clearfix\"","")
            annonce = annonce.replaceAll(" class=\"surtitre\"","")
            
            if (tag=='Jurisprudence') 
            {
                m = Pattern.compile("<a href=.*?>(.*?)</a>",Pattern.DOTALL).matcher(annonce)
                while(m.find())        {    annonce= annonce.replace(m.group(0),m.group(1))    }
            }
               
            // enlève les retours à la ligne et les espaces vides
            annonce = annonce.replaceAll("\n","")
            annonce = annonce.replaceAll("\t","")
            annonce = annonce.replaceAll("   ","")       
            res.add(annonce)
        }
    }
    res
}

def getMessages(def url)
{
    def texte = url.toURL().getText('utf-8')
    def annonces = [], annonce=""
    boolean isDebut = false,isDebutProche=false, isFin=false
    def research="", description = "",urlDescription
    
    texte.eachLine
    {
        if (it.contains("<div class=\"actus-home-right\">"))        isFin   = true
        if (it.contains("<li class=\"actus-case") && isDebutProche)    isDebut = true
        if (isDebut && !isFin)
        {
            if (it.contains("<li class=\"actus-case"))
            {
                if (annonce) annonces.add(annonce)
                annonce = ""
            }
            annonce += it
        }
        if (it.contains("<div class=\"actus-home-left\">"))        isDebutProche = true
    }
    annonces.add(annonce)
    annonces
}
def getMessages2(def url)
{
    def texte = url.toURL().getText('utf-8')
    def annonces = [], annonce=""
    boolean isDebut = false,isDebutProche=false, isFin=false
    def research="", description = "",urlDescription
    
    texte.eachLine
    {
        if (it.contains("<div class=\"actus-liste-pub"))        isFin   = true
        if (it.contains("<div class=\"actus-liste-liste") )        isDebut = true
        if (isDebut && !isFin)
        {
            if (it.contains("<li class=\"actus-liste-item"))
            {
                if (annonce) annonces.add(annonce)
                annonce = ""
            }
            annonce += it
        }
    }
    annonces.add(annonce)
    annonces
}

def getMessages3(def url,log)
{
    def texte = url.toURL().getText('UTF-8')
    def annonce=""
    boolean isDebut = false,isDebutProche=false, isFin=false
    def research="", description = "",urlDescription
    
    texte.eachLine
    {
        if (it.contains("<div class=\"content-right"))    isFin   = true
        if (it.contains("<div class=\"content-left") )    isDebut = true
        if (isDebut && !isFin)
        {
            annonce += it
        }
    }

    annonce
}

def sendMailFinal (def annonces,def toAddress)
{
    def res = ""
    annonces.each{ res +=it}
    def htmlMsg =  "<ul>"+ res +"</ul>"
    if (htmlMsg)
    {
        def subject = "Gazette : " + (new java.text.SimpleDateFormat("dd-MM-yy")).format(new Date())
        sendmail(htmlMsg,subject,toAddress,"gazette@gazette.fr")
    }
}

def filtreExlusions(def annonces,exclusion)
{
    def res = []
    annonces.each
    { annonce ->
        def aExclure = false
        exclusion.each
        {
            if (annonce.contains("<p class=\"surtitre\">"+it)) aExclure=true
        }
        if (!aExclure) res << annonce
    }
    res
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
    msg.setContent(message, "text/html; charset=iso-8859-15")

    Transport transporter = lSession.getTransport("smtp")
    transporter.connect()
    transporter.send(msg)
}