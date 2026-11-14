package br.com.fiap.os.seguranca.config;

import br.com.fiap.os.seguranca.adapter.out.persistence.UsuarioJpaRepository;
import br.com.fiap.os.seguranca.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioJpaRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (repository.count() == 0) {
            Usuario admin = Usuario.builder()
                    .login("admin")
                    .senha(passwordEncoder.encode("admin123"))
                    .nome("Administrador")
                    .build();
            repository.save(admin);
            log.info("Usuário admin criado com sucesso");
        }
    }
}
