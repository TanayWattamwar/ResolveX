package com.resolvex.service;
import com.resolvex.entity.Ticket; import org.springframework.beans.factory.annotation.Value; import org.springframework.mail.SimpleMailMessage; import org.springframework.mail.javamail.JavaMailSender; import org.springframework.stereotype.Service;
@Service public class MailService {
 private final JavaMailSender sender; private final boolean enabled;
 public MailService(JavaMailSender sender,@Value("${app.mail.enabled:false}") boolean enabled){this.sender=sender;this.enabled=enabled;}
 public void notify(String to,String subject,String body){if(!enabled||to==null||to.isBlank())return;try{SimpleMailMessage m=new SimpleMailMessage();m.setTo(to);m.setSubject(subject);m.setText(body);sender.send(m);}catch(Exception ex){System.err.println("Email notification failed: "+ex.getMessage());}}
 public void ticketCreated(Ticket t){notify(t.getRequester().getEmail(),"ResolveX ticket #"+t.getId()+" created","Your ticket '"+t.getTitle()+"' has been created. Status: "+t.getStatus());}
 public void ticketUpdated(Ticket t){notify(t.getRequester().getEmail(),"ResolveX ticket #"+t.getId()+" updated","Your ticket '"+t.getTitle()+"' is now "+t.getStatus()+".");}
}
