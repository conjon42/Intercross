package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.models.Metadata

class MetadataViewModel(private val repo: MetadataRepository): BaseViewModel<Metadata>(repo) {

    val metadata = repo.selectAll()

    fun getId(property: String): Int = repo.getId(property)

    fun insert(item: Metadata): Long = repo.insert(item)

}
