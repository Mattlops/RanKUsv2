package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.models.UserProfile
import com.example.ui.theme.DarkBgSurface
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.OutlineBorder
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TopHeader(
    currentTitle: String,
    userProfile: UserProfile,
    onAvatarClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBgSurface.copy(alpha = 0.98f))
            .statusBarsPadding()
            .padding(top = 12.dp, bottom = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo Badge (Clickable to return Home)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainerHigh)
                    .border(1.5.dp, NeonGreen.copy(alpha = 0.6f), CircleShape)
                    .clickable { onHomeClick() }
                    .testTag("header_logo_btn"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.rankus_butt_trophy_icon_1792015743499),
                    contentDescription = "RanKUs Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Brand Name (Clickable to return Home)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onHomeClick() }
                    .padding(vertical = 4.dp)
                    .testTag("header_brand_name")
            ) {
                Text(
                    text = "Ran",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "KU",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGreen
                )
                Text(
                    text = "s",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Botão Início Direto no Top Header
            val isAtHome = currentTitle == "Início"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAtHome) NeonGreen.copy(alpha = 0.2f) else SurfaceContainer)
                    .border(
                        1.dp,
                        if (isAtHome) NeonGreen.copy(alpha = 0.5f) else OutlineBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onHomeClick() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("header_home_btn"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Início",
                        tint = if (isAtHome) NeonGreen else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Início",
                        fontSize = 11.sp,
                        fontWeight = if (isAtHome) FontWeight.Bold else FontWeight.Medium,
                        color = if (isAtHome) NeonGreen else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Screen Name context pill (if not at Home)
            if (!isAtHome) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceContainerHigh.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentTitle,
                        color = TextPrimary.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }

            // User Avatar with glowing border and online dot
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clickable { onAvatarClick() }
                    .testTag("header_avatar_btn"),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = userProfile.avatarUrl,
                    contentDescription = "Foto de Perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(2.dp, NeonGreen.copy(alpha = 0.6f), CircleShape)
                )
                // Green online indicator
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(NeonGreen)
                        .border(1.5.dp, DarkBgSurface, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}
