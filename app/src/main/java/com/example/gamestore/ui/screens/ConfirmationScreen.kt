package com.example.gamestore.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gamestore.model.OrderReceipt

@Composable
fun ConfirmationScreen(
    receipt: OrderReceipt,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Pedido confirmado",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("ID del pedido: ${receipt.id}")

        receipt.lines.forEach { line ->
            Text(
                "${line.productName} · ${line.quantity} × " +
                        "$${"%.2f".format(line.unitPrice)}"
            )
        }

        Text("Total: $${"%.2f".format(receipt.total)}")
        Text("Cliente: ${receipt.customerName}")
        Text("Teléfono: ${receipt.phone}")
        Text("Facturación: ${receipt.billingType.label}")

        receipt.nit?.let {
            Text("NIT: $it")
        }

        receipt.businessName?.let {
            Text("Razón social: $it")
        }

        Text("Pago: ${receipt.paymentMethod.label}")

        Button(onClick = onDone) {
            Text("Volver al catálogo")
        }
    }
}
