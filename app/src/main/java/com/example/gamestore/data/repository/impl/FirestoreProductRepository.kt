package com.example.gamestore.data.repository.impl

import com.example.gamestore.data.ProductMapper
import com.example.gamestore.data.repository.ProductRepository
import com.example.gamestore.model.GameProduct
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreProductRepository(
    private val firestoreProvider: () -> FirebaseFirestore = {
        FirebaseFirestore.getInstance()
    },
) : ProductRepository {

    override fun observeProducts(): Flow<List<GameProduct>> = callbackFlow {

        val registration = firestoreProvider()
            .collection("products")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    return@addSnapshotListener
                }

                try {
                    val products = snapshot.documents
                        .map { document ->
                            ProductMapper.fromDocument(
                                id = document.id,
                                data = document.data.orEmpty(),
                            )
                        }
                        .sortedBy { it.id }

                    trySend(products)

                } catch (exception: IllegalArgumentException) {
                    close(exception)
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}