
import javax.mail.internet.*;
import javax.mail.*
import javax.activation.*
import groovy.xml.*

def url="http://www.leboncoin.fr/annonces/offres/midi_pyrenees/?f=p&th=1&"
def toAddress = "marc.prevot@free.fr;carine.prevot@voila.fr"
//def toAddress = "marc.prevot@free.fr;"
def categoriesExclues=["Collection","Equipement Auto","Voitures","Décoration","Equipement bébé","Colocations","Moto","Equipement Moto","Montres et Bijoux","Vêtements bébé","(pro)","Ventes immobilières","Locations","Bureaux et Commerces","DVD / Films","CD / Musique","Vêtements","Vélos","Livres"," Téléphonie","Image & Son","Animaux","Jeux et Jouets"]
def titresExclus=["Sac ","Tableau","Location","Pull ","Gilet ","Robe ","Jupe ","Pantalon ","Peluche","bébé","Canapé","Poupee","Fauteuil","Télévision","Jeux","Jouet"]
def annonces

annonces = []
(1..3).each { annonces += getMessages(url+"location=Auzeville-Tolosane%2031320&o="+it)}
annonces= filtreExlusions(annonces,categoriesExclues,titresExclus)
sendMailFinal(annonces,toAddress)

annonces = []
(1..7).each { annonces += getMessages(url+"location=Castanet-Tolosan%2031320&o="+it)}
annonces= filtreExlusions(annonces,categoriesExclues,titresExclus)
sendMailFinal(annonces,toAddress)

annonces = []
(1..3).each { annonces += getMessages(url+"location=P%E9chabou%2031320&o="+it)}
annonces= filtreExlusions(annonces,categoriesExclues,titresExclus)
sendMailFinal(annonces,toAddress)

annonces = []
(1..9).each { annonces += getMessages(url+"location=Ramonville-Saint-Agne%2031520&o="+it)}
annonces= filtreExlusions(annonces,categoriesExclues,titresExclus)
sendMailFinal(annonces,toAddress)

def sendmail(String message ,String subject, String toAddress)
{
	Properties mprops = new Properties()
	mprops.setProperty("mail.transport.protocol","smtp")
	mprops.setProperty("mail.host"				,"smtp.bbox.fr")
	mprops.setProperty("mail.smtp.port"			,"25")

	Session lSession = Session.getDefaultInstance(mprops,null)
	MimeMessage msg = new MimeMessage(lSession)

	StringTokenizer tok = new StringTokenizer(toAddress,";")
	ArrayList emailTos = new ArrayList()
	while(tok.hasMoreElements()){      emailTos.add(new InternetAddress(tok.nextElement().toString()))}
	InternetAddress[] to = new InternetAddress[emailTos.size()]
	to = (InternetAddress[]) emailTos.toArray(to)
	msg.setRecipients(MimeMessage.RecipientType.TO,to)

	msg.setFrom(new InternetAddress("marc.prevot@leboncoin.fr"))
	msg.setSubject(subject)
	msg.setContent(message, "text/html; charset=iso-8859-15")

	Transport transporter = lSession.getTransport("smtp")
	transporter.connect()
	transporter.send(msg)
}

class Annonce
{
	String date=""
	String title=""
	String category=""
	String placement=""
	String price=""
	String description=""
	String url=""
	String urlImage=""

	void clean ()
	{
		date = date.replace("</div>",'').replace("<div>",'')
		date = date.replace("hui",'hui ')
		date = date.replace("ier",'ier ')
		date = date.replace('null',' ')
		date = date.trim()
		description = description.replace("<br>"," ")
		category    = category.replaceAll('&agrave;','a')
		title       = title.replaceAll('&amp;','et')
    }
}

def getMessages(def url)
{
	println url
	def texte = url.toURL().getText()
	texte = texte.replaceAll(	'&nbsp;&euro;'	," €"	)
	texte = texte.replaceAll(	'&agrave;'		,'à'	)
	texte = texte.replaceAll(	'&amp;'			,'et'	)
	texte = texte.replaceAll(	'&ecirc;'		,'ê'	)
	texte = texte.replaceAll(	'&eacute;'		,'é'	)
	texte = texte.replaceAll(	'&egrave;'		,'è'	)
	texte = texte.replaceAll(	'&#45;'			,'-'	)
	
	def annonces = []
	def annonce
	boolean isDebut = false, isFin=false
	def cpt=0
	def research="", description = "",urlDescription
	
	texte.eachLine
	{
		if (it.contains("<div class=\"list-lbc\">"))        isDebut = true
		if (it.contains("<div class=\"list-gallery\">"))    isFin   = true
		if (isDebut && !isFin)
		{
			if (it.contains("<div class=\"lbc\">"))   
			{
				if (annonce) {annonces.add(annonce); annonce.clean()}
				annonce = new Annonce()
			}
			if (it.contains(' href='))
			{
				urlDescription   = it.split(' href=')[1].split('"')[1]
				description =  getDescription(urlDescription)
			}

			['title','category','placement','price','date'].each 
			{nomAtt-> 
				research =  divAttribut(it,nomAtt)?nomAtt:research 
			}
			

			if (research=='date')
			{
				annonce.description = description
				annonce.url = urlDescription
				if (it.contains("<div class=\"image\">")) research=""
			}
			else if (it.contains("</div")) research=""
			if (it.contains("<img ")) annonce.urlImage=it.split("src=\"")[1].split("\"")[0]
			if (research && it && !divAttribut(it,research))	annonce."${research}" += it.trim()
		}
		cpt++
	}
	println "compteur lignes : " +cpt
	annonces
}

def buildHtmlMessage(def annonces)
{
	if (!annonces) return ""
	def writer = new StringWriter()
	def html = new MarkupBuilder(writer)
	html.html (lang:'fr')
	{
		head 
		{
			title 'Annonces Leboncoin'
			meta (charset:"iso-8859-15")
		}
		body()
		{
			annonces.each
			{ annonce ->
				h3 {a style:'color:darkred',href: annonce.url, annonce.category+"  (  "+annonce.title+"  )"}

				div{
					img src:annonce.urlImage, style:"float:left;margin:0 5px 10px 10px;"
					p annonce.description
					p annonce.date +" ( "+annonce.price+" ) "
					}
				hr style:"clear:left;"
				
			}
		}
	}
	writer.toString()
}
def boolean divAttribut(String chaine,String nomAttribut) {  chaine.contains("<div class=\""+nomAttribut+"\">")}

def sendMailFinal (def annonces,def toAddress)
{
	println "Nb annonces : " + annonces.size()

	def htmlMsg =  buildHtmlMessage(annonces)
	if (htmlMsg)
	{
		def subject = "Leboncoin : " + annonces[0].placement
		sendmail(htmlMsg,subject,toAddress)
	}
}

def getDescription(def urlAnnonce)
{
	def texte = urlAnnonce.toURL().getText()
	def debut = false
	def fin = false
	def result= ""
	texte.eachLine
	{
		if (debut && it.contains("</div>")) fin = true
		if (debut && !fin) result += it
		if (it.contains("<div class=\"content\" itemprop=\"description\">")) debut = true
	}
	result
}

def filtreExlusions(def annonces,categExclus,titreExclus)
{
	def res = []
	annonces.each
	{ annonce ->
		def aExclure = false
		
		categExclus.each
		{
			if (annonce.category.contains(it)) aExclure=true
		}
		//println "--"+annonce.category+"--"+annonce.title + "--"+aExclure
		titreExclus.each
		{
			if (annonce.title.toLowerCase().contains(it.toLowerCase())) aExclure=true
		}
		if (!(annonce.date.contains("Aujourd") || annonce.date.contains("Hier"))) aExclure=true
		if (!aExclure) res << annonce
	}
	res
}

def sendmail(String message ,String subject, String toAddress,String from)
{
	Properties mprops = new Properties()
	mprops.setProperty("mail.transport.protocol"	,"smtp")
	mprops.setProperty("mail.host"				,"smtp.bbox.fr")
	mprops.setProperty("mail.smtp.port"			,"25")

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