# 🚗 Loca Mais - Sistema de Gestão de Locadora

> Uma solução desktop robusta para gerenciamento de frotas, clientes e reservas, desenvolvida com foco em integridade de dados e experiência do usuário.

<div align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/Swing-ED8B00?style=for-the-badge&logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/JDBC-007396?style=for-the-badge&logo=java&logoColor=white" />
</div>

---

## 📖 Sobre o Projeto
O **Loca Mais** foi projetado para substituir planilhas e controles manuais em pequenas locadoras de veículos. O sistema centraliza a operação, desde o cadastro da frota até a devolução do veículo, garantindo que não haja conflitos de agenda ou furos no estoque.

Diferente de sistemas básicos, o Loca Mais implementa **internacionalização nativa**, permitindo operação em múltiplos idiomas (PT-BR, EN-US, ES-ES) com troca instantânea, ideal para regiões turísticas.

---

## 📸 Screenshots
*(Adicione aqui prints das telas do seu sistema para mostrar a interface)*

| Login & Internacionalização | Dashboard Principal | Nova Reserva |
|:---:|:---:|:---:|
| ![Tela de Login](assets/login-print.png) | ![Dashboard](assets/dashboard-print.png) | ![Nova Reserva](assets/reserva-print.png) |

---

## 🛠️ Tecnologias e Decisões Técnicas

O projeto foi construído utilizando **Java Puro (Vanilla)**, focando no domínio profundo da linguagem e da biblioteca Swing, sem dependência de frameworks visuais externos.

* **Linguagem:** Java JDK 8+.
* **Interface (GUI):** Java Swing com gerenciadores de layout manuais (`GridBagLayout`, `BorderLayout`) para interfaces responsivas.
* **Persistência:** MySQL com JDBC puro (padrão DAO/Repository).
* **Design Patterns:** Singleton (Conexão DB), Factory (Componentes Visuais) e MVC (Separação Lógica/Visual).

### 🌟 Destaques de Engenharia:
1.  **Transações ACID:** O sistema de reservas utiliza `conn.setAutoCommit(false)` para garantir atomicidade. A reserva só é criada se o veículo for baixado do estoque com sucesso; caso contrário, tudo é revertido (Rollback).
2.  **Internacionalização (i18n):** Implementação manual de `ResourceBundle` via classe `LanguageManager`, permitindo suporte a Português, Inglês e Espanhol, ajustando inclusive formatos de data (`dd/MM` vs `MM/dd`) automaticamente.
3.  **Renderização Customizada:** Uso de `TableCellRenderer` para injetar botões funcionais ("Editar", "Excluir", "Finalizar") diretamente nas células da `JTable`.

---

## 📐 Arquitetura

O sistema segue uma arquitetura modular documentada na [Especificação de Sistema](./ERS_Modelo_Geral.docx).

### Diagrama de Classes
A estrutura separa claramente as camadas de visualização (`*Frame.java`), utilitários (`DateUtil`, `ValidadorCPF`) e modelo de dados.
*(Coloque a imagem do diagrama de classes do seu Word aqui)*
![Diagrama de Classes](assets/diagrama-classes.png)

### Fluxo de Sequência (Reserva)
Demonstração da interação entre a interface, o validador de CPF e o banco de dados durante uma nova locação.
![Diagrama de Sequência](assets/diagrama-sequencia.png)

---

## 🚀 Como Rodar o Projeto

### Pré-requisitos
* [Java JDK 8](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html) ou superior.
* [MySQL Server](https://dev.mysql.com/downloads/mysql/).
* Uma IDE Java (IntelliJ, Eclipse ou NetBeans).

### 🎲 Configurando o Banco de Dados
1.  Crie um banco de dados no MySQL chamado `locamais`.
2.  Execute o script SQL disponível em `Banco de dados.sql`. Ele criará as tabelas e inserirá o usuário administrador padrão.
3.  No arquivo `Main.java`, classe `Conexao`, verifique se as credenciais batem com as da sua máquina:
    ```java
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Coloque sua senha aqui
    ```

### 💻 Executando
1.  Clone o repositório:
    ```bash
    git clone [https://github.com/SEU_USUARIO/LocaMais.git](https://github.com/SEU_USUARIO/LocaMais.git)
    ```
2.  Abra o projeto na sua IDE.
3.  Execute a classe principal `Main.java`.
4.  **Login Padrão:**
    * **Email:** `admin`
    * **Senha:** `admin`

---

## 🔮 Próximos Passos (Roadmap)
* [ ] Implementar testes unitários (JUnit) para validação de regras de negócio.
* [ ] Criar relatórios em PDF para contratos de locação.
* [ ] Migrar o Backend para Spring Boot mantendo o Swing como cliente (Desktop Client).

---

## 📝 Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

<div align="center">
  Desenvolvido por <strong>Marcos Vinicius</strong> 🚀
  <br>
  <a href="LINK_DO_SEU_LINKEDIN">
    <img src="https://img.shields.io/badge/-LinkedIn-blue?style=flat-square&logo=Linkedin&logoColor=white">
  </a>
</div>
