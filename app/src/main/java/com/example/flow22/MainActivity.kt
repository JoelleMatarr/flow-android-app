package com.example.flow22

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.api.PaymentMethodComponent
import com.checkout.components.interfaces.component.RememberMeConfiguration
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.interfaces.model.Phone
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import com.example.flow22.ui.theme.Flow22Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.checkout.components.interfaces.uicustomisation.font.FontName
import com.checkout.components.interfaces.uicustomisation.BorderRadius
import com.checkout.components.interfaces.uicustomisation.designtoken.ColorTokens
import com.checkout.components.interfaces.uicustomisation.font.*
import com.checkout.components.interfaces.uicustomisation.designtoken.DesignTokens


class MainActivity : ComponentActivity() {

    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val paymentSessionID = "ps_35urKj9OCYGmQBvGxA8Cu99v3VP"
        val paymentSessionSecret = "pss_801d3f56-e7f9-4419-bc32-c29dbc9f9345"

        coordinator = GooglePayFlowCoordinator(
            context = this,
            handleActivityResult = { resultCode, data ->
                handleActivityResult(resultCode, data)
            }
        )

        val flowCoordinators = mapOf(
            PaymentMethodName.GooglePay to coordinator
        )

        initializeCheckoutSDK(
            paymentSessionID = paymentSessionID,
            paymentSessionSecret = paymentSessionSecret,
            flowCoordinators = flowCoordinators
        )
    }

    private val customComponentCallback = ComponentCallback(
        onReady = { component ->
            Log.d("flow component", "onReady: " + component.name)
        },
        onSubmit = { component ->
            Log.d("flow component", "onSubmit: " + component.name)
        },
        onSuccess = { component, paymentID ->
            Log.d("flow success ${component.name}", paymentID)
        },
        onError = { component, checkoutError ->
            Log.e(
                "flow error",
                "onError ${checkoutError.message}, code=${checkoutError.code}"
            )
        }
    )

    private fun handleActivityResult(resultCode: Int, data: String) {
        checkoutComponents.handleActivityResult(resultCode, data)
    }

    private fun initializeCheckoutSDK(
        paymentSessionID: String,
        paymentSessionSecret: String,
        flowCoordinators: Map<PaymentMethodName, GooglePayFlowCoordinator>,
    ) {
        val designTokens = DesignTokens(
            colorTokens = ColorTokens(
                colorPrimary = 0xFFB8B8B8.toLong(),
                colorAction = 0xFF0A84FF.toLong(),
                colorBackground = 0xFFFFFFFF.toLong(),
//                colorBorder = 0xFFEA5D29.toLong(),
                colorDisabled = 0xFFB8B8B8.toLong(),
                colorFormBackground = 0xFFFFFFFF.toLong(),
                colorFormBorder = 0xFFC9C9C9.toLong(),
                colorInverse = 0xFFFFFFFF.toLong(),
                colorOutline = 0xFFEA5D29.toLong(),
                colorSecondary = 0xFF000000.toLong(),
                colorSuccess = 0xFFEA5D29.toLong(),
                colorError = 0xFFFF0000.toLong()
            ),
            borderButtonRadius = BorderRadius(all = 20),
            borderFormRadius = BorderRadius(all = 12),
            fonts = mapOf(
                FontName.Subheading to Font(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                ),
                FontName.Input to Font(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                ),
                FontName.Button to Font(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                ),
                FontName.Label to Font(
                    fontStyle = FontStyle.Normal,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.SansSerif,
                ),
            )
        )

        val configuration = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret,
            ),
            componentCallback = customComponentCallback,
            publicKey = "pk_sbox_zxmkbjyj4ec7liyyup23gjfsga#",
            environment = Environment.SANDBOX,
            flowCoordinators = flowCoordinators,
            appearance = designTokens
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Remember Me
                val rememberMeConfiguration = RememberMeConfiguration(
                    data = RememberMeConfiguration.Data(
                        email = "jheng-hao.lin8@checkout.com",
                        phone = Phone(countryCode = "971", number = "524123793")
                    ),
                    showPayButton = true
                )

                // Factory
                checkoutComponents = CheckoutComponentsFactory(config = configuration).create()

                // Individual components
                val cardComponent: PaymentMethodComponent = checkoutComponents.create(
                    PaymentMethodName.Card,
                    ComponentOption(showPayButton = true, rememberMeConfiguration = rememberMeConfiguration)
                )
                val googlePayComponent: PaymentMethodComponent =
                    checkoutComponents.create(PaymentMethodName.GooglePay)

                val isCardAvailable = cardComponent.isAvailable()
                val isGooglePayAvailable = googlePayComponent.isAvailable()

                withContext(Dispatchers.Main) {
                    setContent {
                        Flow22Theme {
                            PaymentScreen(
                                cardComponent = cardComponent,
                                googlePayComponent = googlePayComponent,
                                isCardAvailable = isCardAvailable,
                                isGooglePayAvailable = isGooglePayAvailable,
                                onBack = { finish() }
                            )
                        }
                    }
                }

            } catch (checkoutError: CheckoutError) {
                Log.e(
                    "flow init error",
                    "Checkout init failed: ${checkoutError.message}, details=${checkoutError.details}"
                )
            }
        }
    }
}

// ---------- UI ----------

private enum class PaymentOption {
    CARD,
    GOOGLE_PAY,
    KLARNA
}
@Composable
private fun PaymentScreen(
    cardComponent: PaymentMethodComponent,
    googlePayComponent: PaymentMethodComponent,
    isCardAvailable: Boolean,
    isGooglePayAvailable: Boolean,
    onBack: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(PaymentOption.CARD) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA)) // light sheet background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {

            // Header (fixed, centered, white)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = "PAYMENT",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = Color(0xFF111827)
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111827)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scrollable content (Card / GPay / Klarna)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // CARD
                PaymentMethodRow(
                    option = PaymentOption.CARD,
                    selected = selectedOption == PaymentOption.CARD,
                    onClick = { selectedOption = PaymentOption.CARD },
                    expandedContent = {
                        if (isCardAvailable) {
                            AndroidView(
                                factory = { context ->
                                    val flowContainer = FrameLayout(context)
                                    flowContainer.addView(cardComponent.provideView(flowContainer))
                                    flowContainer
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GOOGLE PAY
                PaymentMethodRow(
                    option = PaymentOption.GOOGLE_PAY,
                    selected = selectedOption == PaymentOption.GOOGLE_PAY,
                    onClick = { selectedOption = PaymentOption.GOOGLE_PAY },
                    expandedContent = {
                        if (isGooglePayAvailable) {
                            AndroidView(
                                factory = { context ->
                                    val googlePayContainer = FrameLayout(context)
                                    googlePayContainer.addView(
                                        googlePayComponent.provideView(googlePayContainer)
                                    )
                                    googlePayContainer
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // KLARNA (static)
                PaymentMethodRow(
                    option = PaymentOption.KLARNA,
                    selected = selectedOption == PaymentOption.KLARNA,
                    onClick = { selectedOption = PaymentOption.KLARNA },
                    expandedContent = {
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            onClick = {
                                Log.d("klarna", "Klarna pay button clicked")
                            }
                        ) {
                            Text("Pay with Klarna")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    option: PaymentOption,
    selected: Boolean,
    onClick: () -> Unit,
    expandedContent: @Composable () -> Unit = {}
) {
    val (label, iconContent) = when (option) {
        PaymentOption.CARD -> "Card" to @Composable {
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 24.dp)
                    .background(Color(0xFF111827), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Card",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        PaymentOption.GOOGLE_PAY -> "Google Pay" to @Composable {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 24.dp)
                    .background(Color.White, RoundedCornerShape(6.dp)),
//                    .border(1.dp, Color(0xFFFFFF), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G Pay",
                    color = Color(0xFF111827),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        PaymentOption.KLARNA -> "Klarna" to @Composable {
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 24.dp)
                    .background(Color(0xFFFFB3C7), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Klarna",
                    color =Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }

    val borderColor = if (selected) Color.White else Color.White
    val backgroundColor = if (selected) Color.White else Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
//        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                iconContent()
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )


                Spacer(modifier = Modifier.width(8.dp))

                RadioButton(
                    selected = selected,
                    onClick = onClick
                )
            }

            if (selected) {
                Spacer(modifier = Modifier.height(8.dp))
                expandedContent()
            }
        }
    }
}