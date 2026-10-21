# Compartilhar Meu Resultado (Resumo Diário) & Lembrete da Meia-Noite

Implementação do botão de compartilhamento diário de resultados no menu Início após registrar pelo menos 1 jogo, gerando uma imagem gamer em alta definição (quadrada 1:1) com resumo completo dos 3 jogos diários (Termo, Globle e SizeItUp), ações para salvar na galeria e compartilhar nos apps, acompanhada da correção e ativação das notificações push diárias às 00h para o Lembrete da Meia-Noite.

## User Review & Critical Decisions

> [!IMPORTANT]
> **Ajuste solicitado pelo usuário incorporado ao plano:**
> Em vez de destacar apenas um jogo, o card de compartilhamento exibirá um **resumo dos 3 jogos diários** do usuário:
> - **Termo:** Exibe quantas palavras acertou e em quantas tentativas (ex: *"4 palavras em 7 tentativas"*). Se o jogador não conseguiu decifrar alguma palavra, exibe *"🤡 Deu ruim"*.
> - **Globle:** Exibe a quantidade de tentativas necessárias para acertar (ex: *"Acertou em 6 tentativas"*).
> - **SizeItUp:** Exibe a pontuação total obtida no desafio (ex: *"92 pts"*).
> - **Jogo Não Registrado:** Para qualquer jogo que o jogador ainda não tenha lançado no dia, exibe *"🫪 N fiz"*.

---

## 1. Overview & Core Concept

- **O que faz:**
  1. **Compartilhamento Diário:** Quando o jogador possui ao menos 1 jogo concluído no dia (`hasDailyScores == true`), surge o botão iluminado **"Compartilhar meu resultado"** no topo da seção "AÇÕES RÁPIDAS" na tela Início.
  2. **Card Gamer Quadrado (1:1):** Ao tocar no botão, uma janela modal estilizada exibe a pré-visualização do card de resultados contendo:
     - Foto de perfil à esquerda (circular com anel neon);
     - Nome do jogador centralizado à direita e handle `@usuario`;
     - Pontuação total do dia ("X pts hoje") e sequência atual ("🔥 X dias seguidos");
     - **Resumo dos 3 Jogos:**
       - 🟩 **Termo Quarteto:** "4 palavras em X tentativas" | se errou: "🤡 Deu ruim" | se não jogou: "🫪 N fiz"
       - 🟦 **Globle:** "Acertou em X tentativas" | se não jogou: "🫪 N fiz"
       - 🟪 **SizeItUp:** "X pts" | se não jogou: "🫪 N fiz"
  3. **Ações do Card:**
     - **Salvar imagem:** Faz o download diretamente na galeria do dispositivo (`Pictures/RanKUs`) com aviso toast de confirmação.
     - **Compartilhar resultado:** Aciona a folha de compartilhamento nativa do Android (`Intent.ACTION_SEND` com `FileProvider`) para envio direto ao WhatsApp, Instagram, Twitter/X, Telegram, etc.
  4. **Lembrete da Meia-Noite:**
     - Solicita permissão `POST_NOTIFICATIONS` no Android 13+.
     - Dispara notificação push imediata de confirmação ao ligar o switch.
     - Agenda `AlarmManager` diário e persistente para disparar pontualmente às 00:00 todos os dias informando a virada dos novos desafios diários.

---

## 2. User Experience & Visual Design

### Fluxo do Usuário
1. **Início (Home):**
   - Caso `hasDailyScores == true`, o botão estilizado com brilho neon surge logo acima de "AÇÕES RÁPIDAS".
   - O clique abre o modal translúcido com o preview em alta resolução e botões de ação na parte inferior.
2. **Layout do Card Gerado (1080x1080px):**
   - **Cabeçalho:** Identidade visual "RanKUs" com badge de data formatada ("19 OUT 2026").
   - **Seção Superior:**
     - Lado esquerdo: Avatar circular do jogador (com anel verde neon).
     - Lado direito centralizado: Nome completo, handle e status.
   - **Métricas Globais:** Banner horizontal com "PONTUAÇÃO HOJE" e "SEQUÊNCIA ATUAL".
   - **Grid de Resumo dos Jogos:**
     - Linhas dedicadas para cada jogo com seus respectivos ícones do menu Jogos:
       - **Termo Quarteto** (Ícone verde): *4 palavras em 7 tentativas* / *🤡 Deu ruim* / *🫪 N fiz*
       - **Globle** (Ícone globo ciano): *Acertou em 5 tentativas* / *🫪 N fiz*
       - **SizeItUp** (Ícone régua violeta): *94 pts conquistados* / *🫪 N fiz*
   - **Rodapé:** Assinatura do app e selo oficial de pontuação.
3. **Menu Jogos - Lembrete da Meia-Noite:**
   - Switch interativo com estado salvo em preferências locais.
   - Ao ativar: confirmação imediata via push ("Lembrete ativado! Às 00h avisaremos dos novos jogos") e alarme recorrente às 00h00.

---

## 3. Key Product Decisions & Trade-Offs

- **Tratamento de Estados dos Jogos no Card:**
  - *Termo:* Extração estruturada a partir do `ScoreHistoryItem` ou `DailyGameStatus` de hoje. Se `score == 0` ou as tentativas estouraram sem 4 acertos, aplica *"🤡 Deu ruim"*. Se completado com sucesso, formata as tentativas e palavras.
  - *Globle:* Identificação de tentativas a partir dos detalhes registrados (ex: 6 tentativas).
  - *SizeItUp:* Exibição clara dos pontos obtidos na partida de hoje.
  - *Não Registrado:* Se o jogo não possui registro hoje, preenche de forma padronizada com *"🫪 N fiz"*.
- **Geração de Imagem com Canvas Nativo:**
  - Renderização direta em `android.graphics.Bitmap` garantindo proporção exata 1080x1080px, nítida para redes sociais e independente do formato de tela do celular.
- **Exportação e Compartilhamento:**
  - `MediaStore.Images.Media` para download local limpo na galeria.
  - `androidx.core.content.FileProvider` para compartilhamento via `Intent.ACTION_SEND`.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      HomeScreen                        │
│   (hasDailyScores == true -> Exibe botão Compartilhar) │
└──────────────────────────┬─────────────────────────────┘
                           │ Abre Modal
                           ▼
┌────────────────────────────────────────────────────────┐
│             ShareResultDialog / Overlay                │
│  - Avalia status dos 3 jogos (Termo, Globle, SizeItUp) │
│  - Renderiza Bitmap 1080x1080 com Canvas nativo:       │
│    • Avatar + Nome centralizado                        │
│    • Total hoje + Sequência de dias                    │
│    • Resumo Termo (Acertos/Tentativas | 🤡 | 🫪)       │
│    • Resumo Globle (Tentativas | 🫪)                   │
│    • Resumo SizeItUp (Pontuação total | 🫪)            │
│  - Botão "Salvar imagem" ──> MediaStore (Galeria)      │
│  - Botão "Compartilhar"  ──> FileProvider + Intent     │
└────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────┐
│              JogosScreen / MidnightReminder            │
│  - Switch "Lembrete da Meia-Noite"                     │
│  - Preferences: "midnight_reminder_enabled"            │
│  - Request Permission: POST_NOTIFICATIONS              │
│  - Agendador: MidnightReminderScheduler                │
│       └── AlarmManager -> MidnightReminderReceiver     │
│       └── NotificationManager (Canal "rankus_midnight")│
└────────────────────────────────────────────────────────┘
```

### Arquivos Envolvidos na Execução:
1. `app/src/main/AndroidManifest.xml`: Permissões (`POST_NOTIFICATIONS`), `FileProvider` e `MidnightReminderReceiver`.
2. `app/src/main/res/xml/file_paths.xml`: Configuração de caminhos do `FileProvider`.
3. `com.example.service.MidnightReminderService.kt`: Agendamento de alarme diário às 00:00 e disparador de notificações push.
4. `com.example.ui.components.ShareResultCard.kt`: Criação do Bitmap gráfico (1080x1080) com os detalhes dos 3 jogos e modal de compartilhamento.
5. `app/src/main/java/com/example/ui/screens/HomeScreen.kt`: Inclusão do botão acima de "AÇÕES RÁPIDAS" e chamada do modal.
6. `app/src/main/java/com/example/ui/screens/JogosScreen.kt`: Conexão do switch com o serviço de alarme e persistência.
