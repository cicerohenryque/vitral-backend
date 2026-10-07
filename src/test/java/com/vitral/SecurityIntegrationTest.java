package com.vitral;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.vitral.entity.Account;
import com.vitral.enumerations.AccountType;
import com.vitral.repository.AccountRepository;
import com.vitral.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtService jwtService;

    private String tokenUsuario;
    private String tokenSebo;
    private String tokenAdmin;
    private Long adminId;

    @BeforeEach
    void setUp() {
        Account usuario = accountRepository.save(Account.builder()
                .name("Usuario Teste")
                .username("usuario.seguranca")
                .email("usuario-seguranca@vitral.com")
                .passwordHash("hash")
                .type(AccountType.USUARIO)
                .emailVerificado(true)
                .build());
        Account sebo = accountRepository.save(Account.builder()
                .name("Sebo Teste")
                .username("sebo.seguranca")
                .email("sebo-seguranca@vitral.com")
                .passwordHash("hash")
                .type(AccountType.SEBO)
                .emailVerificado(true)
                .build());
        Account admin = accountRepository.save(Account.builder()
                .name("Admin Teste").username("admin.seguranca").email("admin-seguranca@vitral.com")
                .passwordHash("hash").type(AccountType.ADMIN).emailVerificado(true).build());
        tokenUsuario = jwtService.generateToken(usuario.getEmail(), usuario.getType().name());
        tokenSebo = jwtService.generateToken(sebo.getEmail(), sebo.getType().name());
        tokenAdmin = jwtService.generateToken(admin.getEmail(), admin.getType().name());
        adminId = admin.getId();
    }

    @Test
    @DisplayName("Rotas publicas de categorias devem aceitar visitante")
    void categorias_visitante_temAcesso() throws Exception {
        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Favoritos devem bloquear visitante sem token")
    void favoritos_visitante_semToken_eBloqueado() throws Exception {
        mockMvc.perform(get("/api/v1/favoritos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Recomendacoes devem bloquear visitante sem token")
    void recomendacoes_visitanteSemToken_eBloqueado() throws Exception {
        mockMvc.perform(get("/api/v1/recomendacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rotas protegidas devem rejeitar token invalido")
    void cesta_tokenInvalido_eBloqueado() throws Exception {
        mockMvc.perform(get("/api/v1/cesta")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Dados da conta devem bloquear visitante sem token")
    void authMe_visitanteSemToken_eBloqueado() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario autenticado deve acessar favoritos e cesta")
    void usuario_acessaRotasDeUsuario() throws Exception {
        mockMvc.perform(get("/api/v1/favoritos")
                        .header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/cesta")
                        .header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/recomendacoes")
                        .header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Conta SEBO nao deve acessar favoritos de usuario")
    void sebo_naoAcessaFavoritos() throws Exception {
        mockMvc.perform(get("/api/v1/favoritos")
                        .header("Authorization", bearer(tokenSebo)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario nao deve acessar historico de vendas do sebo")
    void usuario_naoAcessaVendas() throws Exception {
        mockMvc.perform(get("/api/v1/pedidos/vendas")
                        .header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Somente ADMIN deve acessar endpoints administrativos")
    void endpointsAdmin_validamAutoridadeDoBackend() throws Exception {
        mockMvc.perform(get("/api/v1/admin/usuarios"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/usuarios").header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/usuarios").header("Authorization", bearer(tokenSebo)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/usuarios?page=0&size=2").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.content[0].authVersion").doesNotExist())
                .andExpect(jsonPath("$.content[0].cpf").doesNotExist())
                .andExpect(jsonPath("$.content[0].token").doesNotExist());

        mockMvc.perform(get("/api/v1/admin/dashboard").header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/dashboard").header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarios").isNumber())
                .andExpect(jsonPath("$.produtosAtivos").isNumber());
    }

    @Test
    @DisplayName("Todas as areas administrativas bloqueiam visitante e usuario comum")
    void todasAreasAdmin_exigemAdmin() throws Exception {
        String[] endpoints = { "/api/v1/admin/dashboard", "/api/v1/admin/usuarios", "/api/v1/admin/sebos",
                "/api/v1/admin/produtos", "/api/v1/admin/pedidos", "/api/v1/admin/auditoria",
                "/api/v1/sebos/verificacao/pendentes" };
        for (String endpoint : endpoints) {
            mockMvc.perform(get(endpoint)).andExpect(status().isForbidden());
            mockMvc.perform(get(endpoint).header("Authorization", bearer(tokenUsuario)))
                    .andExpect(status().isForbidden());
            mockMvc.perform(get(endpoint).header("Authorization", bearer(tokenAdmin)))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("ADMIN pode desativar conta comum e recurso inexistente retorna 404")
    void admin_gerenciaStatusERecebeNotFound() throws Exception {
        Account usuario = accountRepository.findByEmail("usuario-seguranca@vitral.com").orElseThrow();
        mockMvc.perform(put("/api/v1/admin/usuarios/{id}/status", usuario.getId())
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(put("/api/v1/admin/usuarios/{id}/status", usuario.getId())
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.ativo").value(false));
        mockMvc.perform(get("/api/v1/admin/auditoria?entidade=CONTA")
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].acao").value("DESATIVOU"))
                .andExpect(jsonPath("$.content[0].entidadeId").value(usuario.getId()))
                .andExpect(jsonPath("$.content[0].adminEmail").value("admin-seguranca@vitral.com"));
        mockMvc.perform(get("/api/v1/admin/auditoria?entidade=CONTA")
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/v1/admin/produtos/999999")
                        .header("Authorization", bearer(tokenAdmin)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ADMIN nao pode desativar a propria conta nem outra conta administrativa")
    void admin_naoDesativaPropriaContaNemUltimoAdmin() throws Exception {
        mockMvc.perform(put("/api/v1/admin/usuarios/{id}/status", adminId)
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Você não pode desativar a própria conta administrativa."));

        Account outroAdmin = accountRepository.save(Account.builder().name("Outro Admin").username("outro.admin")
                .email("outro-admin@vitral.com").passwordHash("hash").type(AccountType.ADMIN)
                .emailVerificado(true).build());
        mockMvc.perform(put("/api/v1/admin/usuarios/{id}/status", outroAdmin.getId())
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Contas administrativas não podem ser alteradas por esta operação."));
    }

    @Test
    @DisplayName("Usuario nao deve cadastrar produto reservado a conta SEBO")
    void usuario_naoCadastraProduto() throws Exception {
        mockMvc.perform(post("/api/v1/produtos")
                        .header("Authorization", bearer(tokenUsuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Livro",
                                  "preco": 10.00,
                                  "estoque": 1,
                                  "condicao": "NOVO"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rota publica de classicos deve aceitar visitante sem token")
    void classicos_visitante_temAcesso() throws Exception {
        mockMvc.perform(get("/api/v1/produtos/classicos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Home dinamica deve aceitar visitante sem token")
    void home_visitante_temAcesso() throws Exception {
        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Token emitido antes da troca de senha deve ser rejeitado")
    void tokenComVersaoAntiga_eBloqueado() throws Exception {
        Account usuario = accountRepository.findByEmail("usuario-seguranca@vitral.com").orElseThrow();
        usuario.setAuthVersion(usuario.getAuthVersion() + 1);
        accountRepository.saveAndFlush(usuario);

        mockMvc.perform(get("/api/v1/favoritos")
                        .header("Authorization", bearer(tokenUsuario)))
                .andExpect(status().isForbidden());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
