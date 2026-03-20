import com.sendgrid.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    public void sendEmail(String to, String subject, String body) {

        Email from = new Email("noreply@yourapp.com");
        Email recipient = new Email(to);

        Content content = new Content("text/html", body);

        Mail mail = new Mail(from, subject, recipient, content);

        SendGrid sg = new SendGrid(apiKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("Email sent! Status: " + response.getStatusCode());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}