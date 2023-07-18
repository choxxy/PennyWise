package com.iogarage.ke.pennywise.service

import com.iogarage.ke.pennywise.util.toModel
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.extensions.toJson
import io.appwrite.models.Document
import io.appwrite.models.DocumentList
import io.appwrite.services.Databases

abstract class AppWriteService constructor(open val databases: Databases) {
    protected val databaseId = "64a3094d05cc0d23b617"
    open val myCollectionId: String get() = TODO()

    protected suspend fun getDocument(
        collectionId: String = "", documentId: String,
    ): Document<Map<String, Any>> {
        return databases.getDocument(
            databaseId,
            collectionId.ifEmpty { myCollectionId },
            documentId = documentId
        )
    }


    protected suspend fun deleteDocument(
        collectionId: String = "",
        documentId: String,
    ) {
        databases.deleteDocument(
            databaseId,
            collectionId.ifEmpty { myCollectionId },
            documentId
        )
    }

    protected suspend inline fun <reified T : Any> updateDocumentAndGet(
        model: T,
        collectionId: String = "",
        documentId: String = ID.unique(),
    ): Document<Map<String, Any>> {
        val json = model.toJson()
        return databases.updateDocument(
            databaseId,
            collectionId.ifEmpty { myCollectionId },
            documentId,
            data = json
        )
    }

    protected suspend inline fun <reified T : Any> uploadDocumentAndGet(
        model: T,
        collectionId: String = "",
        documentId: String = ID.unique(),
    ): T {
        val json = model.toJson()
        return databases.createDocument(
            databaseId,
            collectionId.ifEmpty { myCollectionId },
            documentId,
            data = json
        ).data.toModel<T>()
    }

    protected suspend fun getAllDocuments(
        collectionId: String = "",
        queries: List<String> = emptyList(),
    ): DocumentList<Map<String, Any>> {
        return databases.listDocuments(
            databaseId = databaseId,
            collectionId.ifEmpty { myCollectionId },
            queries = queries.ifEmpty { null }
        )
    }

    protected suspend inline fun <reified T : Any> getAllDocumentsRecursiveWithQuerySearch(
        collectionId: String = "",
        queryAttribute: String,
        queryValue: String,
    ): List<T> {
        val query = Query.search(queryAttribute, queryValue)
        return getAllDocumentsRecursiveWithQuery(collectionId, query)
    }

    protected suspend inline fun <reified T : Any> getAllDocumentsRecursiveWithQueryEqual(
        collectionId: String = "",
        queryAttribute: String,
        queryValue: String,
    ): List<T> {
        val query = Query.equal(queryAttribute, queryValue)
        return getAllDocumentsRecursiveWithQuery<T>(collectionId, query)
    }


    protected suspend inline fun <reified T : Any> getAllDocumentsRecursiveWithQuery(
        collectionId: String = "",
        query: String,
    ) = getAllDocumentsPureRecursiveWithQuery(collectionId, query).map {
        it.data.toModel<T>()
    }


    protected suspend inline fun getAllDocumentsPureRecursiveWithQuery(
        collectionId: String = "",
        query: String,
    ): MutableList<Document<Map<String, Any>>> {

        val queries = mutableListOf(Query.limit(1), Query.offset(0))
        queries.add(query)

        val responseFirst = getAllDocuments(
            collectionId.ifEmpty { myCollectionId },
            queries = queries
        )

        val limit = 25
        val totalDocuments = responseFirst.total

        val documents = mutableListOf<Document<Map<String, Any>>>()

        for (offset in 0 until totalDocuments step limit.toLong()) {
            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId.ifEmpty { myCollectionId },
                queries = mutableListOf(Query.limit(limit), Query.offset(offset.toInt()), query)
            )
            documents.addAll(response.documents)
        }

        return documents
    }


}