package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBgBase
import com.example.ui.theme.DarkBgSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanContainer
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonGreenContainer
import com.example.ui.theme.NeonGreenDark
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.NeonVioletContainer
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SurfaceBright
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.SurfaceContainerLowest
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun JogosScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var midnightReminderEnabled by remember { mutableStateOf(true) }

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBgBase)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Screen Header
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Acessar Jogos",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Escolha seu desafio do dia e jogue direto nas plataformas oficiais. Depois é só voltar e cadastrar seu resultado!",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }

        // Card 1: TERMO (Featured Emerald)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, NeonGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 2x2 Styled Tiles
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceContainerLowest)
                                .padding(4.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                TileBox("T", NeonGreen, DarkBgBase)
                                TileBox("E", SurfaceContainerHigh, TextPrimary)
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                TileBox("R", NeonVioletContainer, TextPrimary)
                                TileBox("M", NeonGreen, DarkBgBase)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = "Termo", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Palavras Diárias", fontSize = 10.sp, color = NeonGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "O famoso jogo diário das palavras em português. Descubra os segredos do Termo Quarteto em até 9 tentativas!",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagBadge("Quarteto (4)")
                    TagBadge("Oficial: term.ooo/4")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { openUrl("https://term.ooo/4") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreenContainer,
                        contentColor = NeonGreenDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("play_termo_btn")
                ) {
                    Text(text = "Jogar Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.NorthEast, contentDescription = null, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Card 2: GLOBLE (Thermal / Cyan)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = "Globle", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Geografia", fontSize = 10.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Adivinhe o país misterioso no menor número de tentativas pelo mapa térmico interativo.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagBadge("Mapa Dinâmico")
                    TagBadge("Quente & Frio")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { openUrl("https://globle-game.com") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceBright,
                        contentColor = ElectricCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("play_globle_btn")
                ) {
                    Text(text = "Jogar Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.NorthEast, contentDescription = null, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Card 3: SIZEITUP (Violet / Scale)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, NeonViolet.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceContainerLowest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Straighten, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(text = "SizeItUp", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Lógica & Curiosidades", fontSize = 10.sp, color = NeonViolet, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Icon(imageVector = Icons.Default.Scale, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(22.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Compare e ordene grandezas, tamanhos e escalas em desafios diários intrigantes de proporção.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TagBadge("Escalas Reais")
                    TagBadge("Ordenação")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { openUrl("https://magnitudle.com/size-it-up") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceBright,
                        contentColor = NeonViolet
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("play_sizeitup_btn")
                ) {
                    Text(text = "Jogar Agora", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.NorthEast, contentDescription = null, modifier = Modifier.size(15.dp))
                }
            }
        }

        // Community suggestions
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Mais Desafios da Comunidade", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(text = "2 Populares", fontSize = 10.sp, color = TextSecondary)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CommunityCard(
                        title = "Conexo",
                        category = "Associação",
                        desc = "Agrupe 16 palavras em 4 conjuntos de temas.",
                        icon = Icons.Default.Hub,
                        color = NeonGreen,
                        onOpen = { openUrl("https://conexo.ws") },
                        modifier = Modifier.weight(1f)
                    )
                    CommunityCard(
                        title = "Wordle",
                        category = "Inglês",
                        desc = "O clássico global original do The New York Times.",
                        icon = Icons.Default.Translate,
                        color = ElectricCyan,
                        onOpen = { openUrl("https://www.nytimes.com/games/wordle") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // How it works 3-step section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceContainerHigh)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Como funciona a pontuação", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cadastrar seu desempenho é instantâneo e 100% automatizado em 3 passos simples:",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                StepItem("1", "Jogue na página oficial", "Abra o Termo, Globle ou SizeItUp e conclua o puzzle diário no seu tempo.", NeonGreen)
                Spacer(modifier = Modifier.height(8.dp))
                StepItem("2", "Copie o resultado gerado", "Ao finalizar a partida, clique no botão nativo de compartilhar (o grid de emojis e tentativas).", ElectricCyan)
                Spacer(modifier = Modifier.height(8.dp))
                StepItem("3", "Cole no RanKUs e pontue!", "Vá para a aba \"Cadastrar\", cole o texto. Nosso sistema valida os dados e você sobe no ranking!", NeonViolet)
            }
        }

        // Midnight Reminder Switch
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceContainer)
                    .border(1.dp, OutlineBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = "Lembrete da Meia-Noite", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(text = "Receba um alerta suave quando os novos desafios virarem às 00:00.", fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = midnightReminderEnabled,
                    onCheckedChange = { midnightReminderEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonGreenDark,
                        checkedTrackColor = NeonGreenContainer,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = SurfaceContainerHigh
                    )
                )
            }
        }

        // Safe browsing footer note
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceContainerLow)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Links abrem direto no navegador padrão externo. Conexão oficial e segura de cada autor.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TileBox(letter: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(text = letter, fontSize = 11.sp, fontWeight = FontWeight.Black, color = fg)
    }
}

@Composable
private fun TagBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceContainerLowest)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CommunityCard(
    title: String,
    category: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceContainerLow)
            .border(1.dp, OutlineBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Text(text = category, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontSize = 10.sp, color = TextSecondary, maxLines = 2)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceContainerHigh)
                .clickable { onOpen() }
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Acessar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.width(3.dp))
            Icon(imageVector = Icons.Default.NorthEast, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
private fun StepItem(num: String, title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(text = num, fontSize = 12.sp, fontWeight = FontWeight.Black, color = DarkBgBase)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = desc, fontSize = 11.sp, color = TextSecondary, lineHeight = 15.sp)
        }
    }
}
