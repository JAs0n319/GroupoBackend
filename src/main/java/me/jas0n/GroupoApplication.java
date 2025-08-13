package me.jas0n;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "me.jas0n")
@EntityScan("me.jas0n.domain")
@EnableJpaRepositories("me.jas0n.repository")
public class GroupoApplication {
    public static void main(String[] args) {
        SpringApplication.run(GroupoApplication.class, args);
    }
}