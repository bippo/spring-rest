package bippotraining;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

@SpringBootApplication(exclude= HibernateJpaAutoConfiguration.class) // https://stackoverflow.com/questions/38627491/spring-4-hibernate-5-org-springframework-orm-jpa-entitymanagerholder-cannot/38637273#38637273
public class Application {

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

}
