# Balanceamento do Globle & Modo Suspense do Administrador (Ranking Oculto)

Implementação da nova fórmula de pontuação do Globle (-5 pontos por tentativa extra, garantindo piso de 10 pontos ao atingir 20 tentativas), atualização textual rigorosa de todas as menções para o nome oficial "Globle", e adição do recurso exclusivo de administrador para o jogador Matheus Lopes (`google_matheus_lopes`) com botão de alternância do "Modo Suspense" que sincroniza em tempo real via Firestore e exibe uma tela especial de sigilo/suspense para os demais competidores.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Decisões confirmadas pelo usuário:**
> - **Penalidade do Globle:** Confirmado **X = 5 pontos por palpite extra**. A fórmula oficial fica: `SE(Tentativas ≤ 2; 100; MÁXIMO(10; 100 - ((Tentativas - 2) × 5)))`. Assim, na 20ª tentativa a pontuação atinge exatamente o piso mínimo de 10 pontos.
> - **Tela de Suspense dos Jogadores:** Quando o administrador ativar "Esconder Ranking", os demais jogadores que acessarem o menu Ranking verão uma **Tela de Suspense da Grande Final** temática ("🏆 Ranking Congelado!"), mantendo o engajamento e a contagem de pontos no escuro.
> - **Atualização do APK para os amigos:**
>   - **Mudanças no código/interface/cálculo:** Requerem que os amigos instalem o novo arquivo `.apk` gerado.
>   - **Dados em nuvem (pontuações e o toggle de esconder ranking):** Sincronizam instantaneamente pela internet via Firestore, sem necessidade de reinstalar caso já estejam na versão compatível.

---

## 1. Overview & Core Concept

- **O que faz:**
  1. **Novo Cálculo e Regras do Globle:**
     - 1 a 2 palpites: 100 pontos garantidos.
     - A partir do 3º palpite: dedução de 5 pontos por tentativa adicional (`(tentativas - 2) * 5`).
     - Piso garantido de 10 pontos (atingido exatamente aos 20 palpites).
     - Atualização do painel explicativo de regras do app e da fórmula nos detalhes do resultado.
  2. **Padronização do Nome Oficial "Globle":**
     - Verificação completa para assegurar que nenhuma referência remanescente use "Globe" em regras, botões, modais ou mensagens.
  3. **Painel de Administrador & Modo Suspense:**
     - Identificação segura do administrador Matheus Lopes (`google_matheus_lopes` / `WTFLopes` / `matheus_lopes@gmail.com`).
     - Banner exclusivo no topo da tela de Rankings com switch: **"Modo Suspense: Esconder Ranking"**.
     - Sincronização em tempo real através do documento `config/app_settings` no Firestore.
     - **Visão do Administrador:** Continua vendo o ranking real com status de alerta ativo.
     - **Visão dos Demais Jogadores:** Visualizam uma tela especial com cadeado dourado, selo "MODO SUSPENSE ATIVADO" e mensagem informando que os resultados estão sob sigilo para a final da temporada, incentivando a continuarem pontuando normalmente.

---

## 2. User Experience & Visual Design

### 1. Visual do Globle Atualizado
- Na tela de **Cadastrar Resultado**, ao colar o resultado do Globle, a mensagem da fórmula exibirá claramente a nova matemática:
  `100 - ((X - 2) × 5) = Y pts (piso mín. 10 pts na 20ª tentativa)`.
- No card de **Regras de Pontuação** em Rankings, o item "Globle (Geografia)" exibirá as regras atualizadas com o novo multiplicador 5.

### 2. Painel do Administrador (Para Matheus Lopes)
- Card destacado com gradiente dourado escuro/âmbar `#231A0E` e borda `#FFA726`:
  - Ícone de escudo/administrador (`Icons.Default.AdminPanelSettings` / `Security`).
  - Título: **"Controle do Administrador"**.
  - Switch: **"Modo Suspense (Esconder Ranking)"**.
  - Legenda explicativa em tempo real:
    - *Ativado:* "🔒 Ranking bloqueado para os jogadores. Eles estão visualizando a tela de suspense da Grande Final."
    - *Desativado:* "🔓 Ranking público aberto para todos os jogadores."

### 3. Tela de Suspense (Para os Demais Competidores)
Quando `isRankingHidden == true` e o usuário não for o administrador:
- Em vez da listagem de líderes, a aba Rankings renderiza uma tela imersiva de mistério:
  - Escudo circular brilhante com cadeado de ouro em destaque (`TrophyGold` / `#FFD700`).
  - Badge em neon: **"CLASSIFICAÇÃO EM SIGILO"**.
  - Título imponente: **"🏆 Ranking Congelado para a Final!"**.
  - Texto de imersão:
    *"A tabela de classificação foi trancada pela administração nos dias decisivos da competição!\n\nOs resultados finais estão sob sigilo absoluto para coroar o grande campeão da temporada. Continue registrando seus jogos diários normalmente: todos os seus pontos continuam sendo computados no escuro!"*
  - Card motivacional: *"Mantenha sua disciplina no Termo Quarteto, Globle e SizeItUp! O pódio será revelado no encerramento."*
  - Botão de ação rápida: *"Jogar Agora"* e *"Registrar Resultado"*.

---

## 3. Key Product Decisions & Trade-Offs

- **Sincronização do Status via Firestore (`config/app_settings`):**
  - *Decisão:* Armazenar `isRankingHidden` em um documento central no Firestore com snapshot listener em tempo real.
  - *Vantagem:* Quando Matheus Lopes ativar ou desativar o Modo Suspense pelo celular dele, a alteração se propaga instantaneamente para os celulares de todos os competidores em segundos, sem necessidade de nova versão.
- **Transparência de Permissões e Fallback:**
  - Além de Firestore, o estado é mantido em cache local no `SharedPreferences` para garantir funcionamento offline imediato sem telas brancas.
- **Acesso do Administrador Preservado:**
  - O administrador nunca perde o acesso visual ao ranking real. Apenas os outros jogadores são direcionados para a tela de suspense.

---

## 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────┐
│                    RankusRepository                          │
│  - isRankingHidden: StateFlow<Boolean>                       │
│  - setRankingHidden(hidden: Boolean): Writes to Firestore    │
│  - Snapshot Listener: /config/app_settings                   │
└──────────────────────────────┬───────────────────────────────┘
                               │
                ┌──────────────┴──────────────┐
                ▼                             ▼
┌──────────────────────────────┐ ┌──────────────────────────────┐
│        Administrador         │ │       Demais Jogadores       │
│  (google_matheus_lopes)      │ │        (Competidores)        │
├──────────────────────────────┤ ├──────────────────────────────┤
│ - Vê Banner de Controle Admin│ │ - isRankingHidden == true:   │
│ - Toggle Liga/Desliga        │ │   Exibe Tela de Suspense     │
│ - Continua vendo o Ranking   │ │   "Ranking Congelado!"       │
└──────────────────────────────┘ └──────────────────────────────┘
```

### Arquivos Envolvidos na Execução:
1. `app/src/main/java/com/example/data/parser/ResultParser.kt`:
   - Atualizar a fórmula de pontuação do Globle: `max(10, 100 - ((triesFound - 2) * 5))`.
   - Atualizar `formulaDisplay` e mensagens de cálculo.
2. `app/src/test/java/com/example/ResultParserTest.kt`:
   - Atualizar o teste de pontuação do Globle para refletir a nova regra com 7 tentativas (75 pontos).
3. `firestore.rules`:
   - Adicionar regra de leitura e gravação da coleção `/config/{configId}`.
   - Publicar regras atualizadas com `DeployRules`.
4. `app/src/main/java/com/example/data/RankusRepository.kt`:
   - Adicionar `isRankingHidden: StateFlow<Boolean>` com listener no Firestore `/config/app_settings`.
   - Adicionar método `setRankingHidden(hidden: Boolean)`.
5. `app/src/main/java/com/example/ui/screens/RankingsScreen.kt`:
   - Atualizar a seção descritiva de regras do Globle (multiplicador 5 e 20 tentativas).
   - Adicionar verificação de administrador (`isAdmin`).
   - Adicionar banner de controle do Modo Suspense para o administrador.
   - Adicionar componente Composable `SuspenseRankingScreen` exibido para usuários comuns quando o ranking estiver oculto.
6. Recompilação do applet e geração do novo APK (`rankus.apk`).
