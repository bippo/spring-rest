package bippotraining;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

// REF: https://www.baeldung.com/hibernate-5-spring

@Configuration
@EnableTransactionManagement
@ImportResource({"classpath:hibernate5Configuration.xml"})
public class HibernateXMLConf {

}
