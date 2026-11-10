package com.resolvex.service;
import com.resolvex.entity.Ticket; import com.resolvex.entity.User; import org.springframework.beans.factory.annotation.Value; import org.springframework.mail.SimpleMailMessage; import org.springframework.mail.javamail.JavaMailSender; import org.springframework.stereotype.Service; import org.springframework.scheduling.annotation.Async;
@Service public class MailService {
 private final JavaMailSender sender; private final boolean enabled;
 public MailService(JavaMailSender sender,@Value("${app.mail.enabled:false}") boolean enabled){this.sender=sender;this.enabled=enabled;}
 @Async public void notify(String to,String subject,String body){if(!enabled||to==null||to.isBlank())return;try{SimpleMailMessage m=new SimpleMailMessage();m.setTo(to);m.setSubject(subject);m.setText(body);sender.send(m);}catch(Exception ex){System.err.println("Email notification failed: "+ex.getMessage());}}
 public void ticketCreated(Ticket t){notify(t.getRequester().getEmail(),"ResolveX ticket #"+t.getId()+" created","Your ticket '"+t.getTitle()+"' has been created. Status: "+t.getStatus());}
 public void ticketUpdated(Ticket t){notify(t.getRequester().getEmail(),"ResolveX ticket #"+t.getId()+" updated","Your ticket '"+t.getTitle()+"' is now "+t.getStatus()+".");}
 public void ticketAssigned(Ticket t){if(t.getAssignee()!=null)notify(t.getAssignee().getEmail(),"ResolveX ticket #"+t.getId()+" assigned to you","Ticket '"+t.getTitle()+"' has been assigned to you. Please sign in to ResolveX to review it.");}
 public void ticketCommentCreated(Ticket t, User sender, String message){
  String subject="New reply on ResolveX ticket #"+t.getId();
  String body="A new public message was added to ticket '"+t.getTitle()+"' by "+sender.getName()+" ("+sender.getRole()+").\n\n"+message+"\n\nSign in to ResolveX to reply.";
  if(t.getRequester()!=null && !t.getRequester().getId().equals(sender.getId())) notify(t.getRequester().getEmail(),subject,body);
  if(t.getAssignee()!=null && !t.getAssignee().getId().equals(sender.getId()) && (t.getRequester()==null || !t.getAssignee().getId().equals(t.getRequester().getId()))) notify(t.getAssignee().getEmail(),subject,body);
 }
}
