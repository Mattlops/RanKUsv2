# Plano de Implementação: Correções Críticas (Gravação na Base, Sincronização do Ranking e Redirecionamento de Cadastro)

## 1. Visão Geral dos Problemas e Diagnóstico

1. **Falha na gravação do "Criar conta" na base de dados (Firestore)**:
   - **Diagnóstico**: As regras de segurança em `firestore.rules` barravam qualquer gravação sem sessão autenticada (`request.auth == null`). O fluxo de "Criar conta" criava um ID local sem autenticar no Firebase Auth com `createUserWithEmailAndPassword`, e ao chamar `db.collection("users").document(newId).set()`, a operação falhava silenciosamente com `PERMISSION_DENIED`. Além disso, faltavam os campos com `FieldValue.serverTimestamp()` para `createdAt` e `updatedAt`.
   - **Solução**: 
     - Autenticar o usuário criado no Firebase Auth (`createUserWithEmailAndPassword`) ao criar conta com e-mail e senha.
     - Ajustar as regras do Firestore (`firestore.rules`) para validar os dados do usuário com segurança e implantar com `deploy_firebase`.
     - Gravar no Firestore `users/{uid}` os dados completos do perfil com timestamps de servidor e redundância em cache local.

2. **Menu Ranking não exibindo os resultados dos jogadores gravados na base de dados**:
   - **Diagnóstico**: O `RankusRepository` não possuía listeners de snapshot ativos para as coleções `users` e `scores` do Firestore. O ranking exibia apenas os dados em memória local do próprio aparelho (`_registeredAccounts`), ocultando todos os outros competidores e resultados reais salvos no banco.
   - **Solução**:
     - Ativar listeners de tempo real (`addSnapshotListener`) para `collection("users")` e `collection("scores")`.
     - No método `getRankingPlayers(gameFilter, timeFilter)`, calcular dinamicamente a pontuação de cada jogador da base de dados respeitando o jogo selecionado ("Todos os Jogos", "Termo", "Globle", "SizeItUp") e o período ("Hoje", "Semana", "Mês", "Geral").
     - Popular o Pódio, a Lista de Classificação e o Gráfico de Linhas com todos os jogadores da base de dados.

3. **Pop-up de sucesso não fecha e não redireciona para o Início ao salvar resultado**:
   - **Diagnóstico**: Em `CadastrarScreen.kt`, o fechamento e o redirecionamento estavam dentro de um `LaunchedEffect(isSubmitting)`. Dentro do próprio bloco do efeito, a linha `isSubmitting = false` alterava o valor da chave `isSubmitting`, fazendo com que o Compose cancelasse imediatamente a corrotina em execução antes de atingir `delay(1300)`, `showSuccessAnimation = false` e `onBackToHome()`. Como a corrotina era abortada, `showSuccessAnimation` permanecia `true` indefinidamente na tela.
   - **Solução**:
     - Remover o gatilho instável `LaunchedEffect(isSubmitting)` e disparar a submissão via `rememberCoroutineScope()`.
     - Executar a sequência com segurança: submeter pontuação -> exibir animação de comemoração -> aguardar tempo de celebração -> ocultar pop-up -> invocar `onBackToHome()`.
     - Adicionar clique no overlay e botão de confirmação ("OK / Continuar") para fechar imediatamente e navegar para o Início caso o usuário queira prosseguir sem esperar o temporizador.

---

## 2. Modificações Planejadas por Arquivo

| Arquivo | Modificações |
| :--- | :--- |
| `firestore.rules` | Atualização das regras de segurança para garantir a gravação e consulta de usuários e pontuações válidas no Firestore, seguida de deploy com `deploy_firebase`. |
| `data/RankusRepository.kt` | - Integração com `createUserWithEmailAndPassword` e `signInWithEmailAndPassword`.<br>- Snapshot listeners ativos para `users` e `scores` da base.<br>- Agregação dinâmica em `getRankingPlayers` e `getEvolutionData` para todos os jogadores do banco. |
| `ui/screens/CadastrarScreen.kt` | - Substituição do `LaunchedEffect(isSubmitting)` por corrotina de escopo estável (`rememberCoroutineScope`).<br>- Garantia de fechamento automático do pop-up e execução de `onBackToHome()`.<br>- Opção de toque/botão para fechamento imediato do pop-up. |
| `ui/screens/AuthScreen.kt` | Feedback visual de carregamento e mensagens claras ao criar conta ou entrar. |
| `ui/screens/RankingsScreen.kt` | Vinculação reativa de todos os competidores reais da base de dados aos filtros de jogo e tempo. |

---

## 3. Plano de Verificação e Critérios de Aceite

1. **Teste de Cadastro na Base de Dados**:
   - Criar uma nova conta pela opção "Criar conta" com Apelido, E-mail e Senha.
   - Confirmar a gravação na coleção `users` do Firestore.
2. **Teste do Menu Ranking com Dados da Base**:
   - Registrar pontuações e acessar a aba "Rankings".
   - Confirmar que todos os jogadores reais da base de dados aparecem no ranking com as pontuações corretas.
   - Testar os filtros ("Termo", "Globle", "SizeItUp" e "Hoje", "Semana", "Mês", "Geral") e verificar a atualização dinâmica.
3. **Teste do Cadastro de Resultados e Redirecionamento**:
   - Submeter um resultado em "Cadastrar".
   - O pop-up de sucesso verde "+X pontos registrados no RanKUs" deve ser exibido, fechar automaticamente e redirecionar para a tela inicial (Início).
4. **Compilação**:
   - Executar `compile_applet` e garantir compilação com sucesso total.
