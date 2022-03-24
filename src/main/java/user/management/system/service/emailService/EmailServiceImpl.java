package user.management.system.service.emailService;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;


@Service(value = "emailService")
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String email;


    @Override
    public String sendSimpleEmail(String to, String body, String subject) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom(email);
        mailMessage.setTo(to);
        mailMessage.setText(body);
        mailMessage.setSubject(subject);
        javaMailSender.send(mailMessage);
        return "success";
    }

    @Override
    @SneakyThrows
    public String sendEmailWithAttachment(String to, String body, String subject)  {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mailMessage = new MimeMessageHelper(mimeMessage, true);

            mailMessage.setFrom(email);
            mailMessage.setTo(to);
            mailMessage.setText(body);
            mailMessage.setSubject(subject);
            File ticketFile = File.createTempFile("TICKET",".txt");
            FileSystemResource fileSystemResource = new FileSystemResource(ticketFile);
            if(fileSystemResource.getFilename()==null)
                return null;
            mailMessage.addAttachment(fileSystemResource.getFilename(), fileSystemResource);
            javaMailSender.send(mimeMessage);
        }
        catch (MessagingException e)
        {
            return e.getMessage();
        }
        return "success";
    }

}
