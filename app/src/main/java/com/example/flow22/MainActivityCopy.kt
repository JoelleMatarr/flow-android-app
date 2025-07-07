package com.example.flow22

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.checkout.components.core.CheckoutComponentsFactory
import com.checkout.components.interfaces.Environment
import com.checkout.components.interfaces.api.CheckoutComponents
import com.checkout.components.interfaces.component.CheckoutComponentConfiguration
import com.checkout.components.interfaces.component.ComponentCallback
import com.checkout.components.interfaces.component.ComponentOption
import com.checkout.components.interfaces.error.CheckoutError
import com.checkout.components.interfaces.model.ComponentName
import com.checkout.components.interfaces.model.PaymentMethodName
import com.checkout.components.interfaces.model.PaymentSessionResponse
import com.checkout.components.wallet.wrapper.GooglePayFlowCoordinator
import com.example.flow22.ui.theme.Flow22Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
class MainActivityCopy : ComponentActivity() {
    private lateinit var checkoutComponents: CheckoutComponents
    private lateinit var coordinator: GooglePayFlowCoordinator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val paymentSessionID = "ps_2vCYqlBGNiJjG0s03ssK8or5nT5"
        val paymentSessionSecret = "pss_834dc3e9-e1c5-45e9-9266-ab32b386d9dd"
        val paymentSessionToken = "YmFzZTY0:eyJpZCI6InBzXzJ2Q1lxbEJHTmlKakcwczAzc3NLOG9yNW5UNSIsImVudGl0eV9pZCI6ImVudF9uaHh2Y2phajc1NXJ3eno2emlkYXl5d29icSIsImV4cGVyaW1lbnRzIjp7fSwicHJvY2Vzc2luZ19jaGFubmVsX2lkIjoicGNfdGljZDZ0MnJybW51amFjYWthZnZ1a2hid3UiLCJhbW91bnQiOjEwLCJsb2NhbGUiOiJlbi1HQiIsImN1cnJlbmN5IjoiQUVEIiwicGF5bWVudF9tZXRob2RzIjpbeyJ0eXBlIjoiY2FyZCIsImNhcmRfc2NoZW1lcyI6WyJWaXNhIiwiTWFzdGVyY2FyZCJdLCJzY2hlbWVfY2hvaWNlX2VuYWJsZWQiOmZhbHNlLCJzdG9yZV9wYXltZW50X2RldGFpbHMiOiJkaXNhYmxlZCJ9LHsidHlwZSI6ImFwcGxlcGF5IiwiZGlzcGxheV9uYW1lIjoidGVzdCIsImNvdW50cnlfY29kZSI6IlNBIiwiY3VycmVuY3lfY29kZSI6IkFFRCIsIm1lcmNoYW50X2NhcGFiaWxpdGllcyI6WyJzdXBwb3J0czNEUyJdLCJzdXBwb3J0ZWRfbmV0d29ya3MiOlsidmlzYSIsIm1hc3RlckNhcmQiXSwidG90YWwiOnsibGFiZWwiOiJ0ZXN0IiwidHlwZSI6ImZpbmFsIiwiYW1vdW50IjoiMC4xIn19LHsidHlwZSI6Imdvb2dsZXBheSIsIm1lcmNoYW50Ijp7ImlkIjoiMDgxMTMwODkzODYyNjg4NDk5ODIiLCJuYW1lIjoidGVzdCIsIm9yaWdpbiI6Imh0dHBzOi8vZXhhbXBsZS5jb20ifSwidHJhbnNhY3Rpb25faW5mbyI6eyJ0b3RhbF9wcmljZV9zdGF0dXMiOiJGSU5BTCIsInRvdGFsX3ByaWNlIjoiMC4xIiwiY291bnRyeV9jb2RlIjoiU0EiLCJjdXJyZW5jeV9jb2RlIjoiQUVEIn0sImNhcmRfcGFyYW1ldGVycyI6eyJhbGxvd2VkX2F1dGhfbWV0aG9kcyI6WyJQQU5fT05MWSIsIkNSWVBUT0dSQU1fM0RTIl0sImFsbG93ZWRfY2FyZF9uZXR3b3JrcyI6WyJWSVNBIiwiTUFTVEVSQ0FSRCJdfX1dLCJmZWF0dXJlX2ZsYWdzIjpbImFuYWx5dGljc19vYnNlcnZhYmlsaXR5X2VuYWJsZWQiLCJnZXRfd2l0aF9wdWJsaWNfa2V5X2VuYWJsZWQiLCJsb2dzX29ic2VydmFiaWxpdHlfZW5hYmxlZCIsInJpc2tfanNfZW5hYmxlZCIsInVzZV9ub25fYmljX2lkZWFsX2ludGVncmF0aW9uIl0sInJpc2siOnsiZW5hYmxlZCI6ZmFsc2V9LCJtZXJjaGFudF9uYW1lIjoidGVzdCIsInBheW1lbnRfc2Vzc2lvbl9zZWNyZXQiOiJwc3NfODM0ZGMzZTktZTFjNS00NWU5LTkyNjYtYWIzMmIzODZkOWRkIiwiaW50ZWdyYXRpb25fZG9tYWluIjoiYXBpLnNhbmRib3guY2hlY2tvdXQuY29tIn0=";
        coordinator = GooglePayFlowCoordinator(
            context = this, // Now `this` is safe
            handleActivityResult = { resultCode, data ->
                handleActivityResult(resultCode, data)
            }
        )

        val flowCoordinators = mapOf(PaymentMethodName.GooglePay to coordinator)



        initializeCheckoutSDK(
            paymentSessionID = paymentSessionID,
            paymentSessionToken = paymentSessionToken,
            paymentSessionSecret = paymentSessionSecret,
            flowCoordinators = flowCoordinators
        )
    }
    val customComponentCallback = ComponentCallback(
        onReady = { component ->
            Log.d("flow component", "test onReady " + component.name)
        },
        onSubmit = { component ->
            Log.d("flow component ", "test onSubmit " + component.name)
        },
        onSuccess = { component, paymentID ->
            Log.d("flow component payment success ${component.name}", paymentID)
        },
        onError = { component, checkoutError ->
            Log.d(
                "flow component callback Error",
                "onError " + checkoutError.message + ", " + checkoutError.code
            )
        },
    )

    private fun handleActivityResult(resultCode: Int, data: String) {
        checkoutComponents.handleActivityResult(resultCode, data)
    }

    private fun initializeCheckoutSDK(
        paymentSessionID: String,
        paymentSessionToken: String,
        paymentSessionSecret: String,
        flowCoordinators: Map<PaymentMethodName, GooglePayFlowCoordinator>,
    ) {
        val configuration = CheckoutComponentConfiguration(
            context = this,
            paymentSession = PaymentSessionResponse(
                id = paymentSessionID,
                paymentSessionToken = paymentSessionToken,
                paymentSessionSecret = paymentSessionSecret,
            ),
            componentCallback = customComponentCallback,
            publicKey = "pk_sbox_cwlkrqiyfrfceqz2ggxodhda2yh",
            environment = Environment.SANDBOX,
            flowCoordinators = flowCoordinators
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                checkoutComponents = CheckoutComponentsFactory(config = configuration).create()

                val flow = checkoutComponents.create(PaymentMethodName.Card, ComponentOption(showPayButton = false))
                val googlePay = checkoutComponents.create(PaymentMethodName.GooglePay)
    print(googlePay)
                withContext(Dispatchers.Main) {
                    setContent {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Render Flow Component
                            AndroidView(
                                factory = { context ->
                                    val flowContainer = FrameLayout(context)
                                    flowContainer.addView(flow.provideView(flowContainer))
                                    flowContainer
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Render Google Pay Component
                            AndroidView(
                                factory = { context ->
                                    val googlePayContainer = FrameLayout(context)
                                    googlePayContainer.addView(googlePay.provideView(googlePayContainer))
                                    googlePayContainer
                                }
                            )
                        }
                    }
                }
            } catch (checkoutError: CheckoutError) {
                Log.d(
                    "flow component error on init",
                    "Error initializing Checkout SDK: ${checkoutError.message}, Details: ${checkoutError.details}"
                )
            }
        }
    }
}
