package com.example.flow22

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    private var isGpayAvailable: Boolean = false
    private var isCardAvailable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val paymentSessionID = "ps_2zYCihjAPG3le91cTrmSsoGyiVg"
        val paymentSessionSecret = "pss_a7f3c38f-f2ec-4740-9a4a-4cf988fe74f2"

        coordinator = GooglePayFlowCoordinator(
            context = this,
            handleActivityResult = { resultCode, data ->
                handleActivityResult(resultCode, data)
            }
        )

        val flowCoordinators = mapOf(PaymentMethodName.GooglePay to coordinator)

        initializeCheckoutSDK(
            paymentSessionID = paymentSessionID,
            paymentSessionSecret = paymentSessionSecret,
            flowCoordinators = flowCoordinators
        )
    }

    private val customComponentCallback = ComponentCallback(
        onReady = { component ->
            Log.d("flow component", "onReady ${component.name}")
        },
        onSubmit = { component ->
            Log.d("flow component", "onSubmit ${component.name}")
        },
        onSuccess = { component, paymentID ->
            Log.d("flow component success", "${component.name}: $paymentID")
        },
        onError = { component, checkoutError ->
            Log.e("flow component error", "${checkoutError.message}, ${checkoutError.code}")
        },
    )

    private fun handleActivityResult(resultCode: Int, data: String) {
        checkoutComponents.handleActivityResult(resultCode, data)
    }

    private fun initializeCheckoutSDK(
        paymentSessionID: String,
        paymentSessionSecret: String,
        flowCoordinators: Map<PaymentMethodName, GooglePayFlowCoordinator>,
    ) {
        val configuration = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                secret = paymentSessionSecret,
            ),
            componentCallback = customComponentCallback,
            publicKey = "pk_sbox_cwlkrqiyfrfceqz2ggxodhda2yh",
            environment = Environment.SANDBOX,
            flowCoordinators = flowCoordinators
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                checkoutComponents = CheckoutComponentsFactory(config = configuration).create()

                var flow = checkoutComponents.create(
                    PaymentMethodName.Card,
                    ComponentOption(showPayButton = true)
                )
                var googlePay = checkoutComponents.create(PaymentMethodName.GooglePay)

                isCardAvailable = flow.isAvailable()
                isGpayAvailable = googlePay.isAvailable()

                withContext(Dispatchers.Main) {
                    setContent {
                        var selectedMethod by remember { mutableStateOf("") }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row {
                                Button(
                                    onClick = { selectedMethod = "card" },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pay with Card")
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = { selectedMethod = "gpay" },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Pay with GooglePay")
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            when (selectedMethod) {
                                "card" -> {
                                    if (isCardAvailable) {
                                        AndroidView(
                                            factory = { context ->
                                                FrameLayout(context).apply {
                                                    addView(flow.provideView(this))
                                                }
                                            }
                                        )
                                    } else {
                                        Text("Card payment is not available.")
                                    }
                                }

                                "gpay" -> {
                                    if (isGpayAvailable) {
                                        AndroidView(
                                            factory = { context ->
                                                FrameLayout(context).apply {
                                                    addView(googlePay.provideView(this))
                                                }
                                            }
                                        )
                                    } else {
                                        Text("Google Pay is not available.")
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (checkoutError: CheckoutError) {
                Log.e(
                    "flow component error",
                    "Initialization error: ${checkoutError.message}, Details: ${checkoutError.details}"
                )
            }
        }
    }
}
