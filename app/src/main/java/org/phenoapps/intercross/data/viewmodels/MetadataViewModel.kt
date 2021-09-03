package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.models.Meta

class MetadataViewModel(private val repo: MetadataRepository): BaseViewModel<Meta>(repo) {

    val metadata = repo.selectAll()

    fun getId(property: String): Int = repo.getId(property)

    fun insert(item: Meta): Long = repo.insert(item)

}
